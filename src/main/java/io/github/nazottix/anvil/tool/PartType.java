package io.github.nazottix.anvil.tool;

import java.util.Set;

/**
 * ANVILパーツタイプ列挙型
 *
 * ツールを構成するパーツの種類を定義します。
 * 各パーツタイプは影響するステータスと適用可能なツールカテゴリを持ちます。
 *
 * 仕様書参照: docs/01_パーツ_素材システム.md - 3.2.2 パーツタイプ定義
 */
public enum PartType {
    // ============================================
    // 基本パーツ（ほぼ全ツールで使用）
    // ============================================

    /**
     * ヘッド - ツールの主要部分
     * 影響ステータス: 攻撃力、採掘速度、採掘レベル
     * 適用: ピッケル、斧、シャベル、クワ
     */
    HEAD(
            "head",
            "ヘッド",
            Set.of(StatType.ATTACK_DAMAGE, StatType.MINING_SPEED, StatType.MINING_LEVEL),
            Set.of(ToolType.ToolCategory.MINING, ToolType.ToolCategory.FARMING)
    ),

    /**
     * ハンドル - ツールの柄
     * 影響ステータス: 耐久値倍率、攻撃速度
     * 適用: 全ツール
     */
    HANDLE(
            "handle",
            "ハンドル",
            Set.of(StatType.DURABILITY_MULTIPLIER, StatType.ATTACK_SPEED),
            Set.of(ToolType.ToolCategory.MINING, ToolType.ToolCategory.COMBAT,
                    ToolType.ToolCategory.FARMING, ToolType.ToolCategory.FISHING,
                    ToolType.ToolCategory.UTILITY)
    ),

    /**
     * バインディング - パーツを繋ぐ部品
     * 影響ステータス: 特性増幅（ステータス直接影響なし）
     * 適用: 採掘ツール
     */
    BINDING(
            "binding",
            "バインディング",
            Set.of(StatType.TRAIT_AMPLIFIER),
            Set.of(ToolType.ToolCategory.MINING)
    ),

    // ============================================
    // 剣用パーツ
    // ============================================

    /**
     * ブレード - 刃部分
     * 影響ステータス: 攻撃力、切断効率
     * 適用: 剣、ハサミ
     */
    BLADE(
            "blade",
            "ブレード",
            Set.of(StatType.ATTACK_DAMAGE, StatType.CUTTING_EFFICIENCY),
            Set.of(ToolType.ToolCategory.COMBAT, ToolType.ToolCategory.UTILITY)
    ),

    /**
     * ガード - 鍔（つば）部分
     * 影響ステータス: 特性付与
     * 適用: 剣
     */
    GUARD(
            "guard",
            "ガード",
            Set.of(StatType.TRAIT_AMPLIFIER),
            Set.of(ToolType.ToolCategory.COMBAT)
    ),

    // ============================================
    // 弓用パーツ
    // ============================================

    /**
     * ボウリム - 弓の腕部分
     * 影響ステータス: 引き速度、射程
     * 適用: 弓
     */
    BOW_LIMB(
            "bow_limb",
            "ボウリム",
            Set.of(StatType.DRAW_SPEED, StatType.RANGE),
            Set.of(ToolType.ToolCategory.COMBAT)
    ),

    /**
     * ボウストリング - 弦
     * 影響ステータス: 矢速度、精度
     * 適用: 弓
     */
    BOWSTRING(
            "bowstring",
            "ボウストリング",
            Set.of(StatType.ARROW_SPEED, StatType.ACCURACY),
            Set.of(ToolType.ToolCategory.COMBAT)
    ),

    // ============================================
    // 釣り竿用パーツ
    // ============================================

    /**
     * ロッド - 釣り竿の竿部分
     * 影響ステータス: 耐久値
     * 適用: 釣り竿
     */
    ROD(
            "rod",
            "ロッド",
            Set.of(StatType.DURABILITY_MULTIPLIER),
            Set.of(ToolType.ToolCategory.FISHING)
    ),

    /**
     * フック - 釣り針
     * 影響ステータス: 釣り効率、レアドロップ率
     * 適用: 釣り竿
     */
    HOOK(
            "hook",
            "フック",
            Set.of(StatType.FISHING_EFFICIENCY, StatType.RARE_DROP_CHANCE),
            Set.of(ToolType.ToolCategory.FISHING)
    ),

    /**
     * ライン - 釣り糸
     * 影響ステータス: 耐久値
     * 適用: 釣り竿
     */
    LINE(
            "line",
            "ライン",
            Set.of(StatType.DURABILITY_MULTIPLIER),
            Set.of(ToolType.ToolCategory.FISHING)
    ),

    // ============================================
    // ハサミ用パーツ
    // ============================================

