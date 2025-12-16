package io.github.nazottix.anvil.item;

import io.github.nazottix.anvil.data.AnvilDataComponents;
import io.github.nazottix.anvil.data.component.ToolPart;
import io.github.nazottix.anvil.material.Grade;
import io.github.nazottix.anvil.material.Material;
import io.github.nazottix.anvil.material.MaterialRegistry;
import io.github.nazottix.anvil.tool.PartType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Optional;

/**
 * パーツアイテム
 *
 * ツールを構成するパーツのアイテムクラスです。
 * 各パーツは素材（Material）、パーツタイプ（PartType）、グレード（Grade）を持ちます。
 *
 * パーツはToolStationでツールに組み立てられます。
 * 素材とグレードによってツールの最終ステータスが決まります。
 *
 * 仕様書参照: docs/01_パーツ_素材システム.md
 */
public class PartItem extends Item {

    // このアイテムのパーツタイプ
    private final PartType partType;

    /**
     * コンストラクタ
     *
     * @param partType パーツタイプ
     * @param properties アイテムプロパティ
     */
    public PartItem(PartType partType, Properties properties) {
        super(properties.stacksTo(64));
        this.partType = partType;
    }

    /**
     * パーツタイプを取得
     */
    public PartType getPartType() {
        return partType;
    }

    // ============================================
    // ToolPartデータ操作
    // ============================================

    /**
     * ItemStackからToolPartデータを取得
     *
     * @param stack アイテムスタック
     * @return ToolPartデータ、なければnull
     */
    public static ToolPart getPartData(ItemStack stack) {
        if (stack.isEmpty()) return null;
        return stack.get(AnvilDataComponents.PART_DATA.get());
    }

    /**
     * ItemStackにToolPartデータを設定
     *
     * @param stack アイテムスタック
     * @param part ToolPartデータ
     */
    public static void setPartData(ItemStack stack, ToolPart part) {
        stack.set(AnvilDataComponents.PART_DATA.get(), part);
    }

    /**
     * 素材とグレードを指定してパーツItemStackを作成
     *
     * @param partType パーツタイプ
     * @param materialId 素材ID
     * @param grade グレード
     * @return 作成されたItemStack
     */
    public static ItemStack createPartStack(PartType partType, String materialId, Grade grade) {
        // パーツアイテムを取得
        Item partItem = AnvilItems.getPartItem(partType);
        if (partItem == null) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(partItem);
        ToolPart data = new ToolPart(materialId, partType.getId(), grade.getId());
        setPartData(stack, data);
        return stack;
    }

    /**
     * 素材IDを指定してパーツItemStackを作成（デフォルトグレードC）
     */
    public static ItemStack createPartStack(PartType partType, String materialId) {
        return createPartStack(partType, materialId, Grade.C);
    }

    /**
     * ItemStackが有効なパーツデータを持つか確認
     */
    public static boolean hasValidPartData(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof PartItem)) {
            return false;
        }
        ToolPart data = getPartData(stack);
        return data != null && data.materialId() != null && !data.materialId().isEmpty();
    }

    // ============================================
    // ツールチップ
    // ============================================

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        ToolPart data = getPartData(stack);
        if (data == null) {
            // データがない場合
            tooltipComponents.add(Component.translatable("tooltip.anvil.part.no_material")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        // 素材名を表示
        // MaterialRegistryのインスタンスから素材を取得
        Optional<Material> materialOpt = MaterialRegistry.getInstance().get(ResourceLocation.parse(data.materialId()));
        if (materialOpt.isPresent()) {
            Material material = materialOpt.get();
            // 素材名（素材の色で表示）
            int color = material.getPrimaryColor();
            tooltipComponents.add(Component.translatable("tooltip.anvil.part.material",
                            Component.translatable(material.getTranslationKey()))
                    .withStyle(style -> style.withColor(color)));
        } else {
            // 素材が見つからない
            tooltipComponents.add(Component.translatable("tooltip.anvil.part.material",
                            Component.literal(data.materialId()))
                    .withStyle(ChatFormatting.GRAY));
        }

        // グレードを表示
        Grade grade = Grade.fromId(data.grade());
        if (grade != null) {
            ChatFormatting gradeColor = getGradeColor(grade);
            tooltipComponents.add(Component.translatable("tooltip.anvil.part.grade",
                            Component.translatable("grade.anvil." + grade.getId()))
                    .withStyle(gradeColor));
        }

        // パーツタイプを表示
        tooltipComponents.add(Component.translatable("tooltip.anvil.part.type",
                        Component.translatable(partType.getTranslationKey()))
                .withStyle(ChatFormatting.DARK_GRAY));
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
        ToolPart data = getPartData(stack);
        if (data != null) {
            // 素材名 + パーツタイプ名
            // MaterialRegistryのインスタンスから素材を取得
            Optional<Material> materialOpt = MaterialRegistry.getInstance().get(ResourceLocation.parse(data.materialId()));
            if (materialOpt.isPresent()) {
                Material material = materialOpt.get();
                return Component.translatable("item.anvil.part.named",
                        Component.translatable(material.getTranslationKey()),
                        Component.translatable(partType.getTranslationKey()));
            }
        }
        // デフォルト名
        return Component.translatable(partType.getTranslationKey());
    }
}
