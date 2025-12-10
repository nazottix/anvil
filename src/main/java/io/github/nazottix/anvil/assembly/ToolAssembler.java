package io.github.nazottix.anvil.assembly;

import io.github.nazottix.anvil.data.component.ToolPart;
import io.github.nazottix.anvil.material.Grade;
import io.github.nazottix.anvil.material.Material;
import io.github.nazottix.anvil.material.MaterialRegistry;
import io.github.nazottix.anvil.tool.PartType;
import io.github.nazottix.anvil.tool.ToolType;
import io.github.nazottix.anvil.trait.Trait;
import io.github.nazottix.anvil.trait.TraitRegistry;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * ツールアセンブラー
 *
 * パーツを組み合わせてツールを作成するメインクラスです。
 * パーツの検証、ステータス計算、特性集約を行います。
 *
 * 使用例:
 * ```java
 * AssemblyResult result = ToolAssembler.assemble(
 *     ToolType.PICKAXE,
 *     List.of(
 *         new ToolPart("anvil:iron", "head", "c"),
 *         new ToolPart("anvil:oak", "handle", "b"),
 *         new ToolPart("anvil:copper", "binding", "c")
 *     )
 * );
 *
 * if (result.isSuccess()) {
 *     // ツール作成成功
 *     CalculatedStats stats = result.stats();
 * }
 * ```
 *
 * 仕様書参照: docs/01_パーツ_素材システム.md
 */
