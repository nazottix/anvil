package io.github.nazottix.anvil.grid;

/**
 * 重量システム
 *
 * グリッドモジュールの総重量に基づいて速度ランクを計算します。
 *
 * 仕様書参照: docs/04_グリッド配置システム.md
 */
public final class WeightSystem {

    // ============================================
    // 速度ランク
    // ============================================

    /**
     * 速度ランク
     *
     * 総重量に基づいて速度乗数が決定されます。
     */
    public enum SpeedRank {
        /** S: 0-15重量, 1.15x 軽量ビルド */
        S("S", 0, 15, 1.15f),

        /** A: 16-30重量, 1.05x 標準-軽め */
        A("A", 16, 30, 1.05f),

        /** B: 31-45重量, 1.00x 標準 */
        B("B", 31, 45, 1.00f),

        /** C: 46-60重量, 0.95x やや重い */
        C("C", 46, 60, 0.95f),

        /** D: 61-80重量, 0.90x 重い */
        D("D", 61, 80, 0.90f),

        /** E: 81+重量, 0.80x 超重量 */
        E("E", 81, Integer.MAX_VALUE, 0.80f);

        private final String id;
        private final int minWeight;
        private final int maxWeight;
        private final float speedMultiplier;

        SpeedRank(String id, int minWeight, int maxWeight, float speedMultiplier) {
            this.id = id;
            this.minWeight = minWeight;
            this.maxWeight = maxWeight;
            this.speedMultiplier = speedMultiplier;
        }

        public String getId() {
            return id;
        }

        public int getMinWeight() {
            return minWeight;
        }

        public int getMaxWeight() {
            return maxWeight;
        }

        /**
         * 速度乗数を取得
         * 攻撃速度・採掘速度に適用されます
         */
        public float getSpeedMultiplier() {
            return speedMultiplier;
        }

        /**
         * 乗数をパーセント表示で取得
         */
        public String getSpeedMultiplierPercent() {
            int percent = Math.round((speedMultiplier - 1.0f) * 100);
            if (percent >= 0) {
                return "+" + percent + "%";
            }
            return percent + "%";
        }
    }

    // ============================================
    // ランク計算
    // ============================================

    /**
     * 総重量から速度ランクを計算
     *
     * @param totalWeight 総重量
     * @return 速度ランク
     */
    public static SpeedRank calculateRank(int totalWeight) {
        for (SpeedRank rank : SpeedRank.values()) {
            if (totalWeight >= rank.minWeight && totalWeight <= rank.maxWeight) {
                return rank;
            }
        }
        return SpeedRank.E;
    }

    /**
     * グリッド構成から速度ランクを計算
     *
     * @param config グリッド構成
     * @return 速度ランク
     */
    public static SpeedRank calculateRank(GridConfiguration config) {
        return calculateRank(config.getTotalWeight());
    }

    /**
     * 総重量から速度乗数を取得
     *
     * @param totalWeight 総重量
     * @return 速度乗数
     */
    public static float getSpeedMultiplier(int totalWeight) {
        return calculateRank(totalWeight).getSpeedMultiplier();
    }

    /**
     * グリッド構成から速度乗数を取得
     *
     * @param config グリッド構成
     * @return 速度乗数
     */
    public static float getSpeedMultiplier(GridConfiguration config) {
        return calculateRank(config).getSpeedMultiplier();
    }

    // ============================================
    // 重量軽減
    // ============================================

    /**
     * 重量軽減ボーナスを適用した実効重量を計算
     *
     * @param totalWeight 総重量
     * @param reductionPercent 重量軽減率（0-100）
     * @return 実効重量
     */
    public static int calculateEffectiveWeight(int totalWeight, float reductionPercent) {
        float reduction = Math.min(reductionPercent, 80f) / 100f; // 最大80%軽減
        return (int) Math.ceil(totalWeight * (1.0f - reduction));
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * 次のランクに必要な重量削減量を計算
     *
     * @param currentWeight 現在の総重量
     * @return 次のランクまでに減らすべき重量（-1 = 既に最高ランク）
     */
    public static int getWeightToNextRank(int currentWeight) {
        SpeedRank currentRank = calculateRank(currentWeight);
        int targetOrdinal = currentRank.ordinal() - 1;

        if (targetOrdinal < 0) {
            return -1; // 既にSランク
        }

        SpeedRank targetRank = SpeedRank.values()[targetOrdinal];
        return currentWeight - targetRank.maxWeight;
    }

    /**
     * 指定ランクを維持できる最大重量を取得
     *
     * @param rank 目標ランク
     * @return 最大重量
     */
    public static int getMaxWeightForRank(SpeedRank rank) {
        return rank.maxWeight;
    }

    // コンストラクタを非公開
    private WeightSystem() {}
}
