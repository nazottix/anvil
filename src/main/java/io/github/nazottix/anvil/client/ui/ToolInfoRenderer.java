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
 * Station Blockスタイルに統一されたデザイン。
 */
public final class ToolInfoRenderer {

    // ============================================
    // アイコン定数（Unicode文字）
    // ============================================

    /** 耐久値アイコン */
    private static final String ICON_DURABILITY = "🛡";
    /** 採掘速度アイコン */
    private static final String ICON_MINING_SPEED = "⛏";
    /** 攻撃力アイコン */
    private static final String ICON_ATTACK_DAMAGE = "⚔";
    /** 攻撃速度アイコン */
    private static final String ICON_ATTACK_SPEED = "💨";
    /** MOD容量アイコン */
    private static final String ICON_MOD_CAPACITY = "◆";
    /** レベルアイコン */
    private static final String ICON_LEVEL = "★";
    /** 経験値アイコン */
    private static final String ICON_XP = "✧";
    /** パーツアイコン */
    private static final String ICON_PARTS = "⚙";
    /** アフィックスアイコン */
    private static final String ICON_AFFIX = "✦";
    /** 弓引き速度アイコン */
    private static final String ICON_DRAW_SPEED = "🏹";
    /** 射程アイコン */
    private static final String ICON_RANGE = "➤";
    /** 釣り効率アイコン */
    private static final String ICON_FISHING = "🎣";
    /** 切断効率アイコン */
    private static final String ICON_CUTTING = "✂";

    // ============================================
    // セクション区切り線
    // ============================================

    /** 区切り線（短） */
    private static final String SEPARATOR_SHORT = "─────────";
    /** 区切り線（長） */
    private static final String SEPARATOR_LONG = "──────────────";

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