public class ToolAssembler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToolAssembler.class);

    /**
     * パーツからツールを組み立てる
     *
     * @param toolType ツールタイプ
     * @param parts パーツリスト
     * @return 組み立て結果
     */
    public static AssemblyResult assemble(ToolType toolType, List<ToolPart> parts) {
        // ツールタイプの検証
        if (toolType == null) {
            return AssemblyResult.failure("invalid_tool_type");
        }

        // パーツの検証
        ValidationResult validation = validateParts(toolType, parts);
        if (!validation.isValid()) {
            return AssemblyResult.failure(toolType, validation.errorKey());
        }

        // ステータス計算
        CalculatedStats stats = ToolStatCalculator.calculate(toolType, parts);

        // 特性の集約
        Map<String, Integer> aggregatedTraits = aggregateTraits(parts);

        // 主要素材の決定（ヘッドまたは最初のパーツの素材）
        Material primaryMaterial = determinePrimaryMaterial(toolType, parts);

        LOGGER.debug("ツール組み立て成功: {} (耐久: {}, 攻撃力: {})",
                toolType.getId(), stats.durability(), stats.attackDamage());

        return AssemblyResult.success(toolType, parts, stats, aggregatedTraits, primaryMaterial);
    }

    /**
     * ツールタイプIDを使用してパーツからツールを組み立てる
     *
     * @param toolTypeId ツールタイプID
     * @param parts パーツリスト
     * @return 組み立て結果
     */
    public static AssemblyResult assemble(String toolTypeId, List<ToolPart> parts) {
        ToolType toolType = ToolType.fromId(toolTypeId);
        if (toolType == null) {
            return AssemblyResult.failure("invalid_tool_type");
        }
        return assemble(toolType, parts);
    }

    /**
     * パーツを検証
     *
     * @param toolType ツールタイプ
     * @param parts パーツリスト
     * @return 検証結果
     */
    public static ValidationResult validateParts(ToolType toolType, List<ToolPart> parts) {
        if (parts == null || parts.isEmpty()) {
            return ValidationResult.failure("no_parts");
        }

        List<PartType> requiredParts = toolType.getRequiredParts();
        List<PartType> providedParts = new ArrayList<>();

        MaterialRegistry materialRegistry = MaterialRegistry.getInstance();

        // 各パーツを検証
        for (ToolPart part : parts) {
            // パーツタイプの検証
            PartType partType = PartType.fromId(part.partType());
            if (partType == null) {
                return ValidationResult.failure("invalid_part_type");
            }
            providedParts.add(partType);

            // 素材の検証
            Optional<Material> material = materialRegistry.get(part.materialId());
            if (material.isEmpty()) {
                return ValidationResult.failure("invalid_material");
            }

            // グレードの検証
            Grade grade = Grade.fromId(part.grade());
            if (grade == null) {
                return ValidationResult.failure("invalid_grade");
            }
        }

        // 必要なパーツがすべて揃っているか確認
        List<PartType> missingParts = new ArrayList<>(requiredParts);
        for (PartType provided : providedParts) {
            missingParts.remove(provided);
        }

        if (!missingParts.isEmpty()) {
            LOGGER.debug("不足パーツ: {}", missingParts);
            return ValidationResult.failure("missing_parts");
        }

        return ValidationResult.success();
    }

    /**
     * パーツから特性を集約
     *
     * 同じ特性は最大レベルまで合算されます。
     *
     * @param parts パーツリスト
     * @return 特性マップ（特性ID → レベル）
     */
    private static Map<String, Integer> aggregateTraits(List<ToolPart> parts) {
        Map<String, Integer> traitLevels = new HashMap<>();
        MaterialRegistry materialRegistry = MaterialRegistry.getInstance();
        TraitRegistry traitRegistry = TraitRegistry.getInstance();

        for (ToolPart part : parts) {
            Optional<Material> materialOpt = materialRegistry.get(part.materialId());
            if (materialOpt.isEmpty()) continue;

            Material material = materialOpt.get();
            PartType partType = PartType.fromId(part.partType());

            // パーツタイプに応じて特性を取得
            List<Material.TraitEntry> traits = switch (partType) {
                case HEAD, BLADE, BOW_LIMB -> material.getHeadTraits();
                case HANDLE, ROD -> material.getHandleTraits();
                default -> material.getExtraTraits();
            };

            // 特性を集約
            for (Material.TraitEntry entry : traits) {
                String traitId = entry.traitId().toString();
                int currentLevel = traitLevels.getOrDefault(traitId, 0);
                int newLevel = currentLevel + entry.level();

                // 最大レベルを確認
                Optional<Trait> traitOpt = traitRegistry.get(entry.traitId());
                if (traitOpt.isPresent()) {
                    newLevel = Math.min(newLevel, traitOpt.get().getMaxLevel());
                }

                traitLevels.put(traitId, newLevel);
            }
        }

        // 相互排他チェック（片方のみ残す）
        removeIncompatibleTraits(traitLevels);

        return traitLevels;
    }

    /**
     * 相互排他の特性を除去
     *
     * 両方存在する場合、レベルの高い方を残します。
     */
    private static void removeIncompatibleTraits(Map<String, Integer> traitLevels) {
        TraitRegistry registry = TraitRegistry.getInstance();
        List<String> toRemove = new ArrayList<>();

        for (String traitId : traitLevels.keySet()) {
            Optional<Trait> traitOpt = registry.get(traitId);
            if (traitOpt.isEmpty()) continue;

            Trait trait = traitOpt.get();
            for (ResourceLocation incompatibleId : trait.getIncompatibleTraits()) {
                String incompatibleStr = incompatibleId.toString();
                if (traitLevels.containsKey(incompatibleStr)) {
                    // レベルの低い方を削除
                    int level1 = traitLevels.get(traitId);
                    int level2 = traitLevels.get(incompatibleStr);
                    if (level1 >= level2) {
                        toRemove.add(incompatibleStr);
                    } else {
                        toRemove.add(traitId);
                    }
                }
            }
        }

        for (String id : toRemove) {
            traitLevels.remove(id);
        }
    }

    /**
     * 主要素材を決定
     *
     * ヘッドまたはブレードの素材を主要素材とします。
     */
    private static Material determinePrimaryMaterial(ToolType toolType, List<ToolPart> parts) {
        MaterialRegistry registry = MaterialRegistry.getInstance();

        // 優先順位: HEAD > BLADE > 最初のパーツ
        for (ToolPart part : parts) {
            if (part.partType().equals("head") || part.partType().equals("blade")) {
                Optional<Material> material = registry.get(part.materialId());
                if (material.isPresent()) {
                    return material.get();
                }
            }
        }

        // フォールバック: 最初のパーツの素材
        if (!parts.isEmpty()) {
            return registry.get(parts.get(0).materialId()).orElse(null);
        }

        return null;
    }

    /**
     * 検証結果
     */
    public record ValidationResult(boolean isValid, String errorKey) {
        // 成功結果を作成するファクトリメソッド
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        // 失敗結果を作成するファクトリメソッド
        public static ValidationResult failure(String errorKey) {
            return new ValidationResult(false, errorKey);
        }

        // 検証が無効かどうか
        public boolean isInvalid() {
            return !isValid;
        }
    }
}
