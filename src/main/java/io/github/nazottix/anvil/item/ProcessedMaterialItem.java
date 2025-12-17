package io.github.nazottix.anvil.item;

import io.github.nazottix.anvil.data.AnvilDataComponents;
import io.github.nazottix.anvil.data.component.ProcessedMaterialData;
import io.github.nazottix.anvil.material.Grade;
import io.github.nazottix.anvil.material.Material;
import io.github.nazottix.anvil.material.MaterialRegistry;
import io.github.nazottix.anvil.processing.ProcessingLevel;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Optional;

/**
 * 加工素材アイテム
 *
 * 加工チェーンで使用される中間素材のアイテムクラスです。
 * 各ステーションで加工されることで、加工レベルが上昇し、
 * 最終的にパーツに変換されます。
 *
 * 加工フロー:
 * バニラ素材 → [精錬所] → REFINED(1)
 *           → [鍛造] → FORGED(2)
 *           → [研磨] → POLISHED(3)/PERFECT(4)/MASTERWORK(5)
 *           → [パーツ鍛造所] → PartItem
 *
 * 仕様書参照: docs/素材加工チェーン仕様.md
 */
public class ProcessedMaterialItem extends Item {

    /**
     * コンストラクタ
     *
     * @param properties アイテムプロパティ
     */
    public ProcessedMaterialItem(Properties properties) {
        super(properties.stacksTo(64));
    }

    // ============================================
    // ProcessedMaterialDataデータ操作
    // ============================================

    /**
     * ItemStackからProcessedMaterialDataを取得
     *
     * @param stack アイテムスタック
     * @return ProcessedMaterialData、なければnull
     */
    public static ProcessedMaterialData getMaterialData(ItemStack stack) {
        if (stack.isEmpty()) return null;
        return stack.get(AnvilDataComponents.PROCESSED_MATERIAL_DATA.get());
    }

    /**
     * ItemStackにProcessedMaterialDataを設定
     *
     * @param stack アイテムスタック
     * @param data ProcessedMaterialData
     */
    public static void setMaterialData(ItemStack stack, ProcessedMaterialData data) {
        stack.set(AnvilDataComponents.PROCESSED_MATERIAL_DATA.get(), data);
    }

    /**
     * 素材と加工レベルを指定してItemStackを作成
     *
     * @param materialId 素材ID
     * @param level 加工レベル
     * @return 作成されたItemStack
     */
    public static ItemStack createStack(String materialId, ProcessingLevel level) {
        ItemStack stack = new ItemStack(AnvilItems.PROCESSED_MATERIAL.get());
        ProcessedMaterialData data = ProcessedMaterialData.withLevel(materialId, level);
        setMaterialData(stack, data);
        return stack;
    }

    /**
     * 素材と加工レベル、品質ボーナスを指定してItemStackを作成
     *
     * @param materialId 素材ID
     * @param level 加工レベル
     * @param qualityBonus 品質ボーナス（0.0-1.0）
     * @return 作成されたItemStack
     */
    public static ItemStack createStack(String materialId, ProcessingLevel level, float qualityBonus) {
        ItemStack stack = new ItemStack(AnvilItems.PROCESSED_MATERIAL.get());
        ProcessedMaterialData data = new ProcessedMaterialData(materialId, level.getLevel(), qualityBonus);
        setMaterialData(stack, data);
        return stack;
    }

    /**
     * ProcessedMaterialDataからItemStackを作成
     *
     * @param data ProcessedMaterialData
     * @return 作成されたItemStack
     */
    public static ItemStack createStack(ProcessedMaterialData data) {
        ItemStack stack = new ItemStack(AnvilItems.PROCESSED_MATERIAL.get());
        setMaterialData(stack, data);
        return stack;
    }

