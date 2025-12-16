package io.github.nazottix.anvil;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * ANVIL設定クラス
 *
 * anvil-common.tomlに保存される設定項目を定義します。
 * 設定はゲーム内のMods画面 > ANVIL > 設定からも変更可能です。
 *
 * 設定カテゴリ:
 * - leveling: レベリングシステム関連
 * - capacity: 容量システム関連（Phase 2で有効化）
 * - grid: グリッドシステム関連（Phase 2で有効化）
 * - rarity: レアリティシステム関連（Phase 3で有効化）
 * - balance: バランス調整関連
 */
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // ============================================
    // レベリング設定 (Leveling)
    // ============================================

    static {
        BUILDER.comment("レベリングシステムの設定").push("leveling");
    }

    /**
     * 最大レベル
     * ツールが到達できる最高レベル
     */
    public static final ModConfigSpec.IntValue MAX_LEVEL = BUILDER
            .comment("ツールの最大レベル (1-10000)")
            .translation("config.anvil.leveling.max_level")
            .defineInRange("maxLevel", 10000, 1, 10000);

    /**
     * XP倍率
     * 獲得経験値に掛けられる倍率
     */
    public static final ModConfigSpec.DoubleValue XP_MULTIPLIER = BUILDER
            .comment("経験値獲得倍率 (0.1-10.0)")
            .translation("config.anvil.leveling.xp_multiplier")
            .defineInRange("xpMultiplier", 1.0, 0.1, 10.0);

    /**
     * マイルストーン報酬の有効化
     * 特定レベル到達時に報酬を与えるかどうか
     */
    public static final ModConfigSpec.BooleanValue MILESTONE_REWARDS = BUILDER
            .comment("マイルストーンレベル(100, 500, 1000等)で報酬を有効にするか")
            .translation("config.anvil.leveling.milestone_rewards")
            .define("milestoneRewards", true);

    static {
        BUILDER.pop();
    }

    // ============================================
    // 容量設定 (Capacity) - Phase 2で有効化
    // ============================================

    static {
        BUILDER.comment("容量システムの設定 (Phase 2で有効化予定)").push("capacity");
    }

    /**
     * ピッケルの基本容量
     */
    public static final ModConfigSpec.IntValue BASE_CAPACITY_PICKAXE = BUILDER
            .comment("ピッケルの基本MOD容量")
            .translation("config.anvil.capacity.base_pickaxe")
            .defineInRange("baseCapacityPickaxe", 30, 10, 100);

    /**
     * 剣の基本容量
     */
    public static final ModConfigSpec.IntValue BASE_CAPACITY_SWORD = BUILDER
            .comment("剣の基本MOD容量")
            .translation("config.anvil.capacity.base_sword")
            .defineInRange("baseCapacitySword", 32, 10, 100);

    /**
     * 最大MODスロット数
     */
    public static final ModConfigSpec.IntValue MAX_MOD_SLOTS = BUILDER
            .comment("ツールあたりの最大MODスロット数")
            .translation("config.anvil.capacity.max_mod_slots")
            .defineInRange("maxModSlots", 8, 1, 16);

    /**
     * オーラシステムの有効化
     */
    public static final ModConfigSpec.BooleanValue ENABLE_AURA_SYSTEM = BUILDER
            .comment("オーラMODシステムを有効にするか")
            .translation("config.anvil.capacity.enable_aura")
            .define("enableAuraSystem", true);

    static {
        BUILDER.pop();
    }

    // ============================================
    // グリッド設定 (Grid) - Phase 2で有効化
    // ============================================

    static {
        BUILDER.comment("グリッドモジュールシステムの設定 (Phase 2で有効化予定)").push("grid");
    }

    /**
     * 重量システムの有効化
     */
    public static final ModConfigSpec.BooleanValue ENABLE_WEIGHT_SYSTEM = BUILDER
            .comment("グリッドモジュールの重量システムを有効にするか")
            .translation("config.anvil.grid.enable_weight")
            .define("enableWeightSystem", true);

    /**
     * 最大グリッドサイズ
     */
    public static final ModConfigSpec.IntValue MAX_GRID_SIZE = BUILDER
            .comment("グリッドの最大サイズ (NxN)")
            .translation("config.anvil.grid.max_size")
            .defineInRange("maxGridSize", 8, 4, 12);

    static {
        BUILDER.pop();
    }

    // ============================================
    // レアリティ設定 (Rarity) - Phase 3で有効化
    // ============================================

    static {
        BUILDER.comment("レアリティシステムの設定 (Phase 3で有効化予定)").push("rarity");
    }

    /**
     * Legacyティアの有効化
     */
    public static final ModConfigSpec.BooleanValue ENABLE_LEGACY_TIER = BUILDER
            .comment("最高レアリティ「Legacy」を有効にするか")
            .translation("config.anvil.rarity.enable_legacy")
            .define("enableLegacyTier", true);

    /**
     * ユニークドロップ倍率
     */
    public static final ModConfigSpec.DoubleValue UNIQUE_DROP_MULTIPLIER = BUILDER
            .comment("ユニークアイテムのドロップ率倍率 (0.1-5.0)")
            .translation("config.anvil.rarity.unique_drop_multiplier")
            .defineInRange("uniqueDropMultiplier", 1.0, 0.1, 5.0);

    static {
        BUILDER.pop();
    }

    // ============================================
    // ジュエルドロップ設定 (Jewel Drop) - Phase 3で有効化
    // ============================================

    static {
        BUILDER.comment("ジュエルドロップシステムの設定 (Phase 3で有効化)").push("jewel_drop");
    }

    /**
     * ジュエルドロップシステムの有効化
     */
    public static final ModConfigSpec.BooleanValue ENABLE_JEWEL_DROP = BUILDER
            .comment("ジュエルドロップシステムを有効にするか")
            .translation("config.anvil.jewel_drop.enable")
            .define("enableJewelDrop", true);

    /**
     * 通常モブのジュエルドロップ確率（%）
     */
    public static final ModConfigSpec.DoubleValue JEWEL_DROP_CHANCE_NORMAL = BUILDER
            .comment("通常モブからのジュエルドロップ確率 (0.0-100.0)")
            .translation("config.anvil.jewel_drop.chance_normal")
            .defineInRange("dropChanceNormal", 1.0, 0.0, 100.0);

    /**
     * 強敵モブのジュエルドロップ確率（%）
     */
    public static final ModConfigSpec.DoubleValue JEWEL_DROP_CHANCE_STRONG = BUILDER
            .comment("強敵モブ（エンダーマン、ウィッチ等）からのドロップ確率 (0.0-100.0)")
            .translation("config.anvil.jewel_drop.chance_strong")
            .defineInRange("dropChanceStrong", 3.0, 0.0, 100.0);

    /**
     * ボスモブのジュエルドロップ確率（%）
     */
    public static final ModConfigSpec.DoubleValue JEWEL_DROP_CHANCE_BOSS = BUILDER
            .comment("ボスモブ（ドラゴン、ウィザー等）からのドロップ確率 (0.0-100.0)")
            .translation("config.anvil.jewel_drop.chance_boss")
            .defineInRange("dropChanceBoss", 50.0, 0.0, 100.0);

    /**
     * 釣りでのジュエルドロップ確率（%）
     */
    public static final ModConfigSpec.DoubleValue JEWEL_DROP_CHANCE_FISHING = BUILDER
            .comment("釣りでのジュエルドロップ確率 (0.0-100.0)")
            .translation("config.anvil.jewel_drop.chance_fishing")
            .defineInRange("dropChanceFishing", 5.0, 0.0, 100.0);

    /**
     * チェストからのジュエルドロップ確率（%）
     */
    public static final ModConfigSpec.DoubleValue JEWEL_DROP_CHANCE_CHEST = BUILDER
            .comment("ダンジョンチェストからのジュエルドロップ確率 (0.0-100.0)")
            .translation("config.anvil.jewel_drop.chance_chest")
            .defineInRange("dropChanceChest", 10.0, 0.0, 100.0);

    /**
     * 強敵からのレアリティボーナス（%）
     */
    public static final ModConfigSpec.DoubleValue RARITY_BONUS_STRONG = BUILDER
            .comment("強敵モブからのレアリティボーナス (0.0-50.0)")
            .translation("config.anvil.jewel_drop.rarity_bonus_strong")
            .defineInRange("rarityBonusStrong", 5.0, 0.0, 50.0);

    /**
     * ボスからのレアリティボーナス（%）
     */
    public static final ModConfigSpec.DoubleValue RARITY_BONUS_BOSS = BUILDER
            .comment("ボスモブからのレアリティボーナス (0.0-50.0)")
            .translation("config.anvil.jewel_drop.rarity_bonus_boss")
            .defineInRange("rarityBonusBoss", 15.0, 0.0, 50.0);

    /**
     * 釣りでのレアリティボーナス（%）
     */
    public static final ModConfigSpec.DoubleValue RARITY_BONUS_FISHING = BUILDER
            .comment("釣りでのレアリティボーナス (0.0-50.0)")
            .translation("config.anvil.jewel_drop.rarity_bonus_fishing")
            .defineInRange("rarityBonusFishing", 5.0, 0.0, 50.0);

    /**
     * チェストからのレアリティボーナス（%）
     */
    public static final ModConfigSpec.DoubleValue RARITY_BONUS_CHEST = BUILDER
            .comment("チェストからのレアリティボーナス (0.0-50.0)")
            .translation("config.anvil.jewel_drop.rarity_bonus_chest")
            .defineInRange("rarityBonusChest", 10.0, 0.0, 50.0);

    static {
        BUILDER.pop();
    }

    // ============================================
    // バランス設定 (Balance)
    // ============================================

    static {
        BUILDER.comment("バランス調整の設定").push("balance");
    }

    /**
     * リスペックコスト倍率
     */
    public static final ModConfigSpec.DoubleValue RESPEC_COST_MULTIPLIER = BUILDER
            .comment("スキルリスペック時のコスト倍率 (0.1-5.0)")
            .translation("config.anvil.balance.respec_cost")
            .defineInRange("respecCostMultiplier", 1.0, 0.1, 5.0);

    static {
        BUILDER.pop();
    }

    // 設定仕様をビルド
    static final ModConfigSpec SPEC = BUILDER.build();
}
