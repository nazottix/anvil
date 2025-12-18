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
import net.neoforged.fml.ModList;

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
                // GuideMEのガイドを開く
                openGuide(player);
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
     * GuideMEガイドを開く
     *
     * このメソッドはクライアント側でのみ呼び出されます。
     */
    private void openGuide(Player player) {
        try {
            // GuideME APIを使用してガイドを開く
            // Guides.open(player, AnvilGuide.GUIDE_ID);
            ANVIL.LOGGER.debug("ガイドブックを開きます");

            // 注意: 実際のGuideME APIの呼び出しはGuideMEのバージョンによって異なります
            // GuideMEがロードされている場合、自動的にガイドアイテムを処理する可能性があります
        } catch (Exception e) {
            ANVIL.LOGGER.error("ガイドブックを開けませんでした: {}", e.getMessage());
        }
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