    /**
     * ピボット - ハサミの軸
     * 影響ステータス: 耐久値
     * 適用: ハサミ
     */
    PIVOT(
            "pivot",
            "ピボット",
            Set.of(StatType.DURABILITY_MULTIPLIER),
            Set.of(ToolType.ToolCategory.UTILITY)
    ),

    // ============================================
    // 追加パーツ（オプション）
    // ============================================

    /**
     * コーティング - 全体コーティング
     * 影響ステータス: 全ステータス強化
     * 適用: 全ツール
     */
    COATING(
            "coating",
            "コーティング",
            Set.of(StatType.ALL_STATS_MULTIPLIER),
            Set.of(ToolType.ToolCategory.MINING, ToolType.ToolCategory.COMBAT,
                    ToolType.ToolCategory.FARMING, ToolType.ToolCategory.FISHING,
                    ToolType.ToolCategory.UTILITY)
    ),

    /**
     * アップグレード - 追加効果スロット
     * 影響ステータス: MODスロット追加
     * 適用: 全ツール
     */
    UPGRADE(
            "upgrade",
            "アップグレード",
            Set.of(StatType.MOD_SLOT_BONUS),
            Set.of(ToolType.ToolCategory.MINING, ToolType.ToolCategory.COMBAT,
                    ToolType.ToolCategory.FARMING, ToolType.ToolCategory.FISHING,
                    ToolType.ToolCategory.UTILITY)
    );

    // パーツのID
    private final String id;

    // 日本語表示名
    private final String displayNameJa;

    // このパーツが影響するステータス
    private final Set<StatType> affectedStats;

    // このパーツが適用可能なツールカテゴリ
    private final Set<ToolType.ToolCategory> applicableCategories;

    /**
     * PartTypeコンストラクタ
     *
     * @param id パーツID
     * @param displayNameJa 日本語表示名
     * @param affectedStats 影響するステータス
     * @param applicableCategories 適用可能なカテゴリ
     */
    PartType(String id, String displayNameJa, Set<StatType> affectedStats,
             Set<ToolType.ToolCategory> applicableCategories) {
        this.id = id;
        this.displayNameJa = displayNameJa;
        this.affectedStats = affectedStats;
        this.applicableCategories = applicableCategories;
    }

    public String getId() {
        return id;
    }

    public String getDisplayNameJa() {
        return displayNameJa;
    }

    public Set<StatType> getAffectedStats() {
        return affectedStats;
    }

    public Set<ToolType.ToolCategory> getApplicableCategories() {
        return applicableCategories;
    }

    /**
     * ローカライズキーを取得
     * 例: "part.anvil.head"
     */
    public String getTranslationKey() {
        return "part.anvil." + id;
    }

    /**
     * 指定カテゴリにこのパーツが適用可能かチェック
     *
     * @param category ツールカテゴリ
     * @return 適用可能な場合true
     */
    public boolean isApplicableTo(ToolType.ToolCategory category) {
        return applicableCategories.contains(category);
    }

    /**
     * IDからPartTypeを取得
     *
     * @param id パーツID
     * @return 対応するPartType、見つからない場合はnull
     */
    public static PartType fromId(String id) {
        for (PartType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }

    /**
     * ステータスタイプ
     *
     * パーツが影響を与えるステータスの種類
     */
    public enum StatType {
        // 基本ステータス
        ATTACK_DAMAGE("attack_damage", "攻撃力"),
        MINING_SPEED("mining_speed", "採掘速度"),
        MINING_LEVEL("mining_level", "採掘レベル"),
        DURABILITY_MULTIPLIER("durability_multiplier", "耐久値倍率"),
        ATTACK_SPEED("attack_speed", "攻撃速度"),

        // 特殊ステータス
        TRAIT_AMPLIFIER("trait_amplifier", "特性増幅"),
        CUTTING_EFFICIENCY("cutting_efficiency", "切断効率"),

        // 弓関連
        DRAW_SPEED("draw_speed", "引き速度"),
        RANGE("range", "射程"),
        ARROW_SPEED("arrow_speed", "矢速度"),
        ACCURACY("accuracy", "精度"),

        // 釣り関連
        FISHING_EFFICIENCY("fishing_efficiency", "釣り効率"),
        RARE_DROP_CHANCE("rare_drop_chance", "レアドロップ率"),

        // 全体修飾
        ALL_STATS_MULTIPLIER("all_stats_multiplier", "全ステータス倍率"),
        MOD_SLOT_BONUS("mod_slot_bonus", "MODスロットボーナス");

        private final String id;
        private final String displayNameJa;

        StatType(String id, String displayNameJa) {
            this.id = id;
            this.displayNameJa = displayNameJa;
        }

        public String getId() {
            return id;
        }

        public String getDisplayNameJa() {
            return displayNameJa;
        }
    }
}
