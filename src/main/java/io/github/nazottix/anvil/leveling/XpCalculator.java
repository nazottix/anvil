package io.github.nazottix.anvil.leveling;

import io.github.nazottix.anvil.Config;

/**
 * XP計算クラス
 *
 * レベルに必要なXP量の計算と、XP獲得量の計算を行います。
 * 10,000レベルに対応する緩やかな指数曲線を実装しています。
 *
 * 仕様書参照: docs/02_レベリングシステム.md
 */
public final class XpCalculator {

    // レベル区分の境界
    private static final int EARLY_GAME_MAX = 100;
    private static final int MID_GAME_MAX = 1000;
    private static final int LATE_GAME_MAX = 5000;

    // 各区間の係数
    private static final long EARLY_MULTIPLIER = 100L;
    private static final long MID_MULTIPLIER = 500L;
    private static final long LATE_MULTIPLIER = 2000L;
    private static final long END_MULTIPLIER = 10000L;

    // キャッシュ（よく使うレベルのXP値）
    private static long cachedXpAt100 = -1;
    private static long cachedXpAt1000 = -1;
    private static long cachedXpAt5000 = -1;

    // ============================================
    // XP要求量計算
    // ============================================

    /**
     * 指定レベルに到達するために必要な累計XPを計算
     *
     * 計算式:
     * - 序盤（1-100）: 100 × level²
     * - 中盤（101-1000）: 序盤分 + 500 × (level-100)²
     * - 後半（1001-5000）: 中盤分 + 2000 × (level-1000) × √(level-1000)
     * - 終盤（5001-10000）: 後半分 + 10000 × (level-5000) × log(level-4999)
     *
     * @param level 目標レベル
     * @return 必要な累計XP
     */
    public static long calculateXpForLevel(int level) {
        if (level <= 0) {
            return 0L;
        }

        if (level <= EARLY_GAME_MAX) {
            // 序盤: 比較的速い成長（新規プレイヤー体験重視）
            return EARLY_MULTIPLIER * level * level;
        } else if (level <= MID_GAME_MAX) {
            // 中盤: 標準的な成長
            long base = getXpAt100();
            int levelDiff = level - EARLY_GAME_MAX;
            return base + MID_MULTIPLIER * levelDiff * levelDiff;
        } else if (level <= LATE_GAME_MAX) {
            // 後半: 緩やかな成長
            long base = getXpAt1000();
            int levelDiff = level - MID_GAME_MAX;
            return base + (long)(LATE_MULTIPLIER * levelDiff * Math.sqrt(levelDiff));
        } else {
            // 終盤: 非常に緩やかな成長（やり込み勢向け）
            long base = getXpAt5000();
            int levelDiff = level - LATE_GAME_MAX;
            return base + (long)(END_MULTIPLIER * levelDiff * Math.log(levelDiff + 1));
        }
    }

    /**
     * 次のレベルに必要なXP量を計算
     *
     * @param currentLevel 現在のレベル
     * @return 次のレベルに必要なXP
     */
    public static long getXpForNextLevel(int currentLevel) {
        return calculateXpForLevel(currentLevel + 1) - calculateXpForLevel(currentLevel);
    }

    /**
     * 累計XPからレベルを計算
     *
     * 二分探索でレベルを特定します。
     *
     * @param totalXp 累計XP
     * @return 対応するレベル
     */
    public static int calculateLevelFromXp(long totalXp) {
        if (totalXp <= 0) {
            return 1;
        }

        int maxLevel = Config.MAX_LEVEL.get();
        int low = 1;
        int high = maxLevel;

        while (low < high) {
            int mid = (low + high + 1) / 2;
            if (calculateXpForLevel(mid) <= totalXp) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }

        return Math.min(low, maxLevel);
    }

    /**
     * 現在レベル内での進捗率を計算
     *
     * @param level 現在のレベル
     * @param currentXp 現在レベル内のXP
     * @return 進捗率（0.0 - 1.0）
     */
    public static float calculateProgress(int level, long currentXp) {
        long needed = getXpForNextLevel(level);
        if (needed <= 0) {
            return 1.0f;
        }
        return Math.min(1.0f, (float) currentXp / needed);
    }

    // ============================================
    // キャッシュ用ヘルパー
    // ============================================

    private static long getXpAt100() {
        if (cachedXpAt100 < 0) {
            cachedXpAt100 = EARLY_MULTIPLIER * EARLY_GAME_MAX * EARLY_GAME_MAX;
        }
        return cachedXpAt100;
    }

    private static long getXpAt1000() {
        if (cachedXpAt1000 < 0) {
            long base = getXpAt100();
            int diff = MID_GAME_MAX - EARLY_GAME_MAX;
            cachedXpAt1000 = base + MID_MULTIPLIER * diff * diff;
        }
        return cachedXpAt1000;
    }

    private static long getXpAt5000() {
        if (cachedXpAt5000 < 0) {
            long base = getXpAt1000();
            int diff = LATE_GAME_MAX - MID_GAME_MAX;
            cachedXpAt5000 = base + (long)(LATE_MULTIPLIER * diff * Math.sqrt(diff));
        }
        return cachedXpAt5000;
    }

    // コンストラクタを非公開
    private XpCalculator() {}
}
