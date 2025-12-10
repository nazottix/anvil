package io.github.nazottix.anvil.trait;

/**
 * 特性カテゴリ列挙型
 *
 * 特性の分類を定義します。カテゴリによって発動条件や効果のタイプが異なります。
 *
 * 仕様書参照: docs/01_パーツ_素材システム.md - 3.6.1 特性カテゴリ
 */
public enum TraitCategory {
    /**
     * 戦闘系 - 攻撃時に発動
     * 例: 出血、吸血、クリティカル
     */
    COMBAT("combat", "戦闘系", "Combat"),

    /**
     * 採掘系 - ブロック破壊時に発動
     * 例: 範囲採掘、自動精錬、幸運
     */
    MINING("mining", "採掘系", "Mining"),

    /**
     * 防御系 - ダメージを受けた時に発動
     * 例: 反射、再生、根性
     */
    DEFENSE("defense", "防御系", "Defense"),

    /**
     * ユーティリティ - 常時または特定条件で発動
     * 例: 磁力、発光、修復
     */
    UTILITY("utility", "ユーティリティ", "Utility"),

    /**
     * 環境適応 - 特定環境で発動
     * 例: 水中速度、溶岩耐性
     */
    ENVIRONMENTAL("environmental", "環境適応", "Environmental"),

    /**
     * シナジー - 特定の組み合わせで発動
     * 例: 相乗効果
     */
    SYNERGY("synergy", "シナジー", "Synergy");

    // カテゴリID
    private final String id;

    // 日本語表示名
    private final String displayNameJa;

    // 英語表示名
    private final String displayNameEn;

    TraitCategory(String id, String displayNameJa, String displayNameEn) {
        this.id = id;
        this.displayNameJa = displayNameJa;
        this.displayNameEn = displayNameEn;
    }

    public String getId() {
        return id;
    }

    public String getDisplayNameJa() {
        return displayNameJa;
    }

    public String getDisplayNameEn() {
        return displayNameEn;
    }

    /**
     * ローカライズキーを取得
     */
    public String getTranslationKey() {
        return "trait_category.anvil." + id;
    }

    /**
     * IDからTraitCategoryを取得
     */
    public static TraitCategory fromId(String id) {
        for (TraitCategory category : values()) {
            if (category.id.equals(id)) {
                return category;
            }
        }
        return null;
    }
}
