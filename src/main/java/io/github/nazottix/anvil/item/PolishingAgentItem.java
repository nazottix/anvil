package io.github.nazottix.anvil.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * 研磨剤アイテム
 *
 * 研磨ステーションで使用する消耗品です。
 * 鍛造素材(FORGED)を研磨素材(POLISHED)に変換する際に品質ボーナスを付与します。
 *
 * 特徴:
 * - 消費アイテム（64スタック可能）
 * - 品質ボーナス値を持つ（グレードにより異なる）
 * - 研磨ごとに1つ消費
 */
public class PolishingAgentItem extends Item {

    // 研磨剤の品質（0.0-1.0）
    // この値が素材のqualityBonusに加算される
    private final float qualityValue;

    /**
     * コンストラクタ
     *
     * @param qualityValue 品質値（0.0-1.0）
     * @param properties アイテムプロパティ
     */
    public PolishingAgentItem(float qualityValue, Properties properties) {
        super(properties.stacksTo(64));
        this.qualityValue = Math.min(1.0f, Math.max(0.0f, qualityValue));
    }

    /**
     * 品質値を取得
     *
     * @return 品質値（0.0-1.0）
     */
    public float getQualityValue() {
        return qualityValue;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        // 用途説明
        tooltipComponents.add(Component.translatable("tooltip.anvil.polishing_agent.desc")
                .withStyle(ChatFormatting.GRAY));

        // 品質値を表示
        int qualityPercent = (int) (qualityValue * 100);
        ChatFormatting color = getQualityColor();
        tooltipComponents.add(Component.translatable("tooltip.anvil.polishing_agent.quality", qualityPercent)
                .withStyle(color));
    }

    /**
     * 品質に応じた色を取得
     */
    private ChatFormatting getQualityColor() {
        if (qualityValue >= 0.75f) {
            return ChatFormatting.GOLD;
        } else if (qualityValue >= 0.50f) {
            return ChatFormatting.LIGHT_PURPLE;
        } else if (qualityValue >= 0.25f) {
            return ChatFormatting.AQUA;
        } else {
            return ChatFormatting.WHITE;
        }
    }

    /**
     * 研磨剤かどうかチェック
     *
     * @param stack チェックするItemStack
     * @return 研磨剤の場合true
     */
    public static boolean isPolishingAgent(ItemStack stack) {
        return stack.getItem() instanceof PolishingAgentItem;
    }

    /**
     * ItemStackから品質値を取得
     *
     * @param stack 研磨剤ItemStack
     * @return 品質値、研磨剤でない場合は0
     */
    public static float getQualityValue(ItemStack stack) {
        if (stack.getItem() instanceof PolishingAgentItem agent) {
            return agent.getQualityValue();
        }
        return 0.0f;
    }
}