    /**
     * ItemStackが有効なProcessedMaterialDataを持つか確認
     *
     * @param stack アイテムスタック
     * @return 有効なデータを持つ場合true
     */
    public static boolean hasValidMaterialData(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ProcessedMaterialItem)) {
            return false;
        }
        ProcessedMaterialData data = getMaterialData(stack);
        return data != null && data.materialId() != null && !data.materialId().isEmpty();
    }

    /**
     * 加工レベルをアップグレードしたItemStackを作成
     *
     * @param stack 元のアイテムスタック
     * @return アップグレードされたItemStack、アップグレード不可の場合はEMPTY
     */
    public static ItemStack upgradeProcessingLevel(ItemStack stack) {
        ProcessedMaterialData data = getMaterialData(stack);
        if (data == null || !data.canUpgrade()) {
            return ItemStack.EMPTY;
        }
        return createStack(data.upgrade());
    }

    // ============================================
    // ツールチップ
    // ============================================

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        ProcessedMaterialData data = getMaterialData(stack);
        if (data == null) {
            // データがない場合
            tooltipComponents.add(Component.translatable("tooltip.anvil.processed_material.no_data")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        // 素材名を表示
        Optional<Material> materialOpt = MaterialRegistry.getInstance().get(ResourceLocation.parse(data.materialId()));
        if (materialOpt.isPresent()) {
            Material material = materialOpt.get();
            int color = material.getPrimaryColor();
            tooltipComponents.add(Component.translatable("tooltip.anvil.processed_material.material",
                            Component.translatable(material.getTranslationKey()))
                    .withStyle(style -> style.withColor(color)));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.anvil.processed_material.material",
                            Component.literal(data.materialId()))
                    .withStyle(ChatFormatting.GRAY));
        }

        // 加工レベルを表示
        ProcessingLevel level = data.getProcessingLevel();
        ChatFormatting levelColor = getProcessingLevelColor(level);
        tooltipComponents.add(Component.translatable("tooltip.anvil.processed_material.level",
                        Component.translatable(level.getTranslationKey()))
                .withStyle(levelColor));

        // グレードを表示
        Grade grade = data.calculateGrade();
        ChatFormatting gradeColor = getGradeColor(grade);
        tooltipComponents.add(Component.translatable("tooltip.anvil.processed_material.grade",
                        Component.translatable(grade.getTranslationKey()))
                .withStyle(gradeColor));

        // 品質ボーナスがある場合表示
        if (data.qualityBonus() > 0.0f) {
            int bonusPercent = (int) (data.qualityBonus() * 100);
            tooltipComponents.add(Component.translatable("tooltip.anvil.processed_material.quality_bonus",
                            bonusPercent)
                    .withStyle(ChatFormatting.AQUA));
        }

        // パーツ変換可能かどうかを表示
        if (data.canConvertToPart()) {
            tooltipComponents.add(Component.translatable("tooltip.anvil.processed_material.convertible")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.anvil.processed_material.needs_processing")
                    .withStyle(ChatFormatting.YELLOW));
        }
    }

    /**
     * 加工レベルに応じた色を取得
     */
    private ChatFormatting getProcessingLevelColor(ProcessingLevel level) {
        return switch (level) {
            case RAW -> ChatFormatting.DARK_GRAY;
            case REFINED -> ChatFormatting.GRAY;
            case FORGED -> ChatFormatting.WHITE;
            case POLISHED -> ChatFormatting.GREEN;
            case PERFECT -> ChatFormatting.BLUE;
            case MASTERWORK -> ChatFormatting.LIGHT_PURPLE;
        };
    }

    /**
     * グレードに応じた色を取得
     */
    private ChatFormatting getGradeColor(Grade grade) {
        return switch (grade) {
            case E -> ChatFormatting.DARK_GRAY;
            case D -> ChatFormatting.GRAY;
            case C -> ChatFormatting.WHITE;
            case B -> ChatFormatting.GREEN;
            case A -> ChatFormatting.BLUE;
            case S -> ChatFormatting.LIGHT_PURPLE;
            case SS -> ChatFormatting.GOLD;
            case SSS -> ChatFormatting.RED;
            case MAX -> ChatFormatting.AQUA;
        };
    }

    @Override
    public Component getName(ItemStack stack) {
        ProcessedMaterialData data = getMaterialData(stack);
        if (data != null) {
            // 素材名 + 加工レベル名
            Optional<Material> materialOpt = MaterialRegistry.getInstance().get(ResourceLocation.parse(data.materialId()));
            if (materialOpt.isPresent()) {
                Material material = materialOpt.get();
                ProcessingLevel level = data.getProcessingLevel();
                return Component.translatable("item.anvil.processed_material.named",
                        Component.translatable(material.getTranslationKey()),
                        Component.translatable(level.getTranslationKey()));
            }
        }
        // デフォルト名
        return Component.translatable("item.anvil.processed_material");
    }
}
