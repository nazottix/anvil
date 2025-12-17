package io.github.nazottix.anvil.affix;

import net.minecraft.ChatFormatting;

/**
 * ツールレアリティ列挙型
 *
 * ツールの希少度を定義します。
 * レアリティが高いほどアフィックス数が多く、ステータス乗数も高くなります。
 *
 * 仕様書参照: docs/06_レアリティシステム.md - 8.2 レアリティティア
 */
public enum ToolRarity {
    /**
     * ノーマル（白）
     * 基本的なツール、アフィックスなし
     */
    NORMAL(
            "normal",
            "ノーマル",
            ChatFormatting.WHITE,
            0xFFFFFF,
            1.0f,
            4,
            0, 0  // プレフィックス0、サフィックス0
    ),

    /**
     * マジック（青）
     * 1-2個のアフィックスを持つ
     */
    MAGIC(
            "magic",
            "マジック",
            ChatFormatting.BLUE,
            0x5555FF,
            1.1f,
            5,
            1, 1  // プレフィックス最大1、サフィックス最大1
    ),

    /**
     * レア（黄）
     * 3-6個のアフィックスを持つ強力なツール
     */
    RARE(
            "rare",
            "レア",
            ChatFormatting.YELLOW,
            0xFFFF55,
            1.25f,
            6,
            3, 3  // プレフィックス最大3、サフィックス最大3
    ),

    /**
     * ユニーク（オレンジ）
     * 固有の効果を持つ特別なツール
     */
    UNIQUE(
            "unique",
            "ユニーク",
            ChatFormatting.GOLD,
            0xFFAA00,
            1.0f,  // ユニークは固定ステータス
            0,     // ユニークはMODスロット固定
            0, 0   // ユニークは固有効果
    ),

    /**
     * レガシー（赤）
     * 最高峰のツール、4-8個のアフィックス
     */
    LEGACY(
            "legacy",
            "レガシー",
            ChatFormatting.RED,
            0xFF5555,
            1.5f,
            8,
            4, 4  // プレフィックス最大4、サフィックス最大4
    );

    private final String id;
    private final String displayNameJa;
    private final ChatFormatting chatColor;
    private final int color;
    private final float statMultiplier;
    private final int modSlots;
    private final int maxPrefixes;
    private final int maxSuffixes;

    ToolRarity(String id, String displayNameJa, ChatFormatting chatColor, int color,
               float statMultiplier, int modSlots, int maxPrefixes, int maxSuffixes) {
        this.id = id;
        this.displayNameJa = displayNameJa;
        this.chatColor = chatColor;
        this.color = color;
        this.statMultiplier = statMultiplier;
        this.modSlots = modSlots;
        this.maxPrefixes = maxPrefixes;
        this.maxSuffixes = maxSuffixes;
    }

    // ============================================
    // ゲッター
    // ============================================

    public String getId() {
        return id;
    }

    public String getDisplayNameJa() {
        return displayNameJa;
    }

    public ChatFormatting getChatColor() {
        return chatColor;
    }

    public int getColor() {
        return color;
    }

    /**
     * ステータス乗数を取得
     * 全ステータスにこの乗数が適用される
     */
    public float getStatMultiplier() {
        return statMultiplier;
    }

    /**
     * MODスロット数を取得
     */
    public int getModSlots() {
        return modSlots;
    }

    /**
     * プレフィックス最大数を取得
     */
    public int getMaxPrefixes() {
        return maxPrefixes;
    }

    /**
     * サフィックス最大数を取得
     */
    public int getMaxSuffixes() {
        return maxSuffixes;
    }

    /**
     * アフィックス最大数（合計）を取得
     */
    public int getMaxAffixes() {
        return maxPrefixes + maxSuffixes;
    }

    /**
     * アフィックスを持てるかどうか
     */
    public boolean canHaveAffixes() {
        return maxPrefixes > 0 || maxSuffixes > 0;
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * ローカライズキーを取得
     */
    public String getTranslationKey() {
        return "rarity.anvil." + id;
    }

    /**
     * 次のレアリティを取得
     *
     * @return 次のレアリティ、LEGACY以降はnull
     */
    public ToolRarity getNext() {
        return switch (this) {
            case NORMAL -> MAGIC;
            case MAGIC -> RARE;
            case RARE -> LEGACY;
            default -> null;
        };
    }

    /**
     * IDからToolRarityを取得
     *
     * @param id レアリティID
     * @return 対応するToolRarity、見つからない場合はNORMAL
     */
    public static ToolRarity fromId(String id) {
        for (ToolRarity rarity : values()) {
            if (rarity.id.equals(id)) {
                return rarity;
            }
        }
        return NORMAL;
    }

    /**
     * アフィックス数からレアリティを判定
     *
     * @param prefixCount プレフィックス数
     * @param suffixCount サフィックス数
     * @return 適切なレアリティ
     */
    public static ToolRarity fromAffixCount(int prefixCount, int suffixCount) {
        int total = prefixCount + suffixCount;
        if (total == 0) {
            return NORMAL;
        } else if (total <= 2 && prefixCount <= 1 && suffixCount <= 1) {
            return MAGIC;
        } else if (total <= 6 && prefixCount <= 3 && suffixCount <= 3) {
            return RARE;
        } else {
            return LEGACY;
        }
    }
}
