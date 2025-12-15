package io.github.nazottix.anvil.mod;

import net.minecraft.ChatFormatting;

/**
 * MODレアリティの定義
 *
 * MODの希少度と基本性能を決定します。
 *
 * 仕様書参照: docs/05_容量制限システム.md
 */
public enum ModRarity {

    // ============================================
    // レアリティ（4種類）
    // ============================================

    /**
     * コモン
     * - ベースドレイン: 2-4
     * - 最大ランク: 5
     * - ドロップ率: 60%
     */
    COMMON("common", 2, 4, 5, 0.60, ChatFormatting.WHITE),

    /**
     * アンコモン
     * - ベースドレイン: 4-6
     * - 最大ランク: 5
     * - ドロップ率: 25%
     */
    UNCOMMON("uncommon", 4, 6, 5, 0.25, ChatFormatting.GREEN),

    /**
     * レア
     * - ベースドレイン: 6-10
     * - 最大ランク: 10
     * - ドロップ率: 12%
     */
    RARE("rare", 6, 10, 10, 0.12, ChatFormatting.BLUE),

    /**
     * レジェンダリー
     * - ベースドレイン: 10-14
     * - 最大ランク: 10
     * - ドロップ率: 3%
     */
    LEGENDARY("legendary", 10, 14, 10, 0.03, ChatFormatting.GOLD);

    // ============================================
    // フィールド
    // ============================================

    private final String id;
    private final int minDrain;
    private final int maxDrain;
    private final int maxRank;
    private final double dropRate;
    private final ChatFormatting color;

    ModRarity(String id, int minDrain, int maxDrain, int maxRank, double dropRate, ChatFormatting color) {
        this.id = id;
        this.minDrain = minDrain;
        this.maxDrain = maxDrain;
        this.maxRank = maxRank;
        this.dropRate = dropRate;
        this.color = color;
    }

    // ============================================
    // ゲッター
    // ============================================

    public String getId() {
        return id;
    }

    public int getMinDrain() {
        return minDrain;
    }

    public int getMaxDrain() {
        return maxDrain;
    }

    public int getMaxRank() {
        return maxRank;
    }

    public double getDropRate() {
        return dropRate;
    }

    public ChatFormatting getColor() {
        return color;
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * IDからレアリティを取得
     */
    public static ModRarity fromId(String id) {
        if (id == null || id.isEmpty()) {
            return COMMON;
        }
        for (ModRarity rarity : values()) {
            if (rarity.id.equals(id)) {
                return rarity;
            }
        }
        return COMMON;
    }

    /**
     * ランダムなドレイン値を取得
     */
    public int getRandomDrain(java.util.Random random) {
        if (minDrain == maxDrain) {
            return minDrain;
        }
        return minDrain + random.nextInt(maxDrain - minDrain + 1);
    }
}
