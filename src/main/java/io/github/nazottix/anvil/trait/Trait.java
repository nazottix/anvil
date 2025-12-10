package io.github.nazottix.anvil.trait;

import net.minecraft.resources.ResourceLocation;

/**
 * 特性データクラス
 *
 * ツールに付与される特性を定義します。
 * 各特性はカテゴリ、トリガー、最大レベル、レベルスケーリングを持ちます。
 *
 * 仕様書参照: docs/01_パーツ_素材システム.md - 3.6 特性（Trait）システム
 */
public class Trait {

    // 特性ID（例: "anvil:magnetic"）
    private final ResourceLocation id;

    // ローカライズキー
    private final String translationKey;

    // 特性カテゴリ
    private final TraitCategory category;

    // 発動トリガー
    private final TraitTrigger trigger;

    // 最大レベル
    private final int maxLevel;

    // レベルあたりの効果量（基本値）
    private final float effectPerLevel;

    // 効果の単位（表示用）
    private final String effectUnit;

    // 効果の説明キー
    private final String descriptionKey;

    // 表示色（16進数RGB）
    private final int color;

    // レアリティ（高いほど希少）
    private final int rarity;

    // 他の特性との相互排他リスト（IDのリスト）
    private final ResourceLocation[] incompatibleTraits;

    /**
     * Traitコンストラクタ（ビルダー経由で生成推奨）
     */
    private Trait(Builder builder) {
        this.id = builder.id;
        this.translationKey = builder.translationKey;
        this.category = builder.category;
        this.trigger = builder.trigger;
        this.maxLevel = builder.maxLevel;
        this.effectPerLevel = builder.effectPerLevel;
        this.effectUnit = builder.effectUnit;
        this.descriptionKey = builder.descriptionKey;
        this.color = builder.color;
        this.rarity = builder.rarity;
        this.incompatibleTraits = builder.incompatibleTraits;
    }

    // ============================================
    // ゲッター
    // ============================================

    public ResourceLocation getId() {
        return id;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public TraitCategory getCategory() {
        return category;
    }

    public TraitTrigger getTrigger() {
        return trigger;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public float getEffectPerLevel() {
        return effectPerLevel;
    }

    public String getEffectUnit() {
        return effectUnit;
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }

    public int getColor() {
        return color;
    }

    public int getRarity() {
        return rarity;
    }

    public ResourceLocation[] getIncompatibleTraits() {
        return incompatibleTraits;
    }

    // ============================================
    // ユーティリティメソッド
    // ============================================

    /**
     * 指定レベルでの効果量を計算
     *
     * @param level 特性レベル
     * @return 効果量
     */
    public float getEffectAtLevel(int level) {
        return effectPerLevel * Math.min(level, maxLevel);
    }

    /**
     * 指定した特性と互換性がないかチェック
     *
     * @param other 他の特性
     * @return 互換性がない場合true
     */
    public boolean isIncompatibleWith(Trait other) {
        for (ResourceLocation incompatible : incompatibleTraits) {
            if (incompatible.equals(other.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 説明文のローカライズキーを取得
     */
    public String getDescriptionTranslationKey() {
        return descriptionKey != null ? descriptionKey : translationKey + ".desc";
    }

    // ============================================
    // ビルダー
    // ============================================

    /**
     * 新しいビルダーを作成
     *
     * @param id 特性ID
     * @return ビルダーインスタンス
     */
    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    /**
     * 特性ビルダー
     */
    public static class Builder {
        private final ResourceLocation id;
        private String translationKey;
        private TraitCategory category = TraitCategory.UTILITY;
        private TraitTrigger trigger = TraitTrigger.ALWAYS;
        private int maxLevel = 5;
        private float effectPerLevel = 1.0f;
        private String effectUnit = "";
        private String descriptionKey;
        private int color = 0xFFFFFF;
        private int rarity = 1;
        private ResourceLocation[] incompatibleTraits = new ResourceLocation[0];

        private Builder(ResourceLocation id) {
            this.id = id;
            this.translationKey = "trait." + id.getNamespace() + "." + id.getPath();
            this.descriptionKey = this.translationKey + ".desc";
        }

        public Builder translationKey(String key) {
            this.translationKey = key;
            this.descriptionKey = key + ".desc";
            return this;
        }

        public Builder category(TraitCategory category) {
            this.category = category;
            return this;
        }

        public Builder trigger(TraitTrigger trigger) {
            this.trigger = trigger;
            return this;
        }

        public Builder maxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
            return this;
        }

        public Builder effectPerLevel(float effectPerLevel) {
            this.effectPerLevel = effectPerLevel;
            return this;
        }

        public Builder effectUnit(String unit) {
            this.effectUnit = unit;
            return this;
        }

        public Builder color(int color) {
            this.color = color;
            return this;
        }

        public Builder rarity(int rarity) {
            this.rarity = rarity;
            return this;
        }

        public Builder incompatibleWith(ResourceLocation... traits) {
            this.incompatibleTraits = traits;
            return this;
        }

        public Trait build() {
            return new Trait(this);
        }
    }
}
