package io.github.nazottix.anvil.mod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * MOD効果の定義
 *
 * MODが提供する単一の効果（ステータス修正）を表します。
 * 複合MODは複数のModEffectを持ちます。
 */
public record ModEffect(
        // 効果対象のステータスID（例: "damage", "mining_speed", "durability"）
        String statId,
        // 効果値（正の値=ボーナス、負の値=デメリット）
        float value,
        // 効果タイプ（加算/乗算）
        EffectType effectType
) {

    // ============================================
    // 効果タイプ
    // ============================================

    /**
     * 効果の適用方法
     */
    public enum EffectType {
        /** 加算（例: +10ダメージ） */
        ADDITIVE("additive"),
        /** 乗算（例: +10%ダメージ） */
        MULTIPLICATIVE("multiplicative");

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
            return ADDITIVE;
        }
    }

    // ============================================
    // Codec
    // ============================================

    /**
     * 保存/読み込み用Codec
     */
    public static final Codec<ModEffect> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("stat_id").forGetter(ModEffect::statId),
                    Codec.FLOAT.fieldOf("value").forGetter(ModEffect::value),
                    Codec.STRING.optionalFieldOf("effect_type", "additive")
                            .xmap(EffectType::fromId, EffectType::getId)
                            .forGetter(ModEffect::effectType)
            ).apply(instance, ModEffect::new)
    );

    /**
     * ネットワーク通信用StreamCodec
     */
    public static final StreamCodec<ByteBuf, ModEffect> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ModEffect decode(ByteBuf buf) {
            String statId = ByteBufCodecs.STRING_UTF8.decode(buf);
            float value = buf.readFloat();
            String effectTypeId = ByteBufCodecs.STRING_UTF8.decode(buf);
            return new ModEffect(statId, value, EffectType.fromId(effectTypeId));
        }

        @Override
        public void encode(ByteBuf buf, ModEffect effect) {
            ByteBufCodecs.STRING_UTF8.encode(buf, effect.statId());
            buf.writeFloat(effect.value());
            ByteBufCodecs.STRING_UTF8.encode(buf, effect.effectType().getId());
        }
    };

    // ============================================
    // ファクトリメソッド
    // ============================================

    /**
     * 加算効果を作成
     *
     * @param statId ステータスID
     * @param value 加算値
     * @return 加算効果
     */
    public static ModEffect additive(String statId, float value) {
        return new ModEffect(statId, value, EffectType.ADDITIVE);
    }

    /**
     * 乗算効果を作成（パーセント指定）
     *
     * @param statId ステータスID
     * @param percent パーセント値（例: 10で+10%）
     * @return 乗算効果
     */
    public static ModEffect multiplicative(String statId, float percent) {
        return new ModEffect(statId, percent / 100f, EffectType.MULTIPLICATIVE);
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * ランクに応じた効果値を計算
     *
     * @param rank 現在のランク（0〜maxRank）
     * @param maxRank 最大ランク
     * @return スケールされた効果値
     */
    public float getScaledValue(int rank, int maxRank) {
        if (maxRank <= 0) {
            return value;
        }
        // ランク0でも基本効果の一部を得られるようにする
        float baseRatio = 0.2f; // 20%は基本効果
        float scaledRatio = baseRatio + (1.0f - baseRatio) * ((float) rank / maxRank);
        return value * scaledRatio;
    }

    /**
     * 効果がボーナスかどうか
     */
    public boolean isBonus() {
        return value > 0;
    }

    /**
     * 効果がデメリットかどうか
     */
    public boolean isDrawback() {
        return value < 0;
    }

    /**
     * 表示用フォーマット文字列を取得
     */
    public String getDisplayString() {
        String prefix = value >= 0 ? "+" : "";
        if (effectType == EffectType.MULTIPLICATIVE) {
            return String.format("%s%.0f%%", prefix, value * 100);
        }
        return String.format("%s%.1f", prefix, value);
    }
}
