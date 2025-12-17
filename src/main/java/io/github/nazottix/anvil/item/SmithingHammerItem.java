package io.github.nazottix.anvil.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * 鍛冶ハンマーアイテム
 *
 * 鍛造ステーションで使用する消耗品ツールです。
 * 精錬素材(REFINED)を鍛造素材(FORGED)に変換する際に耐久力を消費します。
 *
 * 特徴:
 * - 耐久値を持つ（デフォルト250回使用可能）
 * - 鍛造ごとに1耐久消費
 * - 耐久0で破壊
 */
public class SmithingHammerItem extends Item {

    // デフォルト耐久値
    public static final int DEFAULT_DURABILITY = 250;

    /**
     * コンストラクタ
     */
    public SmithingHammerItem(Properties properties) {
        super(properties
                .durability(DEFAULT_DURABILITY)  // 耐久値設定
                .stacksTo(1)  // スタック不可
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        // 用途説明
        tooltipComponents.add(Component.translatable("tooltip.anvil.smithing_hammer.desc")
                .withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        // エンチャント不可
        return false;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        // 修理不可（新しいハンマーを作る必要あり）
        return false;
    }
}
