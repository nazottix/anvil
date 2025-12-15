package io.github.nazottix.anvil.mod;

import io.github.nazottix.anvil.ANVIL;
import net.minecraft.resources.ResourceLocation;

/**
 * MODフュージョンシステム
 *
 * MODのランクアップ（強化）を管理します。
 * 同じMODを融合することでランクを上げることができます。
 *
 * フュージョンコスト:
 * - 同MOD融合: 効率100%
 * - 同レアリティ融合: 効率50%
 * - 低レアリティ融合: 効率25%
 * - エンドー（汎用素材）: 効率100%
 */
public final class ModFusion {

    // ============================================
    // 定数
    // ============================================

    /** ランクあたりの基本融合ポイント（レアリティで乗算） */
    private static final int BASE_FUSION_POINTS_PER_RANK = 100;

    /** レアリティ別融合ポイント乗数 */
    private static final float[] RARITY_MULTIPLIERS = {
            1.0f,   // Common
            2.0f,   // Uncommon
            4.0f,   // Rare
            8.0f    // Legendary
    };

    // ============================================
    // 融合ポイント計算
    // ============================================

    /**
     * 次のランクに必要な融合ポイントを計算
     *
     * @param def MOD定義
     * @param currentRank 現在のランク
     * @return 必要な融合ポイント
     */
    public static int getPointsForNextRank(ModDefinition def, int currentRank) {
        if (currentRank >= def.maxRank()) {
            return Integer.MAX_VALUE; // 最大ランク
        }

        float rarityMultiplier = RARITY_MULTIPLIERS[def.rarity().ordinal()];
        // ランクが上がるほど必要ポイントが増加（1.5倍/ランク）
        return (int) (BASE_FUSION_POINTS_PER_RANK * rarityMultiplier * Math.pow(1.5, currentRank));
    }

    /**
     * MODを素材として融合した場合の獲得ポイントを計算
     *
     * @param targetDef 強化対象のMOD定義
     * @param fuelDef 素材MODの定義
     * @param fuelRank 素材MODのランク
     * @return 獲得融合ポイント
     */
    public static int getFusionPointsFromMod(ModDefinition targetDef, ModDefinition fuelDef, int fuelRank) {
        // 同じMODの場合は効率100%
        if (targetDef.id().equals(fuelDef.id())) {
            return calculateBasePoints(fuelDef, fuelRank);
        }

        // 同じレアリティの場合は効率50%
        if (targetDef.rarity() == fuelDef.rarity()) {
            return calculateBasePoints(fuelDef, fuelRank) / 2;
        }

        // 異なるレアリティの場合は効率25%
        return calculateBasePoints(fuelDef, fuelRank) / 4;
    }

    /**
     * MODの基本融合ポイントを計算
     *
     * @param def MOD定義
     * @param rank ランク
     * @return 基本融合ポイント
     */
    private static int calculateBasePoints(ModDefinition def, int rank) {
        float rarityMultiplier = RARITY_MULTIPLIERS[def.rarity().ordinal()];
        // ランク0でも基本ポイントを持つ
        int basePoints = (int) (BASE_FUSION_POINTS_PER_RANK * rarityMultiplier);
        // ランクごとに50%増加
        return (int) (basePoints * Math.pow(1.5, rank));
    }

    // ============================================
    // 融合実行
    // ============================================

    /**
     * 融合結果
     */
    public record FusionResult(
            InstalledMod resultMod,
            int remainingPoints,
            int totalPointsUsed,
            boolean leveledUp
    ) {}

