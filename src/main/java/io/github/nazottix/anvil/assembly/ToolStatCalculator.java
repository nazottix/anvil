package io.github.nazottix.anvil.assembly;

import io.github.nazottix.anvil.data.component.ToolPart;
import io.github.nazottix.anvil.material.Grade;
import io.github.nazottix.anvil.material.Material;
import io.github.nazottix.anvil.material.MaterialRegistry;
import io.github.nazottix.anvil.material.MaterialStats;
import io.github.nazottix.anvil.tool.PartType;
import io.github.nazottix.anvil.tool.ToolType;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * ツールステータス計算機
 *
 * パーツ構成、素材、グレードからツールの最終ステータスを計算します。
 *
 * 計算式:
 * - 耐久値 = Σ(パーツ耐久値 × グレード乗数) × ハンドル耐久倍率
 * - 採掘速度 = ヘッド採掘速度 × グレード乗数
 * - 攻撃力 = ヘッド/ブレード攻撃力 × グレード乗数
 * - 攻撃速度 = 基本値 + ハンドル攻撃速度修正
 *
 * 仕様書参照: docs/01_パーツ_素材システム.md
 */
public class ToolStatCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToolStatCalculator.class);

    // 基本攻撃速度
    private static final float BASE_ATTACK_SPEED = 4.0f;

    /**
     * パーツリストからツールステータスを計算
     *
     * @param toolType ツールタイプ
     * @param parts パーツリスト
     * @return 計算されたステータス
     */
    public static CalculatedStats calculate(ToolType toolType, List<ToolPart> parts) {
        CalculatedStats.Builder builder = CalculatedStats.builder();

        // 基本容量を設定
        builder.modCapacity(toolType.getBaseCapacity());

        // パーツがない場合はデフォルト値を返す
        if (parts == null || parts.isEmpty()) {
            LOGGER.warn("パーツがありません。デフォルト値を使用します: {}", toolType.getId());
            return builder.build();
        }

        // 各パーツの素材を取得してステータスを計算
        MaterialRegistry registry = MaterialRegistry.getInstance();

        // 耐久値計算用の変数
        int totalDurability = 0;
        float durabilityMultiplier = 1.0f;

        // 採掘/攻撃用の変数
        float miningSpeed = 0;
        int miningLevel = 0;
        float attackDamage = 0;
        float attackSpeedModifier = 0;

        // 弓用の変数
        float drawSpeed = 0;
        float range = 0;
        int bowLimbCount = 0;

        // 釣り竿用
        float fishingEfficiency = 0;

        // ハサミ用
        float cuttingEfficiency = 0;
        int bladeCount = 0;

        // 各パーツを処理
        for (ToolPart part : parts) {
            // 素材を取得
            Optional<Material> materialOpt = registry.get(ResourceLocation.parse(part.materialId()));
            if (materialOpt.isEmpty()) {
                LOGGER.warn("素材が見つかりません: {}", part.materialId());
                continue;
            }

            Material material = materialOpt.get();
            Grade grade = Grade.fromId(part.grade());
            PartType partType = PartType.fromId(part.partType());

            if (partType == null) {
                LOGGER.warn("パーツタイプが見つかりません: {}", part.partType());
                continue;
            }

            // パーツタイプに応じてステータスを計算
            switch (partType) {
                case HEAD -> {
                    // ヘッドパーツ: 耐久値、採掘速度、採掘レベル、攻撃力
                    MaterialStats.HeadStats stats = material.getHeadStatsWithGrade(grade);
                    totalDurability += stats.durability();
                    miningSpeed = Math.max(miningSpeed, stats.miningSpeed());
                    miningLevel = Math.max(miningLevel, stats.miningLevel());
                    attackDamage = Math.max(attackDamage, stats.attackDamage());
                }
                case HANDLE -> {
                    // ハンドルパーツ: 耐久倍率、攻撃速度
                    MaterialStats.HandleStats stats = material.getHandleStatsWithGrade(grade);
                    durabilityMultiplier *= stats.durabilityMultiplier();
                    attackSpeedModifier += stats.attackSpeedModifier();
                }
                case BINDING -> {
                    // バインディング: 特性増幅のみ（ステータス直接影響なし）
                    // 耐久値に少し貢献
                    totalDurability += (int)(material.getHeadStats().durability() * 0.1 * grade.getStatMultiplier());
                }
                case BLADE -> {
                    // ブレード: 攻撃力、切断効率
                    MaterialStats.HeadStats stats = material.getHeadStatsWithGrade(grade);
                    attackDamage += stats.attackDamage();
                    cuttingEfficiency += stats.miningSpeed() * 0.5f;
                    totalDurability += stats.durability();
                    bladeCount++;
                }
                case GUARD -> {
                    // ガード: 耐久値に少し貢献
                    totalDurability += (int)(material.getHeadStats().durability() * 0.15 * grade.getStatMultiplier());
                }
                case BOW_LIMB -> {
                    // ボウリム: 引き速度、射程
                    MaterialStats.BowLimbStats stats = material.getBowLimbStats().withGradeMultiplier(grade.getStatMultiplier());
                    drawSpeed += stats.drawSpeed();
                    range += stats.range();
                    totalDurability += (int)(material.getHeadStats().durability() * 0.5 * grade.getStatMultiplier());
                    bowLimbCount++;
                }
                case BOWSTRING -> {
                    // ボウストリング: 引き速度と精度に影響
                    MaterialStats.BowstringStats stats = material.getBowstringStats().withGradeMultiplier(grade.getStatMultiplier());
                    drawSpeed *= stats.arrowSpeed();
                    totalDurability += (int)(material.getHeadStats().durability() * 0.2 * grade.getStatMultiplier());
                }
                case ROD -> {
                    // ロッド（釣り竿）: 耐久値
                    MaterialStats.HandleStats stats = material.getHandleStatsWithGrade(grade);
                    durabilityMultiplier *= stats.durabilityMultiplier();
                    totalDurability += (int)(material.getHeadStats().durability() * 0.8 * grade.getStatMultiplier());
                }
                case HOOK -> {
                    // フック: 釣り効率
                    MaterialStats.HookStats stats = material.getHookStats().withGradeMultiplier(grade.getStatMultiplier());
                    fishingEfficiency = stats.fishingEfficiency();
                }
                case LINE -> {
                    // ライン: 耐久値
                    totalDurability += (int)(material.getHeadStats().durability() * 0.3 * grade.getStatMultiplier());
                }
                case PIVOT -> {
                    // ピボット: 耐久値
                    totalDurability += (int)(material.getHeadStats().durability() * 0.2 * grade.getStatMultiplier());
                }
                case COATING -> {
                    // コーティング: 全ステータス倍率
                    MaterialStats.CoatingStats stats = material.getCoatingStats().withGradeMultiplier(grade.getStatMultiplier());
                    // コーティングは最終計算で適用（現時点では未実装）
                }
                case UPGRADE -> {
                    // アップグレード: MODスロット追加
                    MaterialStats.UpgradeStats stats = material.getUpgradeStats();
                    builder.modCapacity(toolType.getBaseCapacity() + stats.modSlotBonus());
                }
            }
        }

        // 最終計算
        // 耐久値 = 合計 × 倍率
        int finalDurability = (int)(totalDurability * durabilityMultiplier);
        builder.durability(Math.max(1, finalDurability));

        // 採掘速度（最低1.0）
        builder.miningSpeed(Math.max(1.0f, miningSpeed));

        // 採掘レベル
        builder.miningLevel(miningLevel);

        // 攻撃力
        if (bladeCount > 0) {
            // ハサミは2枚のブレードの平均
            attackDamage /= bladeCount;
        }
        builder.attackDamage(Math.max(1.0f, attackDamage));

        // 攻撃速度 = 基本値 + 修正値
        builder.attackSpeed(Math.max(0.5f, BASE_ATTACK_SPEED + attackSpeedModifier));

        // 弓の引き速度と射程（複数リムの平均）
        if (bowLimbCount > 0) {
            builder.drawSpeed(drawSpeed / bowLimbCount);
            builder.range(range / bowLimbCount);
        } else {
            builder.drawSpeed(1.0f);
            builder.range(1.0f);
        }

        // 釣り効率
        builder.fishingEfficiency(Math.max(1.0f, fishingEfficiency));

        // 切断効率
        builder.cuttingEfficiency(Math.max(1.0f, cuttingEfficiency));

        return builder.build();
    }

    /**
     * ツールタイプIDとパーツリストからステータスを計算
     *
     * @param toolTypeId ツールタイプID
     * @param parts パーツリスト
     * @return 計算されたステータス
     */
    public static CalculatedStats calculate(String toolTypeId, List<ToolPart> parts) {
        ToolType toolType = ToolType.fromId(toolTypeId);
        if (toolType == null) {
            LOGGER.warn("ツールタイプが見つかりません: {}", toolTypeId);
            return CalculatedStats.DEFAULT;
        }
        return calculate(toolType, parts);
    }
}
