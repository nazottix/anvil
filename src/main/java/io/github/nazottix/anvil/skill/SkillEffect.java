package io.github.nazottix.anvil.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * スキル効果定義
 *
 * スキルノードが提供する単一の効果を定義します。
 *
 * 仕様書参照: docs/03_スキルツリーシステム.md
 */
public record SkillEffect(
        // 効果タイプ
        EffectType type,
        // ステータスIDまたは特殊効果ID
        String targetId,
        // 効果値
        float value,
        // 演算方法
        Operation operation
) {

    // ============================================
    // 効果タイプ
    // ============================================

    /**
     * 効果の種類
     */
    public enum EffectType {
        /** ステータス修正（採掘速度、攻撃力など） */
        STAT("stat"),
        /** 特殊効果（キーストーン用） */
        SPECIAL("special"),
        /** 属性ダメージ追加 */
        ELEMENT("element"),
        /** リソース修正（耐久、容量など） */
        RESOURCE("resource");

        private final String id;

        EffectType(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static EffectType fromId(String id) {
            for (EffectType type : values()) {
                if (type.id.equals(id)) {
                    return type;
                }
            }
            return STAT;
        }
    }

    /**
     * 演算方法
     */
    public enum Operation {
        /** 固定値加算 */
        FLAT("flat"),
        /** パーセント加算 */
        PERCENT("percent"),
        /** 乗算（最終計算） */
        MULTIPLY("multiply"),
        /** 特殊処理 */
        SPECIAL("special");

        private final String id;

        Operation(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static Operation fromId(String id) {
            for (Operation op : values()) {
                if (op.id.equals(id)) {
                    return op;
                }
            }
            return FLAT;
        }
    }

    // ============================================
    // Codec
    // ============================================

    /**
     * 保存/読み込み用Codec
     */
    public static final Codec<SkillEffect> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("type")
                            .xmap(EffectType::fromId, EffectType::getId)
                            .forGetter(SkillEffect::type),
                    Codec.STRING.fieldOf("target_id").forGetter(SkillEffect::targetId),
                    Codec.FLOAT.fieldOf("value").forGetter(SkillEffect::value),
                    Codec.STRING.optionalFieldOf("operation", "percent")
                            .xmap(Operation::fromId, Operation::getId)
                            .forGetter(SkillEffect::operation)
            ).apply(instance, SkillEffect::new)
    );

    /**
     * ネットワーク通信用StreamCodec
     */
    public static final StreamCodec<ByteBuf, SkillEffect> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SkillEffect decode(ByteBuf buf) {
            EffectType type = EffectType.fromId(ByteBufCodecs.STRING_UTF8.decode(buf));
            String targetId = ByteBufCodecs.STRING_UTF8.decode(buf);
            float value = buf.readFloat();
            Operation operation = Operation.fromId(ByteBufCodecs.STRING_UTF8.decode(buf));
            return new SkillEffect(type, targetId, value, operation);
        }

        @Override
        public void encode(ByteBuf buf, SkillEffect effect) {
            ByteBufCodecs.STRING_UTF8.encode(buf, effect.type().getId());
            ByteBufCodecs.STRING_UTF8.encode(buf, effect.targetId());
            buf.writeFloat(effect.value());
            ByteBufCodecs.STRING_UTF8.encode(buf, effect.operation().getId());
        }
    };

    // ============================================
    // ファクトリメソッド
    // ============================================

    /**
     * ステータスパーセント修正を作成
     */
    public static SkillEffect statPercent(String statId, float percent) {
        return new SkillEffect(EffectType.STAT, statId, percent, Operation.PERCENT);
    }

    /**
     * ステータス固定値修正を作成
     */
    public static SkillEffect statFlat(String statId, float value) {
        return new SkillEffect(EffectType.STAT, statId, value, Operation.FLAT);
    }

    /**
     * 特殊効果を作成
     */
    public static SkillEffect special(String specialId, float value) {
        return new SkillEffect(EffectType.SPECIAL, specialId, value, Operation.SPECIAL);
    }

    /**
     * 属性ダメージを作成
     */
    public static SkillEffect element(String elementId, float value) {
        return new SkillEffect(EffectType.ELEMENT, elementId, value, Operation.FLAT);
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * 効果がボーナスかどうか
     */
    public boolean isPositive() {
        return value > 0;
    }

    /**
     * 効果がデメリットかどうか
     */
    public boolean isNegative() {
        return value < 0;
    }

    /**
     * 表示用フォーマット文字列を取得
     */
    public String getDisplayString() {
        String prefix = value >= 0 ? "+" : "";
        return switch (operation) {
            case FLAT -> String.format("%s%.1f %s", prefix, value, targetId);
            case PERCENT -> String.format("%s%.0f%% %s", prefix, value, targetId);
            case MULTIPLY -> String.format("x%.2f %s", 1 + value / 100, targetId);
            case SPECIAL -> String.format("%s: %.0f", targetId, value);
        };
    }
}
