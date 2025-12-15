package io.github.nazottix.anvil.item;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.data.AnvilDataComponents;
import io.github.nazottix.anvil.skill.SkillEffect;
import io.github.nazottix.anvil.skill.jewel.JewelData;
import io.github.nazottix.anvil.skill.jewel.JewelRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * ジュエルアイテムクラス
 *
 * スキルツリーのジュエルソケットに装着可能なアイテム。
 * 各ジュエルはJewelDataを持ち、装着時にスキル効果を付与します。
 *
 * 仕様書参照: docs/03_スキルツリーシステム.md
 */
public class JewelItem extends Item {

    /**
     * JewelItemコンストラクタ
     *
     * @param properties アイテムプロパティ
     */
    public JewelItem(Properties properties) {
        super(properties
                // ジュエルは16個までスタック可能
                .stacksTo(16)
        );
    }

    // ============================================
    // ジュエルデータ操作
    // ============================================

    /**
     * ItemStackからジュエルIDを取得
     *
     * @param stack アイテムスタック
     * @return ジュエルID、存在しない場合はnull
     */
    public static ResourceLocation getJewelId(ItemStack stack) {
        return stack.get(AnvilDataComponents.JEWEL_ID.get());
    }

    /**
     * ItemStackにジュエルIDを設定
     *
     * @param stack アイテムスタック
     * @param jewelId ジュエルID
     */
    public static void setJewelId(ItemStack stack, ResourceLocation jewelId) {
        stack.set(AnvilDataComponents.JEWEL_ID.get(), jewelId);
    }

    /**
     * ItemStackからJewelDataを取得
     *
     * @param stack アイテムスタック
     * @return JewelData、存在しない場合はnull
     */
    public static JewelData getJewelData(ItemStack stack) {
        ResourceLocation id = getJewelId(stack);
        if (id == null) {
            return null;
        }
        // JewelRegistry.get()はOptionalを返すのでorElse(null)で取得
        return JewelRegistry.get(id).orElse(null);
    }

    /**
     * 特定のジュエルIDを持つItemStackを作成
     *
     * @param jewelId ジュエルID
     * @return ジュエルItemStack
     */
    public static ItemStack createJewelStack(ResourceLocation jewelId) {
        // AnvilItemsからJewelItemを取得
        ItemStack stack = new ItemStack(AnvilItems.JEWEL.get());
        setJewelId(stack, jewelId);
        return stack;
    }

    // ============================================
    // ツールチップ
    // ============================================

    /**
     * ツールチップを追加
     *
     * ジュエルの名前、レアリティ、効果を表示します。
     */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        JewelData data = getJewelData(stack);
        if (data == null) {
            // ジュエルデータがない場合
            tooltipComponents.add(Component.literal("Invalid Jewel")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        // レアリティ表示
        tooltipComponents.add(Component.translatable("jewel.anvil.rarity." + data.rarity().getId())
                .withStyle(Style.EMPTY.withColor(data.rarity().getColor())));

        // 空行
        tooltipComponents.add(Component.empty());

        // 効果一覧
        tooltipComponents.add(Component.translatable("tooltip.anvil.jewel.effects")
                .withStyle(ChatFormatting.GRAY));

        // レアリティ倍率を適用した効果を表示
        List<SkillEffect> scaledEffects = data.getScaledEffects();
        for (SkillEffect effect : scaledEffects) {
            Component effectLine = formatEffect(effect);
            tooltipComponents.add(effectLine);
        }

        // 半径効果がある場合
        if (data.hasRadiusEffect()) {
            tooltipComponents.add(Component.empty());
            tooltipComponents.add(Component.translatable("tooltip.anvil.jewel.radius", data.radiusEffect())
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        // 装着可能なツールタイプの制限がある場合
        if (!data.allowedToolTypes().isEmpty()) {
            tooltipComponents.add(Component.empty());
            tooltipComponents.add(Component.translatable("tooltip.anvil.jewel.allowed_tools")
                    .withStyle(ChatFormatting.DARK_GRAY));
            for (String toolType : data.allowedToolTypes()) {
                tooltipComponents.add(Component.literal("  - ")
                        .append(Component.translatable("tool.anvil." + toolType))
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    /**
     * スキル効果をフォーマット
     *
     * @param effect スキル効果
     * @return フォーマット済みComponent
     */
    private Component formatEffect(SkillEffect effect) {
        // 効果タイプに応じてフォーマット
        String operationSymbol = switch (effect.operation()) {
            case FLAT -> "+";
            case PERCENT -> "+";
            case MULTIPLY -> "x";
            case SPECIAL -> "★"; // 特殊効果
        };

        String valueSuffix = switch (effect.operation()) {
            case FLAT -> "";
            case PERCENT -> "%";
            case MULTIPLY -> "%";
            case SPECIAL -> ""; // 特殊効果は単位なし
        };

        // 値をフォーマット（小数点以下があれば表示）
        String valueStr;
        if (effect.value() == (int) effect.value()) {
            valueStr = String.valueOf((int) effect.value());
        } else {
            valueStr = String.format("%.1f", effect.value());
        }

        // 効果の色を決定
        ChatFormatting color = effect.value() >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;

        return Component.literal("  " + operationSymbol + valueStr + valueSuffix + " ")
                .withStyle(color)
                .append(Component.translatable("skill.anvil.stat." + effect.targetId())
                        .withStyle(ChatFormatting.WHITE));
    }

    /**
     * アイテム名を取得
     *
     * ジュエルデータに基づいた名前を返します。
     */
    @Override
    public Component getName(ItemStack stack) {
        JewelData data = getJewelData(stack);
        if (data != null) {
            return Component.translatable(data.nameKey())
                    .withStyle(Style.EMPTY.withColor(data.rarity().getColor()));
        }
        return super.getName(stack);
    }

    /**
     * アイテムにエンチャントの輝きを付けるかどうか
     *
     * ユニーク以上のレアリティは輝きを付ける
     */
    @Override
    public boolean isFoil(ItemStack stack) {
        JewelData data = getJewelData(stack);
        if (data != null) {
            return data.rarity() == JewelData.JewelRarity.UNIQUE
                    || data.rarity() == JewelData.JewelRarity.PRIMORDIAL;
        }
        return super.isFoil(stack);
    }
}