        // ツールタイプ（Station Blockスタイルの色）
        tooltip.add(Component.translatable("tooltip.anvil.tool_type",
                        Component.translatable(toolType.getTranslationKey()))
                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ANVIL_STEEL))));

        // レベルと経験値バー（グラデーション対応）
        tooltip.add(getLevelComponentEnhanced(toolData));

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

        // ツールタイプ（Station Blockスタイルの色）
        tooltip.add(Component.translatable("tooltip.anvil.tool_type",
                        Component.translatable(toolType.getTranslationKey()))
                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ANVIL_STEEL))));

        // レベルと経験値（グラデーション対応）
        tooltip.add(getLevelComponentEnhanced(toolData));

        // レベルアップまでの必要経験値
        tooltip.add(getXpToNextLevelComponent(toolData));

        // セクション区切り線
        tooltip.add(getSeparator());

        // ステータスヘッダー（アイコン付き）
        tooltip.add(Component.literal(ICON_PARTS + " ")
                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.MOLTEN_GOLD)))
                .append(Component.translatable("tooltip.anvil.stats")
                        .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.MOLTEN_GOLD)))));

        // 耐久値（アイコン付き）
        tooltip.add(getStatComponentWithIcon(ICON_DURABILITY, "tooltip.anvil.durability",
                String.valueOf(stats.durability()), AnvilColors.DURABILITY_COLOR));

        // ツールタイプに応じたステータス表示
        switch (toolType) {
            case PICKAXE, AXE, SHOVEL, HOE -> {
                // 採掘ツール
                tooltip.add(getStatComponentWithIcon(ICON_MINING_SPEED, "tooltip.anvil.mining_speed",
                        String.format("%.1f", stats.miningSpeed()), AnvilColors.MINING_SPEED_COLOR));
                tooltip.add(getStatComponentWithIcon("◈", "tooltip.anvil.mining_level",
                        getMiningLevelName(stats.miningLevel()), AnvilColors.MINING_SPEED_COLOR));
            }
            case SWORD -> {
                // 剣
                tooltip.add(getStatComponentWithIcon(ICON_ATTACK_DAMAGE, "tooltip.anvil.attack_damage",
                        String.format("%.1f", stats.attackDamage()), AnvilColors.ATTACK_DAMAGE_COLOR));
                tooltip.add(getStatComponentWithIcon(ICON_ATTACK_SPEED, "tooltip.anvil.attack_speed",
                        String.format("%.1f", stats.attackSpeed()), AnvilColors.ATTACK_SPEED_COLOR));
            }
            case BOW -> {
                // 弓
                tooltip.add(getStatComponentWithIcon(ICON_DRAW_SPEED, "tooltip.anvil.draw_speed",
                        String.format("%.1f", stats.drawSpeed()), AnvilColors.ATTACK_SPEED_COLOR));
                tooltip.add(getStatComponentWithIcon(ICON_RANGE, "tooltip.anvil.range",
                        String.format("%.1f", stats.range()), AnvilColors.MINING_SPEED_COLOR));
            }
            case FISHING_ROD -> {
                // 釣り竿
                tooltip.add(getStatComponentWithIcon(ICON_FISHING, "tooltip.anvil.fishing_efficiency",
                        String.format("%.1f", stats.fishingEfficiency()), AnvilColors.TECH_CYAN));
            }
            case SHEARS -> {
                // ハサミ
                tooltip.add(getStatComponentWithIcon(ICON_CUTTING, "tooltip.anvil.cutting_efficiency",
                        String.format("%.1f", stats.cuttingEfficiency()), AnvilColors.MINING_SPEED_COLOR));
            }
        }

        // MOD容量（アイコン付き）
        tooltip.add(getStatComponentWithIcon(ICON_MOD_CAPACITY, "tooltip.anvil.mod_capacity",
                String.valueOf(stats.modCapacity()), AnvilColors.ARCANE_PURPLE));

        return tooltip;
    }

    /**
     * パーツ情報ツールチップを生成（詳細ステータス付き）
     *
     * @param toolData ツールデータ
     * @return パーツ情報コンポーネントのリスト
     */
    public static List<Component> getPartsTooltip(AnvilToolData toolData) {
        List<Component> tooltip = new ArrayList<>();

        if (toolData.parts().isEmpty()) {
            tooltip.add(Component.translatable("tooltip.anvil.no_parts")
                    .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ANVIL_STEEL))));
            return tooltip;
        }

        // セクション区切り線
        tooltip.add(getSeparator());

        // パーツヘッダー（アイコン付き）
        tooltip.add(Component.literal(ICON_PARTS + " ")
                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.MOLTEN_GOLD)))
                .append(Component.translatable("tooltip.anvil.parts")
                        .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.MOLTEN_GOLD)))));

        MaterialRegistry registry = MaterialRegistry.getInstance();

        for (ToolPart part : toolData.parts()) {
            // グレードカラーを取得
            int gradeColor = AnvilColors.getGradeColor(part.grade());

            // 素材名を取得
            String materialName = part.materialId();
            Optional<Material> materialOpt = registry.get(part.materialId());
            if (materialOpt.isPresent()) {
                String id = materialOpt.get().getId().getPath();
                materialName = id.substring(0, 1).toUpperCase() + id.substring(1).replace("_", " ");
            }

            // パーツ行を構築（アイコン付き）
            MutableComponent partLine = Component.literal("  ");

            // グレード表示（色付き角括弧）
            partLine.append(Component.literal("[" + part.grade().toUpperCase() + "] ")
                    .withStyle(Style.EMPTY.withColor(gradeColor)));

            // パーツタイプ（Station Blockスタイル白色）
            partLine.append(Component.translatable("part.anvil." + part.partType())
                    .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ETHER_WHITE))));

            // 素材名（グレー）
            partLine.append(Component.literal(" - ")
                    .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ANVIL_STEEL))));
            partLine.append(Component.literal(materialName)
                    .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ANVIL_STEEL))));

            tooltip.add(partLine);

            // パーツの詳細ステータスを表示（素材のステータス）
            if (materialOpt.isPresent()) {
                Material material = materialOpt.get();
                List<Component> partStats = getPartDetailedStats(part, material);
                tooltip.addAll(partStats);
            }
        }

        return tooltip;
    }

    /**
     * パーツの詳細ステータスを取得
     *
     * @param part パーツ
     * @param material 素材
     * @return ステータスコンポーネントのリスト
     */
    private static List<Component> getPartDetailedStats(ToolPart part, Material material) {
        List<Component> stats = new ArrayList<>();

        String partType = part.partType();
        // インデント用のプレフィックス
        String indent = "    ";

        switch (partType) {
            case "head" -> {
                // ヘッドパーツのステータス
                var headStats = material.getHeadStats();
                if (headStats != null) {
                    stats.add(Component.literal(indent)
                            .append(Component.literal("耐久: ")
                                    .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ANVIL_STEEL))))
                            .append(Component.literal("+" + headStats.durability())
                                    .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.DURABILITY_COLOR))))
                            .append(Component.literal(" 採掘: ")
                                    .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ANVIL_STEEL))))
                            .append(Component.literal(String.format("+%.1f", headStats.miningSpeed()))
                                    .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.MINING_SPEED_COLOR)))));
                }
            }
            case "handle" -> {
                // ハンドルパーツのステータス
                var handleStats = material.getHandleStats();
                if (handleStats != null) {
                    stats.add(Component.literal(indent)
                            .append(Component.literal("耐久倍率: ")
                                    .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ANVIL_STEEL))))
                            .append(Component.literal(String.format("×%.2f", handleStats.durabilityMultiplier()))
                                    .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.DURABILITY_COLOR))))
                            .append(Component.literal(" 攻撃速度: ")
                                    .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ANVIL_STEEL))))
                            .append(Component.literal(String.format("%+.2f", handleStats.attackSpeedModifier()))
                                    .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ATTACK_SPEED_COLOR)))));
                }
            }
            case "binding" -> {
                // バインディングパーツのステータス
                var bindingStats = material.getBindingStats();
                if (bindingStats != null) {
                    stats.add(Component.literal(indent)
                            .append(Component.literal("特性増幅: ")
                                    .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ANVIL_STEEL))))
                            .append(Component.literal(String.format("×%.2f", bindingStats.traitAmplifier()))
                                    .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ARCANE_PURPLE)))));
                }
            }
        }

        return stats;
    }

    /**
     * レアリティ表示コンポーネントを生成
     *
     * @param toolData ツールデータ
     * @return レアリティコンポーネント、レアリティがNORMALの場合はnull
     */
    public static Component getRarityComponent(AnvilToolData toolData) {
        io.github.nazottix.anvil.affix.ToolRarity rarity =
                io.github.nazottix.anvil.affix.ToolRarity.fromId(toolData.rarity());

        if (rarity == io.github.nazottix.anvil.affix.ToolRarity.NORMAL) {
            return null;
        }

        // レアリティアイコンを追加
        String rarityIcon = getRarityIcon(rarity);
        return Component.literal(rarityIcon + " ")
                .withStyle(Style.EMPTY.withColor(rarity.getColor()))
                .append(Component.translatable("rarity.anvil." + rarity.getId())
                        .withStyle(Style.EMPTY.withColor(rarity.getColor())));
    }

    /**
     * レアリティに応じたアイコンを取得
     */
    private static String getRarityIcon(io.github.nazottix.anvil.affix.ToolRarity rarity) {
        return switch (rarity) {
            case NORMAL -> "";
            case MAGIC -> "✧";
            case RARE -> "✦";
            case UNIQUE -> "◆";
            case LEGACY -> "★";
        };
    }

    /**
     * アフィックス情報ツールチップを生成（詳細効果付き）
     *
     * @param toolData ツールデータ
     * @return アフィックス情報コンポーネントのリスト
     */
    public static List<Component> getAffixesTooltip(AnvilToolData toolData) {
        List<Component> tooltip = new ArrayList<>();

        java.util.List<io.github.nazottix.anvil.affix.AffixInstance> affixes = toolData.affixes();

        if (affixes == null || affixes.isEmpty()) {
            return tooltip;
        }

        // セクション区切り線
        tooltip.add(getSeparator());

        // アフィックスヘッダー（アイコン付き）
        tooltip.add(Component.literal(ICON_AFFIX + " ")
                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ARCANE_PURPLE)))
                .append(Component.translatable("tooltip.anvil.affixes")
                        .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ARCANE_PURPLE)))));

        // プレフィックスを先に表示
        for (io.github.nazottix.anvil.affix.AffixInstance instance : affixes) {
            if (instance.type() == io.github.nazottix.anvil.affix.AffixType.PREFIX) {
                tooltip.add(getAffixLineEnhanced(instance));
            }
        }

        // サフィックスを後に表示
        for (io.github.nazottix.anvil.affix.AffixInstance instance : affixes) {
            if (instance.type() == io.github.nazottix.anvil.affix.AffixType.SUFFIX) {
                tooltip.add(getAffixLineEnhanced(instance));
            }
        }

        return tooltip;
    }

    /**
     * 単一アフィックス行を生成（詳細効果付き）
     */
    private static Component getAffixLineEnhanced(io.github.nazottix.anvil.affix.AffixInstance instance) {
        io.github.nazottix.anvil.affix.Affix affix = instance.affix();
        io.github.nazottix.anvil.affix.AffixEffect effect = instance.effect();
        io.github.nazottix.anvil.affix.AffixTier tier = instance.tier();

        if (affix == null || effect == null) {
            return Component.literal("  ??? 不明なアフィックス")
                    .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ANVIL_STEEL)));
        }

        MutableComponent line = Component.literal("  ");

        // ティアアイコン
        String tierIcon = getTierIcon(tier.getTier());
        int tierColor = tier.getColor();

        line.append(Component.literal(tierIcon + " ")
                .withStyle(Style.EMPTY.withColor(tierColor)));

        // アフィックス名
        line.append(Component.translatable(affix.getTranslationKey())
                .withStyle(Style.EMPTY.withColor(tierColor)));

        // ティア表示
        line.append(Component.literal(" T" + tier.getTier())
                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ANVIL_STEEL))));

        // 効果値（色分け：正は緑、負は赤）
        line.append(Component.literal(": ")
                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ANVIL_STEEL))));

        String effectDisplay = effect.getDisplayValue();
        int effectColor = effect.value() >= 0 ? AnvilColors.DURABILITY_COLOR : AnvilColors.ATTACK_DAMAGE_COLOR;
        line.append(Component.literal(effectDisplay)
                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(effectColor))));

        // 影響するステータスを表示
        line.append(Component.literal(" (")
                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ANVIL_STEEL))));
        line.append(Component.translatable(effect.getStatTranslationKey())
                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.TECH_CYAN)).withItalic(true)));
        line.append(Component.literal(")")
                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ANVIL_STEEL))));

        return line;
    }

    /**
     * ティアに応じたアイコンを取得
     */
    private static String getTierIcon(int tier) {
        return switch (tier) {
            case 1 -> "◇";
            case 2 -> "◆";
            case 3 -> "✦";
            case 4 -> "✧";
            case 5 -> "★";
            default -> "·";
        };
    }

    // ============================================
    // ヘルパーメソッド
    // ============================================

    /**
     * セクション区切り線を取得
     */
    private static Component getSeparator() {
        return Component.literal(SEPARATOR_SHORT)
                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ANVIL_STEEL)));
    }

    /**
     * 強化版レベルコンポーネントを生成（グラデーション対応）
     */
    private static Component getLevelComponentEnhanced(AnvilToolData toolData) {
        long nextLevelXp = calculateXpForLevel(toolData.level() + 1);
        float progress = (float) toolData.currentXp() / nextLevelXp;

        // レベルアイコンと数値
        MutableComponent levelComp = Component.literal(ICON_LEVEL + " Lv.")
                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.LEVEL_COLOR)));
        levelComp.append(Component.literal(String.valueOf(toolData.level()))
                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ETHER_WHITE)).withBold(true)));

        // 経験値バー（グラデーション）
        levelComp.append(Component.literal(" ")
                .withStyle(ChatFormatting.RESET));
        levelComp.append(getGradientProgressBar(progress, 10));

        // パーセンテージ
        levelComp.append(Component.literal(" " + (int)(progress * 100) + "%")
                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.XP_COLOR))));

        return levelComp;
    }

    /**
     * レベルアップまでの必要経験値コンポーネントを生成
     */
    private static Component getXpToNextLevelComponent(AnvilToolData toolData) {
        long nextLevelXp = calculateXpForLevel(toolData.level() + 1);
        long remaining = nextLevelXp - toolData.currentXp();

        return Component.literal("  " + ICON_XP + " ")
                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.XP_COLOR)))
                .append(Component.translatable("tooltip.anvil.xp_to_next",
                                formatNumber(toolData.currentXp()), formatNumber(nextLevelXp))
                        .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ANVIL_STEEL))))
                .append(Component.literal(" (残り: " + formatNumber(remaining) + ")")
                        .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.XP_COLOR))));
    }

    /**
     * グラデーションプログレスバーを生成
     */
    private static Component getGradientProgressBar(float progress, int length) {
        int filled = (int) (progress * length);
        int empty = length - filled;

        MutableComponent bar = Component.literal("");

        // グラデーション色配列（暗い→明るい）
        int[] gradientColors = {
                0xFF0D4F8C, // 濃い青
                0xFF0891B2, // ティール
                0xFF06B6D4, // シアン
                0xFF22D3EE, // 明るいシアン
                0xFF67E8F9  // 非常に明るいシアン
        };

        // 塗りつぶし部分（グラデーション）
        for (int i = 0; i < filled; i++) {
            int colorIndex = (int) ((float) i / length * (gradientColors.length - 1));
            colorIndex = Math.min(colorIndex, gradientColors.length - 1);
            bar.append(Component.literal("█")
                    .withStyle(Style.EMPTY.withColor(gradientColors[colorIndex])));
        }

        // 空の部分
        if (empty > 0) {
            bar.append(Component.literal("░".repeat(empty))
                    .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.VOID_BLACK))));
        }

        return bar;
    }

    /**
     * プログレスバーを生成（後方互換性）
     */
    private static Component getProgressBar(float progress, int length) {
        return getGradientProgressBar(progress, length);
    }

    /**
     * アイコン付きステータス行コンポーネントを生成
     */
    private static Component getStatComponentWithIcon(String icon, String translationKey, String value, int color) {
        MutableComponent comp = Component.literal("  ");
        comp.append(Component.literal(icon + " ")
                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(color))));
        comp.append(Component.translatable(translationKey)
                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ANVIL_STEEL))));
        comp.append(Component.literal(": ")
                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(AnvilColors.ANVIL_STEEL))));
        comp.append(Component.literal(value)
                .withStyle(Style.EMPTY.withColor(AnvilColors.toRGB(color))));
        return comp;
    }

    /**
     * ステータス行コンポーネントを生成（後方互換性）
     */
    private static Component getStatComponent(String translationKey, String value, int color) {
        return getStatComponentWithIcon("·", translationKey, value, color);
    }

    /**
     * レベルコンポーネントを生成（後方互換性）
     */
    private static Component getLevelComponent(AnvilToolData toolData) {
        return getLevelComponentEnhanced(toolData);
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
     * レベルに必要な経験値を計算
     */
    private static long calculateXpForLevel(int level) {
        // 計算式: level^2 * 100
        return (long) level * level * 100;
    }

    /**
     * 数値をフォーマット（K, Mなどの単位付き）
     */
    private static String formatNumber(long number) {
        if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000.0);
        }
        return String.valueOf(number);
    }

    // コンストラクタを非公開
    private ToolInfoRenderer() {}
}
