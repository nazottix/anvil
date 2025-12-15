package io.github.nazottix.anvil.skill.jewel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.nazottix.anvil.skill.SkillEffect;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * ジュエルデータ
 *
 * スキルツリーのジュエルソケットに装着可能なジュエルの定義。
 * 各ジュエルは複数のスキル効果を持ち、装着時にツールに適用されます。
 *
 * 仕様書参照: docs/03_スキルツリーシステム.md
 */
public record JewelData(
        // ジュエルID
        ResourceLocation id,
        // 表示名キー
        String nameKey,
        // レアリティ
        JewelRarity rarity,
        // 効果リスト
        List<SkillEffect> effects,
        // 装着可能なツールタイプ（空=全て）
        List<String> allowedToolTypes,
        // 半径効果（周囲のノードにも効果を与える）
        int radiusEffect,
        // アイコンパス（オプション）
        String iconPath
) {

    // ============================================
    // レアリティ
    // ============================================

    /**
     * ジュエルのレアリティ
     */
    public enum JewelRarity {
        /** 通常 - 基本的な効果 */
        NORMAL("normal", 0xAAAAAA, 1.0f),
        /** マジック - やや強化された効果 */
        MAGIC("magic", 0x5555FF, 1.25f),
        /** レア - 強力な効果 */
        RARE("rare", 0xFFFF55, 1.5f),
        /** ユニーク - 特殊な効果を持つ */
        UNIQUE("unique", 0xFFAA00, 2.0f),
        /** プライムル - 最高級（半径効果付き） */
        PRIMORDIAL("primordial", 0xFF55FF, 2.5f);

        private final String id;
        private final int color;
        private final float effectMultiplier;

        JewelRarity(String id, int color, float effectMultiplier) {
            this.id = id;
            this.color = color;
            this.effectMultiplier = effectMultiplier;
        }

        public String getId() {
            return id;
        }

        public int getColor() {
            return color;
        }

        public float getEffectMultiplier() {
            return effectMultiplier;
        }

        public static JewelRarity fromId(String id) {
            for (JewelRarity rarity : values()) {
                if (rarity.id.equals(id)) {
                    return rarity;
                }
            }
            return NORMAL;
        }
    }

    // ============================================
    // Codec
    // ============================================

    /**
     * 保存/読み込み用Codec
     */
    public static final Codec<JewelData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(JewelData::id),
                    Codec.STRING.fieldOf("name_key").forGetter(JewelData::nameKey),
                    Codec.STRING.xmap(JewelRarity::fromId, JewelRarity::getId)
                            .optionalFieldOf("rarity", JewelRarity.NORMAL).forGetter(JewelData::rarity),
                    SkillEffect.CODEC.listOf().fieldOf("effects").forGetter(JewelData::effects),
                    Codec.STRING.listOf().optionalFieldOf("allowed_tool_types", List.of())
                            .forGetter(JewelData::allowedToolTypes),
                    Codec.INT.optionalFieldOf("radius_effect", 0).forGetter(JewelData::radiusEffect),
                    Codec.STRING.optionalFieldOf("icon_path", "").forGetter(JewelData::iconPath)
            ).apply(instance, JewelData::new)
    );

    /**
     * ネットワーク通信用StreamCodec
     */
    public static final StreamCodec<ByteBuf, JewelData> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public JewelData decode(ByteBuf buf) {
            ResourceLocation id = ResourceLocation.parse(ByteBufCodecs.STRING_UTF8.decode(buf));
            String nameKey = ByteBufCodecs.STRING_UTF8.decode(buf);
            JewelRarity rarity = JewelRarity.fromId(ByteBufCodecs.STRING_UTF8.decode(buf));

            int effectCount = ByteBufCodecs.VAR_INT.decode(buf);
            List<SkillEffect> effects = new ArrayList<>(effectCount);
            for (int i = 0; i < effectCount; i++) {
                effects.add(SkillEffect.STREAM_CODEC.decode(buf));
            }

            int toolTypeCount = ByteBufCodecs.VAR_INT.decode(buf);
            List<String> allowedToolTypes = new ArrayList<>(toolTypeCount);
            for (int i = 0; i < toolTypeCount; i++) {
                allowedToolTypes.add(ByteBufCodecs.STRING_UTF8.decode(buf));
            }

            int radiusEffect = ByteBufCodecs.VAR_INT.decode(buf);
            String iconPath = ByteBufCodecs.STRING_UTF8.decode(buf);

            return new JewelData(id, nameKey, rarity, effects, allowedToolTypes, radiusEffect, iconPath);
        }

        @Override
        public void encode(ByteBuf buf, JewelData data) {
            ByteBufCodecs.STRING_UTF8.encode(buf, data.id().toString());
            ByteBufCodecs.STRING_UTF8.encode(buf, data.nameKey());
            ByteBufCodecs.STRING_UTF8.encode(buf, data.rarity().getId());

            ByteBufCodecs.VAR_INT.encode(buf, data.effects().size());
            for (SkillEffect effect : data.effects()) {
                SkillEffect.STREAM_CODEC.encode(buf, effect);
            }

            ByteBufCodecs.VAR_INT.encode(buf, data.allowedToolTypes().size());
            for (String toolType : data.allowedToolTypes()) {
                ByteBufCodecs.STRING_UTF8.encode(buf, toolType);
            }

            ByteBufCodecs.VAR_INT.encode(buf, data.radiusEffect());
            ByteBufCodecs.STRING_UTF8.encode(buf, data.iconPath());
        }
    };

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * 指定したツールタイプに装着可能か
     */
    public boolean canEquipTo(String toolType) {
        return allowedToolTypes.isEmpty() || allowedToolTypes.contains(toolType);
    }

    /**
     * レアリティ倍率を適用した効果を取得
     */
    public List<SkillEffect> getScaledEffects() {
        float multiplier = rarity.getEffectMultiplier();
        if (multiplier == 1.0f) {
            return effects;
        }

        return effects.stream()
                .map(e -> new SkillEffect(e.type(), e.targetId(), e.value() * multiplier, e.operation()))
                .toList();
    }

    /**
     * 半径効果を持つか
     */
    public boolean hasRadiusEffect() {
        return radiusEffect > 0;
    }
}
