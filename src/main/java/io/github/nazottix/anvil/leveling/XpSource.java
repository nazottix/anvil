package io.github.nazottix.anvil.leveling;

import io.github.nazottix.anvil.tool.ToolType;

/**
 * XP獲得ソースの定義
 *
 * 各ツールタイプごとのXP獲得条件とベースXPを定義します。
 *
 * 仕様書参照: docs/02_レベリングシステム.md
 */
public enum XpSource {

    // ============================================
    // 採掘系
    // ============================================

    /** 通常ブロック破壊 */
    BLOCK_BREAK(1, 5),

    /** 鉱石破壊 */
    ORE_BREAK(10, 50),

    /** ネザー鉱石破壊 */
    NETHER_ORE_BREAK(20, 80),

    /** 古代の残骸破壊 */
    ANCIENT_DEBRIS_BREAK(100, 100),

    // ============================================
    // 木こり系
    // ============================================

    /** 原木破壊 */
    LOG_BREAK(2, 10),

    /** 特殊木材破壊 */
    SPECIAL_LOG_BREAK(5, 15),

    // ============================================
    // 掘削系
    // ============================================

    /** 土/砂系破壊 */
    SOIL_BREAK(1, 5),

    // ============================================
    // 戦闘系
    // ============================================

    /** 敵対モブキル */
    HOSTILE_KILL(10, 100),

    /** ボスキル */
    BOSS_KILL(200, 500),

    /** プレイヤーキル（PvP） */
    PLAYER_KILL(50, 200),

    // ============================================
    // 農業系
    // ============================================

    /** 作物収穫 */
    CROP_HARVEST(5, 20),

    /** 完全成長作物収穫 */
    MATURE_CROP_HARVEST(10, 30),

    // ============================================
    // 弓系
    // ============================================

    /** 近距離キル */
    BOW_CLOSE_KILL(15, 80),

    /** 遠距離キル（ボーナス） */
    BOW_LONG_KILL(30, 150),

    /** スナイパーキル（超遠距離） */
    BOW_SNIPER_KILL(100, 600),

    // ============================================
    // 釣り系
    // ============================================

    /** ジャンク釣り */
    FISHING_JUNK(5, 10),

    /** 魚釣り */
    FISHING_FISH(10, 30),

    /** 宝釣り */
    FISHING_TREASURE(50, 100),

    // ============================================
    // ハサミ系
    // ============================================

    /** 羊毛刈り */
    SHEARING_WOOL(5, 10),

    /** 葉収穫 */
    SHEARING_LEAVES(1, 5),

    /** 特殊刈り取り */
    SHEARING_SPECIAL(10, 15);

    // ============================================
    // フィールド
    // ============================================

    private final int minXp;
    private final int maxXp;

    XpSource(int minXp, int maxXp) {
        this.minXp = minXp;
        this.maxXp = maxXp;
    }

    /**
     * 最小XPを取得
     */
    public int getMinXp() {
        return minXp;
    }

    /**
     * 最大XPを取得
     */
    public int getMaxXp() {
        return maxXp;
    }

    /**
     * ベースXPを取得（最小と最大の平均）
     */
    public int getBaseXp() {
        return (minXp + maxXp) / 2;
    }

    /**
     * ランダムなXP値を取得
     *
     * @param random 乱数
     * @return minXp〜maxXpの範囲のXP
     */
    public int getRandomXp(java.util.Random random) {
        if (minXp == maxXp) {
            return minXp;
        }
        return minXp + random.nextInt(maxXp - minXp + 1);
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * ツールタイプに対応するデフォルトXPソースを取得
     *
     * @param toolType ツールタイプ
     * @return デフォルトのXPソース
     */
    public static XpSource getDefaultForToolType(ToolType toolType) {
        return switch (toolType) {
            case PICKAXE -> BLOCK_BREAK;
            case AXE -> LOG_BREAK;
            case SHOVEL -> SOIL_BREAK;
            case SWORD -> HOSTILE_KILL;
            case HOE -> CROP_HARVEST;
            case BOW -> BOW_CLOSE_KILL;
            case FISHING_ROD -> FISHING_FISH;
            case SHEARS -> SHEARING_WOOL;
        };
    }
}