    /**
     * MODを融合
     *
     * @param targetMod 強化対象MOD
     * @param fuelMods 素材MODリスト
     * @return 融合結果
     */
    public static FusionResult fuseMods(InstalledMod targetMod, java.util.List<InstalledMod> fuelMods) {
        ModDefinition targetDef = ModRegistry.get(targetMod);
        if (targetDef == null) {
            return new FusionResult(targetMod, 0, 0, false);
        }

        // 既に最大ランクの場合
        if (targetMod.rank() >= targetDef.maxRank()) {
            return new FusionResult(targetMod, 0, 0, false);
        }

        // 融合ポイントを計算
        int totalPoints = 0;
        for (InstalledMod fuel : fuelMods) {
            ModDefinition fuelDef = ModRegistry.get(fuel);
            if (fuelDef != null) {
                totalPoints += getFusionPointsFromMod(targetDef, fuelDef, fuel.rank());
            }
        }

        // ランクアップ処理
        int currentRank = targetMod.rank();
        int remainingPoints = totalPoints;
        boolean leveledUp = false;

        while (remainingPoints > 0 && currentRank < targetDef.maxRank()) {
            int neededPoints = getPointsForNextRank(targetDef, currentRank);
            if (remainingPoints >= neededPoints) {
                remainingPoints -= neededPoints;
                currentRank++;
                leveledUp = true;
            } else {
                break;
            }
        }

        InstalledMod resultMod = targetMod.withRank(currentRank);
        int usedPoints = totalPoints - remainingPoints;

        ANVIL.LOGGER.debug("MOD融合: {} ランク {} -> {} (使用ポイント: {})",
                targetMod.modId(), targetMod.rank(), currentRank, usedPoints);

        return new FusionResult(resultMod, remainingPoints, usedPoints, leveledUp);
    }

    // ============================================
    // エンドー（汎用融合素材）
    // ============================================

    /**
     * エンドーの種類
     */
    public enum EndoType {
        /** 小エンドー: 50ポイント */
        SMALL(50),
        /** 中エンドー: 200ポイント */
        MEDIUM(200),
        /** 大エンドー: 1000ポイント */
        LARGE(1000),
        /** 極大エンドー: 5000ポイント */
        LEGENDARY(5000);

        private final int points;

        EndoType(int points) {
            this.points = points;
        }

        public int getPoints() {
            return points;
        }
    }

    /**
     * エンドーを使用してMODを融合
     *
     * @param targetMod 強化対象MOD
     * @param endoType エンドーの種類
     * @param amount エンドーの数量
     * @return 融合結果
     */
    public static FusionResult fuseWithEndo(InstalledMod targetMod, EndoType endoType, int amount) {
        ModDefinition targetDef = ModRegistry.get(targetMod);
        if (targetDef == null) {
            return new FusionResult(targetMod, 0, 0, false);
        }

        if (targetMod.rank() >= targetDef.maxRank()) {
            return new FusionResult(targetMod, 0, 0, false);
        }

        int totalPoints = endoType.getPoints() * amount;
        int currentRank = targetMod.rank();
        int remainingPoints = totalPoints;
        boolean leveledUp = false;

        while (remainingPoints > 0 && currentRank < targetDef.maxRank()) {
            int neededPoints = getPointsForNextRank(targetDef, currentRank);
            if (remainingPoints >= neededPoints) {
                remainingPoints -= neededPoints;
                currentRank++;
                leveledUp = true;
            } else {
                break;
            }
        }

        InstalledMod resultMod = targetMod.withRank(currentRank);
        int usedPoints = totalPoints - remainingPoints;

        return new FusionResult(resultMod, remainingPoints, usedPoints, leveledUp);
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * MODの融合進捗率を取得
     *
     * @param mod MOD
     * @param currentPoints 現在の融合ポイント
     * @return 進捗率（0.0 - 1.0）
     */
    public static float getFusionProgress(InstalledMod mod, int currentPoints) {
        ModDefinition def = ModRegistry.get(mod);
        if (def == null || mod.rank() >= def.maxRank()) {
            return 1.0f;
        }

        int neededPoints = getPointsForNextRank(def, mod.rank());
        return Math.min(1.0f, (float) currentPoints / neededPoints);
    }

    /**
     * MODの現在ランクでの効果値を取得
     *
     * @param mod MOD
     * @param effectIndex 効果インデックス
     * @return 効果値、見つからない場合は0
     */
    public static float getEffectValueAtRank(InstalledMod mod, int effectIndex) {
        ModDefinition def = ModRegistry.get(mod);
        if (def == null || effectIndex >= def.effects().size()) {
            return 0f;
        }

        ModEffect effect = def.effects().get(effectIndex);
        return effect.getScaledValue(mod.rank(), def.maxRank());
    }

    // コンストラクタを非公開
    private ModFusion() {}
}
