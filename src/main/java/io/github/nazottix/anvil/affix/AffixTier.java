package io.github.nazottix.anvil.affix;

/**
 * アフィックスティア列挙型
 *
 * アフィックスの強さの段階を定義します。
 * T1が最弱、T10が最強となります。
 *
 * 仕様書参照: docs/06_レアリティシステム.md - 8.3 アフィックスシステム
 */
public enum AffixTier {
    T1(1, 0, 1000, 0.30f),
    T2(2, 500, 2000, 0.25f),
    T3(3, 1000, 3000, 0.18f),
    T4(4, 2000, 4500, 0.12f),
    T5(5, 3000, 6000, 0.08f),
    T6(6, 4500, 7000, 0.04f),
    T7(7, 6000, 8000, 0.015f),
    T8(8, 7000, 8500, 0.008f),
    T9(9, 8000, 9500, 0.004f),
    T10(10, 9000, 10000, 0.002f);

    private final int tier;
    private final int minLevel;
    private final int maxLevel;
    private final float dropWeight;

    AffixTier(int tier, int minLevel, int maxLevel, float dropWeight) {
        this.tier = tier;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.dropWeight = dropWeight;
    }

    /**
     * ティア番号を取得
     */
    public int getTier() {
        return tier;
    }

    /**
     * このティアが出現する最小レベルを取得
     */
    public int getMinLevel() {
        return minLevel;
    }

    /**
     * このティアが出現する最大レベルを取得
     */
    public int getMaxLevel() {
        return maxLevel;
    }

    /**
     * ドロップ重み（確率用）を取得
     */
    public float getDropWeight() {
        return dropWeight;
    }

    /**
     * 指定レベルで出現可能かチェック
     *
     * @param level ツールレベル
     * @return 出現可能な場合true
     */
    public boolean canAppearAtLevel(int level) {
        return level >= minLevel && level <= maxLevel;
    }

    /**
     * ティア番号からAffixTierを取得
     *
     * @param tier ティア番号（1-10）
     * @return 対応するAffixTier、見つからない場合はT1
     */
    public static AffixTier fromTier(int tier) {
        for (AffixTier t : values()) {
            if (t.tier == tier) {
                return t;
            }
        }
        return T1;
    }

    /**
     * 表示名を取得
     */
    public String getDisplayName() {
        return "Tier " + tier;
    }

    /**
     * ローカライズキーを取得
     */
    public String getTranslationKey() {
        return "affix.anvil.tier." + tier;
    }


    /**
     * ティアに応じた色を取得
     * 高ティアほど豪華な色になる
     *
     * @return RGB色コード
     */
    public int getColor() {
        return switch (this) {
            case T1 -> 0xAAAAAA;   // グレー
            case T2 -> 0xFFFFFF;   // 白
            case T3 -> 0x55FF55;   // 緑
            case T4 -> 0x5555FF;   // 青
            case T5 -> 0xFFFF55;   // 黄
            case T6 -> 0xFF5555;   // 赤
            case T7 -> 0xFFAA00;   // オレンジ
            case T8 -> 0xAA00FF;   // 紫
            case T9 -> 0x55FFFF;   // シアン
            case T10 -> 0xFF55FF;  // マゼンタ（最高ティア）
        };
    }
}
