package io.github.nazottix.anvil.client.ui;

import io.github.nazottix.anvil.assembly.CalculatedStats;
import io.github.nazottix.anvil.data.AnvilDataComponents;
import io.github.nazottix.anvil.data.component.AnvilToolData;
import io.github.nazottix.anvil.data.component.ToolPart;
import io.github.nazottix.anvil.material.Grade;
import io.github.nazottix.anvil.material.Material;
import io.github.nazottix.anvil.material.MaterialRegistry;
import io.github.nazottix.anvil.tool.ToolType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ツール情報レンダラー
 *
 * ANVILツールの情報をUI用に整形して提供します。
 * ツールチップ、詳細画面、ステーション画面などで使用されます。
 */
public final class ToolInfoRenderer {

    // ============================================
    // ツールチップ生成
    // ============================================

    /**
     * 基本ツールチップを生成
     *
     * @param stack アイテムスタック
     * @param toolType ツールタイプ
     * @return ツールチップコンポーネントのリスト
     */
    public static List<Component> getBasicTooltip(ItemStack stack, ToolType toolType) {
        List<Component> tooltip = new ArrayList<>();

        // ツールデータを取得
        AnvilToolData toolData = stack.get(AnvilDataComponents.TOOL_DATA.get());
        if (toolData == null) {
            toolData = AnvilToolData.DEFAULT;
        }

        // ツールタイプ
        tooltip.add(Component.translatable("tooltip.anvil.tool_type",
                        Component.translatable(toolType.getTranslationKey()))
                .withStyle(ChatFormatting.GRAY));

        // レベルと経験値バー
        tooltip.add(getLevelComponent(toolData));

        return tooltip;
    }

    /**
     * 詳細ツールチップを生成（ステータス情報を含む）
     *
     * @param stack アイテムスタック
     * @param toolType ツールタイプ
     * @return ツールチップコンポーネントのリスト
     */
    public static List<Component> getDetailedTooltip(ItemStack stack, ToolType toolType) {
        List<Component> tooltip = new ArrayList<>();

        // ツールデータを取得
        AnvilToolData toolData = stack.get(AnvilDataComponents.TOOL_DATA.get());
        CalculatedStats stats = stack.get(AnvilDataComponents.CALCULATED_STATS.get());

        if (toolData == null) {
            toolData = AnvilToolData.DEFAULT;
        }
        if (stats == null) {
            stats = CalculatedStats.DEFAULT;
        }

        // ツールタイプ
        tooltip.add(Component.translatable("tooltip.anvil.tool_type",
                        Component.translatable(toolType.getTranslationKey()))
                .withStyle(ChatFormatting.GRAY));

        // レベルと経験値
        tooltip.add(getLevelComponent(toolData));

        // 空行
        tooltip.add(Component.empty());

        // ステータス
        tooltip.add(Component.translatable("tooltip.anvil.stats")
                .withStyle(ChatFormatting.GOLD));

        // 耐久値
        tooltip.add(getStatComponent("tooltip.anvil.durability",
                String.valueOf(stats.durability()), AnvilColors.DURABILITY_COLOR));

        // ツールタイプに応じたステータス表示
        switch (toolType) {
            case PICKAXE, AXE, SHOVEL, HOE -> {
                // 採掘ツール
                tooltip.add(getStatComponent("tooltip.anvil.mining_speed",
                        String.format("%.1f", stats.miningSpeed()), AnvilColors.MINING_SPEED_COLOR));
                tooltip.add(getStatComponent("tooltip.anvil.mining_level",
                        getMiningLevelName(stats.miningLevel()), AnvilColors.MINING_SPEED_COLOR));
            }
            case SWORD -> {
                // 剣
                tooltip.add(getStatComponent("tooltip.anvil.attack_damage",
                        String.format("%.1f", stats.attackDamage()), AnvilColors.ATTACK_DAMAGE_COLOR));
                tooltip.add(getStatComponent("tooltip.anvil.attack_speed",
                        String.format("%.1f", stats.attackSpeed()), AnvilColors.ATTACK_SPEED_COLOR));
            }
            case BOW -> {
                // 弓
                tooltip.add(getStatComponent("tooltip.anvil.draw_speed",
                        String.format("%.1f", stats.drawSpeed()), AnvilColors.ATTACK_SPEED_COLOR));
                tooltip.add(getStatComponent("tooltip.anvil.range",
                        String.format("%.1f", stats.range()), AnvilColors.MINING_SPEED_COLOR));
            }
            case FISHING_ROD -> {
                // 釣り竿
                tooltip.add(getStatComponent("tooltip.anvil.fishing_efficiency",
                        String.format("%.1f", stats.fishingEfficiency()), AnvilColors.TECH_CYAN));
            }
            case SHEARS -> {
                // ハサミ
                tooltip.add(getStatComponent("tooltip.anvil.cutting_efficiency",
                        String.format("%.1f", stats.cuttingEfficiency()), AnvilColors.MINING_SPEED_COLOR));
            }
        }

        // MOD容量
        tooltip.add(getStatComponent("tooltip.anvil.mod_capacity",
                String.valueOf(stats.modCapacity()), AnvilColors.ARCANE_PURPLE));

        return tooltip;
    }

