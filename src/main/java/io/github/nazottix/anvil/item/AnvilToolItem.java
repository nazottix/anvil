package io.github.nazottix.anvil.item;

import io.github.nazottix.anvil.assembly.CalculatedStats;
import io.github.nazottix.anvil.client.ui.AnvilColors;
import io.github.nazottix.anvil.client.ui.ToolInfoRenderer;
import io.github.nazottix.anvil.data.AnvilDataComponents;
import io.github.nazottix.anvil.data.component.AnvilToolData;
import io.github.nazottix.anvil.data.component.ToolPart;
import io.github.nazottix.anvil.tool.ToolType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
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
     * Shiftキーで詳細表示（ステータス、パーツ情報、アフィックス情報）を表示します。
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
        CalculatedStats stats = getCalculatedStats(stack);

        // Shift押下または詳細モードの場合
        boolean showDetails = tooltipFlag.isAdvanced() || tooltipFlag.hasShiftDown();

        if (showDetails) {
            // 詳細ツールチップ（ToolInfoRendererを使用）
            tooltipComponents.addAll(ToolInfoRenderer.getDetailedTooltip(stack, toolType));

            // レアリティ表示（NORMALでない場合のみ）
            Component rarityComp = ToolInfoRenderer.getRarityComponent(data);
            if (rarityComp != null) {
                tooltipComponents.add(rarityComp);
            }

            // 空行
            tooltipComponents.add(Component.empty());

            // パーツ情報
            tooltipComponents.addAll(ToolInfoRenderer.getPartsTooltip(data));

            // アフィックス情報（存在する場合のみ）
            List<Component> affixTooltip = ToolInfoRenderer.getAffixesTooltip(data);
            if (!affixTooltip.isEmpty()) {
                tooltipComponents.add(Component.empty());
                tooltipComponents.addAll(affixTooltip);
            }
        } else {
            // 簡易ツールチップ
            // ツールタイプ
            tooltipComponents.add(Component.translatable("tooltip.anvil.tool_type",
                            Component.translatable(toolType.getTranslationKey()))
                    .withStyle(ChatFormatting.GRAY));

            // レアリティ表示（NORMALでない場合のみ）
            Component rarityComp = ToolInfoRenderer.getRarityComponent(data);
            if (rarityComp != null) {
                tooltipComponents.add(rarityComp);
            }

            // レベルをカラフルに表示
            tooltipComponents.add(Component.literal("Lv.")
                    .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.LEVEL_COLOR)))
                    .append(Component.literal(String.valueOf(data.level()))
                            .withStyle(ChatFormatting.WHITE)));

            // 主要ステータス（ツールタイプに応じて）
            addQuickStats(tooltipComponents, stats);

            // アフィックス数を簡易表示（存在する場合のみ）
            if (!data.affixes().isEmpty()) {
                int prefixCount = data.getPrefixCount();
                int suffixCount = data.getSuffixCount();
                tooltipComponents.add(Component.translatable("tooltip.anvil.affix_count", prefixCount, suffixCount)
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
            }

            // Shiftキーでの詳細表示ヒント
            tooltipComponents.add(Component.translatable("tooltip.anvil.shift_for_details")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    /**
     * 簡易ステータス表示を追加
     */
    private void addQuickStats(List<Component> tooltip, CalculatedStats stats) {
        switch (toolType) {
            case PICKAXE, AXE, SHOVEL, HOE -> {
                // 採掘ツール: 採掘速度
                tooltip.add(Component.literal("  ")
                        .append(Component.translatable("tooltip.anvil.mining_speed_short"))
                        .append(Component.literal(": "))
                        .append(Component.literal(String.format("%.1f", stats.miningSpeed()))
                                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.MINING_SPEED_COLOR)))));
            }
            case SWORD -> {
                // 剣: 攻撃力
                tooltip.add(Component.literal("  ")
                        .append(Component.translatable("tooltip.anvil.attack_damage_short"))
                        .append(Component.literal(": "))
                        .append(Component.literal(String.format("%.1f", stats.attackDamage()))
                                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ATTACK_DAMAGE_COLOR)))));
            }
            case BOW -> {
                // 弓: 引き速度
                tooltip.add(Component.literal("  ")
                        .append(Component.translatable("tooltip.anvil.draw_speed_short"))
                        .append(Component.literal(": "))
                        .append(Component.literal(String.format("%.1f", stats.drawSpeed()))
                                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ATTACK_SPEED_COLOR)))));
            }
            case FISHING_ROD -> {
                // 釣り竿: 釣り効率
                tooltip.add(Component.literal("  ")
                        .append(Component.translatable("tooltip.anvil.fishing_short"))
                        .append(Component.literal(": "))
                        .append(Component.literal(String.format("%.1f", stats.fishingEfficiency()))
                                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.TECH_CYAN)))));
            }
            case SHEARS -> {
                // ハサミ: 切断効率
                tooltip.add(Component.literal("  ")
                        .append(Component.translatable("tooltip.anvil.cutting_short"))
                        .append(Component.literal(": "))
                        .append(Component.literal(String.format("%.1f", stats.cuttingEfficiency()))
                                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.MINING_SPEED_COLOR)))));
            }
        }
    }

    /**
     * CalculatedStatsを取得
     */
    public static CalculatedStats getCalculatedStats(ItemStack stack) {
        CalculatedStats stats = stack.get(AnvilDataComponents.CALCULATED_STATS.get());
        return stats != null ? stats : CalculatedStats.DEFAULT;
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
