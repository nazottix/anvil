package io.github.nazottix.anvil.item;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.guide.AnvilGuide;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * ANVILガイドブックアイテム
 *
 * 右クリックでGuideMEガイドブックを開きます。
 * GuideMEがインストールされていない場合は、メッセージを表示します。
 */
public class GuideBookItem extends Item {

    public GuideBookItem(Properties properties) {
        super(properties);
    }

    /**
     * 右クリック時の処理
     *
     * GuideMEが利用可能な場合はガイドを開き、
     * そうでない場合はメッセージを表示します。
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            if (AnvilGuide.isGuideMeAvailable()) {
                // GuideMEのガイドを開く（クライアント側で実行）
                AnvilGuide.openGuide();
            } else {
                // GuideMEが利用できない場合のメッセージ
                player.displayClientMessage(
                        Component.translatable("message.anvil.guideme_not_installed"),
                        true
                );
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /**
     * ツールチップを追加
     */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        // GuideMEが利用可能かどうかで異なるツールチップを表示
        if (AnvilGuide.isGuideMeAvailable()) {
            tooltip.add(Component.translatable("tooltip.anvil.guide_book.open"));
        } else {
            tooltip.add(Component.translatable("tooltip.anvil.guide_book.guideme_required"));
        }
    }
}