    /**
     * パーツ情報ツールチップを生成
     *
     * @param toolData ツールデータ
     * @return パーツ情報コンポーネントのリスト
     */
    public static List<Component> getPartsTooltip(AnvilToolData toolData) {
        List<Component> tooltip = new ArrayList<>();

        if (toolData.parts().isEmpty()) {
            tooltip.add(Component.translatable("tooltip.anvil.no_parts")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return tooltip;
        }

        tooltip.add(Component.translatable("tooltip.anvil.parts")
                .withStyle(ChatFormatting.YELLOW));

        MaterialRegistry registry = MaterialRegistry.getInstance();

        for (ToolPart part : toolData.parts()) {
            // グレードカラーを取得
            int gradeColor = AnvilColors.getGradeColor(part.grade());

            // 素材名を取得（翻訳キーから素材ID部分を抽出）
            String materialName = part.materialId();
            Optional<Material> materialOpt = registry.get(part.materialId());
            if (materialOpt.isPresent()) {
                // 素材IDから表示名を生成（例: anvil:iron -> Iron）
                String id = materialOpt.get().getId().getPath();
                materialName = id.substring(0, 1).toUpperCase() + id.substring(1).replace("_", " ");
            }

            // パーツ行を構築
            MutableComponent partLine = Component.literal("  ");

            // グレード表示
            partLine.append(Component.literal("[" + part.grade().toUpperCase() + "] ")
                    .withStyle(Style.EMPTY.withColor(gradeColor)));

            // パーツタイプ
            partLine.append(Component.translatable("part.anvil." + part.partType())
                    .withStyle(ChatFormatting.WHITE));

            // 素材名
            partLine.append(Component.literal(" - ")
                    .withStyle(ChatFormatting.DARK_GRAY));
            partLine.append(Component.literal(materialName)
                    .withStyle(ChatFormatting.GRAY));

            tooltip.add(partLine);
        }

        return tooltip;
    }

    // ============================================
    // ヘルパーメソッド
    // ============================================

    /**
     * レベルコンポーネントを生成
     */
    private static Component getLevelComponent(AnvilToolData toolData) {
        // 次のレベルまでの必要経験値を計算（仮実装）
        long nextLevelXp = calculateXpForLevel(toolData.level() + 1);
        float progress = (float) toolData.currentXp() / nextLevelXp;

        // レベル表示
        MutableComponent levelComp = Component.literal("Lv.")
                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.LEVEL_COLOR)));
        levelComp.append(Component.literal(String.valueOf(toolData.level()))
                .withStyle(ChatFormatting.WHITE));

        // 経験値バー
        levelComp.append(Component.literal(" ")
                .withStyle(ChatFormatting.RESET));
        levelComp.append(getProgressBar(progress, 10));

        // パーセンテージ
        levelComp.append(Component.literal(" " + (int)(progress * 100) + "%")
                .withStyle(ChatFormatting.GRAY));

        return levelComp;
    }

    /**
     * プログレスバーを生成
     */
    private static Component getProgressBar(float progress, int length) {
        int filled = (int) (progress * length);
        int empty = length - filled;

        MutableComponent bar = Component.literal("");

        // 塗りつぶし部分
        if (filled > 0) {
            bar.append(Component.literal("█".repeat(filled))
                    .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.XP_COLOR))));
        }

        // 空の部分
        if (empty > 0) {
            bar.append(Component.literal("░".repeat(empty))
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        return bar;
    }

    /**
     * ステータス行コンポーネントを生成
     */
    private static Component getStatComponent(String translationKey, String value, int color) {
        MutableComponent comp = Component.literal("  ");
        comp.append(Component.translatable(translationKey)
                .withStyle(ChatFormatting.GRAY));
        comp.append(Component.literal(": ")
                .withStyle(ChatFormatting.DARK_GRAY));
        comp.append(Component.literal(value)
                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(color))));
        return comp;
    }

    /**
     * 採掘レベル名を取得
     */
    private static String getMiningLevelName(int level) {
        return switch (level) {
            case 0 -> "Wood";
            case 1 -> "Stone";
            case 2 -> "Iron";
            case 3 -> "Diamond";
            case 4 -> "Netherite";
            case 5 -> "Legendary";
            case 6 -> "Mythic";
            default -> "Unknown";
        };
    }

    /**
     * レベルに必要な経験値を計算（仮実装）
     */
    private static long calculateXpForLevel(int level) {
        // 仮の計算式: level^2 * 100
        return (long) level * level * 100;
    }

    // コンストラクタを非公開
    private ToolInfoRenderer() {}
}
