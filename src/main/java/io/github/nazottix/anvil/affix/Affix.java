package io.github.nazottix.anvil.affix;

import java.util.Map;

/**
 * アフィックス定義クラス
 *
 * ツールに付与されるアフィックス（接辞）を定義します。
 * アフィックスは名前と効果の両方を持ちます。
 *
 * 仕様書参照: docs/06_レアリティシステム.md - 8.3 アフィックスシステム
 */
public class Affix {

    private final String id;
    private final AffixType type;
    private final String nameKey;
    private final Map<AffixTier, AffixEffect> tierEffects;
    private final int minRequiredLevel;

    /**
     * コンストラクタ
     *
     * @param id アフィックスID
     * @param type アフィックスタイプ（PREFIX/SUFFIX）
     * @param nameKey ローカライズキー
     * @param tierEffects ティアごとの効果マップ
     * @param minRequiredLevel 最低要求レベル
     */
    public Affix(String id, AffixType type, String nameKey,
                 Map<AffixTier, AffixEffect> tierEffects, int minRequiredLevel) {
        this.id = id;
        this.type = type;
        this.nameKey = nameKey;
        this.tierEffects = tierEffects;
        this.minRequiredLevel = minRequiredLevel;
    }

    // ============================================
    // ゲッター
    // ============================================

    public String getId() {
        return id;
    }

    public AffixType getType() {
        return type;
    }

    public String getNameKey() {
        return nameKey;
    }

    public Map<AffixTier, AffixEffect> getTierEffects() {
        return tierEffects;
    }

    public int getMinRequiredLevel() {
        return minRequiredLevel;
    }

    /**
     * 指定ティアの効果を取得
     *
     * @param tier ティア
     * @return 効果、存在しない場合はnull
     */
    public AffixEffect getEffect(AffixTier tier) {
        return tierEffects.get(tier);
    }

    /**
     * 指定レベルで出現可能かチェック
     *
     * @param level ツールレベル
     * @return 出現可能な場合true
     */
    public boolean canAppearAtLevel(int level) {
        return level >= minRequiredLevel;
    }

    /**
     * 指定レベルで出現可能な最高ティアを取得
     *
     * @param level ツールレベル
     * @return 最高ティア、出現不可の場合はnull
     */
    public AffixTier getHighestTierForLevel(int level) {
        if (!canAppearAtLevel(level)) {
            return null;
        }

        AffixTier highest = null;
        for (AffixTier tier : tierEffects.keySet()) {
            if (tier.canAppearAtLevel(level)) {
                if (highest == null || tier.getTier() > highest.getTier()) {
                    highest = tier;
                }
            }
        }
        return highest;
    }

    /**
     * ローカライズキーを取得
     */
    public String getTranslationKey() {
        return "affix.anvil." + id;
    }

    // ============================================
    // ビルダー
    // ============================================

    /**
     * ビルダーを作成
     */
    public static Builder builder(String id, AffixType type) {
        return new Builder(id, type);
    }

    /**
     * アフィックスビルダー
     */
    public static class Builder {
        private final String id;
        private final AffixType type;
        private String nameKey;
        private final java.util.HashMap<AffixTier, AffixEffect> tierEffects = new java.util.HashMap<>();
        private int minRequiredLevel = 1;

        private Builder(String id, AffixType type) {
            this.id = id;
            this.type = type;
            this.nameKey = "affix.anvil." + id;
        }

        /**
         * ローカライズキーを設定
         */
        public Builder nameKey(String nameKey) {
            this.nameKey = nameKey;
            return this;
        }

        /**
         * ティア効果を追加
         */
        public Builder addTier(AffixTier tier, AffixEffect effect) {
            this.tierEffects.put(tier, effect);
            return this;
        }

        /**
         * 全ティア（T1-T10）に効果を追加（線形スケーリング）
         *
         * @param statId ステータスID
         * @param minValue T1の値
         * @param maxValue T10の値
         * @param isMultiplier 乗算効果かどうか
         */
        public Builder allTiers(String statId, float minValue, float maxValue, boolean isMultiplier) {
            float range = maxValue - minValue;
            for (AffixTier tier : AffixTier.values()) {
                float value = minValue + (range * (tier.getTier() - 1) / 9.0f);
                this.tierEffects.put(tier, new AffixEffect(statId, value, isMultiplier));
            }
            return this;
        }

        /**
         * 特定範囲のティアに効果を追加
         *
         * @param fromTier 開始ティア
         * @param toTier 終了ティア
         * @param statId ステータスID
         * @param minValue 開始ティアの値
         * @param maxValue 終了ティアの値
         * @param isMultiplier 乗算効果かどうか
         */
        public Builder tierRange(int fromTier, int toTier, String statId,
                                 float minValue, float maxValue, boolean isMultiplier) {
            float range = maxValue - minValue;
            int tierCount = toTier - fromTier;
            for (int t = fromTier; t <= toTier; t++) {
                AffixTier tier = AffixTier.fromTier(t);
                float value = minValue + (tierCount > 0 ? range * (t - fromTier) / tierCount : 0);
                this.tierEffects.put(tier, new AffixEffect(statId, value, isMultiplier));
            }
            return this;
        }

        /**
         * 最低要求レベルを設定
         */
        public Builder minLevel(int level) {
            this.minRequiredLevel = level;
            return this;
        }

        /**
         * アフィックスを構築
         */
        public Affix build() {
            return new Affix(id, type, nameKey, Map.copyOf(tierEffects), minRequiredLevel);
        }
    }
}
