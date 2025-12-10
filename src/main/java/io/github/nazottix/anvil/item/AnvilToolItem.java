package io.github.nazottix.anvil.item;

import io.github.nazottix.anvil.data.AnvilDataComponents;
import io.github.nazottix.anvil.data.component.AnvilToolData;
import io.github.nazottix.anvil.data.component.ToolPart;
import io.github.nazottix.anvil.tool.ToolType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * ANVILツールアイテムの基底クラス
 *
 * 全てのANVILツール（ピッケル、剣、斧など）の共通機能を提供します。
 * 各ツールタイプはこのクラスを継承して具体的な動作を実装します。
 *
 * 主な機能:
 * - Data Componentsを使用したツールデータの保存/読み込み
 * - ツールチップ表示（レベル、パーツ情報）
 * - 将来的にはステータス計算、特性発動なども追加予定
 *
 * 仕様書参照: docs/01_パーツ_素材システム.md
 */
public class AnvilToolItem extends Item {
    // このアイテムのツールタイプ
    private final ToolType toolType;

    /**
     * AnvilToolItemコンストラクタ
     *
     * @param toolType ツールタイプ
     * @param properties アイテムプロパティ
     */
    public AnvilToolItem(ToolType toolType, Properties properties) {
        super(properties
                // ツールは1スタック1個
                .stacksTo(1)
                // デフォルトのツールデータをコンポーネントとして設定
                .component(AnvilDataComponents.TOOL_DATA.get(), AnvilToolData.DEFAULT)
        );
        this.toolType = toolType;
    }

    /**
     * ツールタイプを取得
     *
     * @return このアイテムのToolType
     */
    public ToolType getToolType() {
        return toolType;
    }

    /**
     * ItemStackからAnvilToolDataを取得
     *
     * @param stack アイテムスタック
     * @return AnvilToolData、存在しない場合はデフォルト値
     */
    public static AnvilToolData getToolData(ItemStack stack) {
        AnvilToolData data = stack.get(AnvilDataComponents.TOOL_DATA.get());
        return data != null ? data : AnvilToolData.DEFAULT;
    }

    /**
     * ItemStackにAnvilToolDataを設定
     *
     * @param stack アイテムスタック
     * @param data 設定するツールデータ
     */
    public static void setToolData(ItemStack stack, AnvilToolData data) {
        stack.set(AnvilDataComponents.TOOL_DATA.get(), data);
    }

    /**
     * ツールチップを追加
     *
     * アイテムにカーソルを合わせた時に表示される情報を追加します。
     *
     * @param stack アイテムスタック
     * @param context ツールチップコンテキスト
     * @param tooltipComponents ツールチップコンポーネントのリスト
     * @param tooltipFlag ツールチップフラグ（詳細表示かどうか）
     */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        // ツールデータを取得
        AnvilToolData data = getToolData(stack);

        // ツールタイプを表示
        tooltipComponents.add(Component.translatable("tooltip.anvil.tool_type",
                        Component.translatable(toolType.getTranslationKey()))
                .withStyle(ChatFormatting.GRAY));

        // レベルを表示
        tooltipComponents.add(Component.translatable("tooltip.anvil.level", data.level())
                .withStyle(ChatFormatting.GREEN));

        // 経験値を表示
        tooltipComponents.add(Component.translatable("tooltip.anvil.xp", data.currentXp())
                .withStyle(ChatFormatting.AQUA));

        // パーツ情報を表示（Shiftキーで詳細表示）
        if (tooltipFlag.isAdvanced() || tooltipFlag.hasShiftDown()) {
            if (!data.parts().isEmpty()) {
                tooltipComponents.add(Component.translatable("tooltip.anvil.parts")
                        .withStyle(ChatFormatting.YELLOW));

                for (ToolPart part : data.parts()) {
                    // パーツ情報: [グレード] パーツタイプ - 素材
                    tooltipComponents.add(Component.literal("  ")
                            .append(Component.literal("[" + part.grade().toUpperCase() + "] ")
                                    .withStyle(ChatFormatting.LIGHT_PURPLE))
                            .append(Component.translatable("part.anvil." + part.partType()))
                            .append(Component.literal(" - "))
                            .append(Component.literal(part.materialId()))
                            .withStyle(ChatFormatting.GRAY));
                }
            }
        } else {
            // Shiftキーで詳細表示のヒント
            if (!data.parts().isEmpty()) {
                tooltipComponents.add(Component.translatable("tooltip.anvil.shift_for_details")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }
        }
    }

    /**
     * アイテムにエンチャントの輝きを付けるかどうか
     *
     * 高レベル（100以上）のツールは輝きを付ける
     *
     * @param stack アイテムスタック
     * @return 輝きを付ける場合true
     */
    @Override
    public boolean isFoil(ItemStack stack) {
        AnvilToolData data = getToolData(stack);
        return data.level() >= 100 || super.isFoil(stack);
    }
}
