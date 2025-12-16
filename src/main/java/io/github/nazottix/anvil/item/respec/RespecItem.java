package io.github.nazottix.anvil.item.respec;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.data.AnvilDataComponents;
import io.github.nazottix.anvil.skill.SkillAllocation;
import io.github.nazottix.anvil.skill.SkillNode;
import io.github.nazottix.anvil.skill.SkillNodeType;
import io.github.nazottix.anvil.skill.SkillTree;
import io.github.nazottix.anvil.skill.SkillTreeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.*;

/**
 * リスペックアイテム基底クラス
 *
 * スキルツリーのスキルポイントをリスペック（再配分）するためのアイテム。
 * 使用するとメインハンド/オフハンドのANVILツールに対してリスペックを実行します。
 *
 * 仕様書参照: docs/08_入手_リスペックシステム.md
 */
public class RespecItem extends Item {

    // リスペックタイプ
    private final RespecType respecType;

    /**
     * コンストラクタ
     *
     * @param respecType リスペックタイプ
     * @param properties アイテムプロパティ
     */
    public RespecItem(RespecType respecType, Properties properties) {
        super(properties.stacksTo(16));
        this.respecType = respecType;
    }

    /**
     * リスペックタイプを取得
     */
    public RespecType getRespecType() {
        return respecType;
    }

    // ============================================
    // アイテム使用
    // ============================================

    /**
     * アイテムを使用
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.pass(heldItem);
        }

        // ANVILツールを探す（メインハンド優先、なければオフハンド）
        ItemStack toolStack = findAnvilTool(player, hand);
        if (toolStack.isEmpty()) {
            // ツールが見つからない
            player.displayClientMessage(
                    Component.translatable("message.anvil.respec.no_tool").withStyle(ChatFormatting.RED),
                    true
            );
            return InteractionResultHolder.fail(heldItem);
        }

        // スキル配分を取得
        if (!toolStack.has(AnvilDataComponents.SKILL_ALLOCATION.get())) {
            player.displayClientMessage(
                    Component.translatable("message.anvil.respec.no_allocation").withStyle(ChatFormatting.RED),
                    true
            );
            return InteractionResultHolder.fail(heldItem);
        }

        SkillAllocation allocation = toolStack.get(AnvilDataComponents.SKILL_ALLOCATION.get());

        // リスペック実行
        RespecResult result = executeRespec(allocation, toolStack);

        if (result.success()) {
            // 成功：配分を更新
            toolStack.set(AnvilDataComponents.SKILL_ALLOCATION.get(), result.newAllocation());

            // アイテムを消費
            if (!player.getAbilities().instabuild) {
                heldItem.shrink(1);
            }

            // 成功メッセージ
            player.displayClientMessage(
                    Component.translatable("message.anvil.respec.success", result.pointsRefunded())
                            .withStyle(ChatFormatting.GREEN),
                    true
            );

            ANVIL.LOGGER.info("リスペック成功: {} ポイントを返還", result.pointsRefunded());
            return InteractionResultHolder.success(heldItem);
        } else {
            // 失敗メッセージ
            player.displayClientMessage(
                    Component.translatable("message.anvil.respec.fail").withStyle(ChatFormatting.RED),
                    true
            );
            return InteractionResultHolder.fail(heldItem);
        }
    }

    /**
     * ANVILツールを探す
     */
    private ItemStack findAnvilTool(Player player, InteractionHand usedHand) {
        // 反対の手をチェック
        InteractionHand otherHand = usedHand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;

        ItemStack otherStack = player.getItemInHand(otherHand);
        if (otherStack.has(AnvilDataComponents.TOOL_DATA.get())) {
            return otherStack;
        }

        return ItemStack.EMPTY;
    }

    /**
     * リスペックを実行
     */
    private RespecResult executeRespec(SkillAllocation allocation, ItemStack toolStack) {
        // スキルツリーを取得
        SkillTree tree = SkillTreeRegistry.getForToolType(getToolType(toolStack));
        if (tree == null) {
            return new RespecResult(false, allocation, 0);
        }

        return switch (respecType) {
            case FULL -> executeFullRespec(allocation, tree);
            case PARTIAL -> executePartialRespec(allocation, tree, respecType.getPointsToRefund());
            case KEYSTONE_ONLY -> executeKeystoneRespec(allocation, tree);
        };
    }

    /**
     * 完全リスペック
     */
    private RespecResult executeFullRespec(SkillAllocation allocation, SkillTree tree) {
        int totalPoints = allocation.usedPoints();

        // ジュエルも全て取り外し
        SkillAllocation newAllocation = allocation.reset();

        return new RespecResult(true, newAllocation, totalPoints);
    }

    /**
     * 部分リスペック（指定ポイント分のノードを解除）
     */
    private RespecResult executePartialRespec(SkillAllocation allocation, SkillTree tree, int pointsToRefund) {
        if (allocation.usedPoints() == 0) {
            return new RespecResult(false, allocation, 0);
        }

        // 解除可能なノードを取得（ルート以外、末端から）
        List<ResourceLocation> removableNodes = findRemovableNodes(allocation, tree);
        if (removableNodes.isEmpty()) {
            return new RespecResult(false, allocation, 0);
        }

        int refundedPoints = 0;
        SkillAllocation newAllocation = allocation;

        // ポイント分のノードを解除
        for (ResourceLocation nodeId : removableNodes) {
            if (refundedPoints >= pointsToRefund) {
                break;
            }

            Optional<SkillNode> nodeOpt = tree.getNode(nodeId);
            if (nodeOpt.isPresent()) {
                SkillNode node = nodeOpt.get();
                int cost = node.getActualCost();

                // ジュエルソケットの場合、装着済みジュエルも取り外す
                if (node.type() == SkillNodeType.JEWEL_SOCKET && newAllocation.hasJewel(nodeId)) {
                    newAllocation = newAllocation.withRemovedJewel(nodeId);
                }

                newAllocation = newAllocation.withDeallocatedNode(nodeId, cost);
                refundedPoints += cost;
            }
        }

        return new RespecResult(refundedPoints > 0, newAllocation, refundedPoints);
    }

    /**
     * キーストーンのみリスペック
     */
    private RespecResult executeKeystoneRespec(SkillAllocation allocation, SkillTree tree) {
        int refundedPoints = 0;
        SkillAllocation newAllocation = allocation;

        // キーストーンノードを探して解除
        for (SkillNode node : tree.nodes()) {
            if (node.type() == SkillNodeType.KEYSTONE && allocation.isAllocated(node.id())) {
                int cost = node.getActualCost();
                newAllocation = newAllocation.withDeallocatedNode(node.id(), cost);
                refundedPoints += cost;
            }
        }

        return new RespecResult(refundedPoints > 0, newAllocation, refundedPoints);
    }

    /**
     * 解除可能なノードを取得（末端から順に）
     */
    private List<ResourceLocation> findRemovableNodes(SkillAllocation allocation, SkillTree tree) {
        List<ResourceLocation> removable = new ArrayList<>();
        Set<ResourceLocation> allocated = new HashSet<>(allocation.allocatedNodes());

        // 繰り返し末端ノードを探す
        boolean found;
        do {
            found = false;
            for (ResourceLocation nodeId : new ArrayList<>(allocated)) {
                Optional<SkillNode> nodeOpt = tree.getNode(nodeId);
                if (nodeOpt.isEmpty()) continue;

                SkillNode node = nodeOpt.get();

                // ルートは解除不可
                if (node.isRoot()) continue;

                // 他の解放済みノードの接続元になっていないかチェック
                boolean isLeaf = true;
                for (SkillNode connected : tree.getConnectedNodes(nodeId)) {
                    if (allocated.contains(connected.id()) && !connected.id().equals(nodeId)) {
                        // 接続先が解放済みで、その接続先がこのノード以外と繋がっていないなら末端ではない
                        boolean connectedHasOther = false;
                        for (SkillNode otherConn : tree.getConnectedNodes(connected.id())) {
                            if (allocated.contains(otherConn.id()) && !otherConn.id().equals(nodeId)) {
                                connectedHasOther = true;
                                break;
                            }
                        }
                        if (!connectedHasOther && !connected.isRoot()) {
                            // このノードを解除すると孤立するノードがある
                            isLeaf = false;
                            break;
                        }
                    }
                }

                if (isLeaf) {
                    removable.add(nodeId);
                    allocated.remove(nodeId);
                    found = true;
                }
            }
        } while (found && !allocated.isEmpty());

        return removable;
    }

    /**
     * ツールタイプを取得
     *
     * AnvilToolDataからツールタイプIDを取得します。
     * AnvilToolItemの場合はアイテムのToolTypeを使用し、
     * それ以外の場合はAnvilToolDataのtoolTypeを使用します。
     */
    private String getToolType(ItemStack toolStack) {
        // AnvilToolItemの場合、直接ToolTypeを取得
        if (toolStack.getItem() instanceof io.github.nazottix.anvil.item.AnvilToolItem anvilTool) {
            return anvilTool.getToolType().getId();
        }

        // それ以外の場合、AnvilToolDataからtoolTypeを取得
        if (toolStack.has(AnvilDataComponents.TOOL_DATA.get())) {
            io.github.nazottix.anvil.data.component.AnvilToolData toolData =
                    toolStack.get(AnvilDataComponents.TOOL_DATA.get());
            return toolData.toolType();
        }

        // フォールバック
        return "pickaxe";
    }

    // ============================================
    // ツールチップ
    // ============================================

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        // リスペックタイプに応じた説明
        tooltipComponents.add(Component.empty());

        switch (respecType) {
            case PARTIAL -> {
                tooltipComponents.add(Component.translatable("tooltip.anvil.respec.partial")
                        .withStyle(ChatFormatting.GRAY));
                tooltipComponents.add(Component.translatable("tooltip.anvil.respec.partial.points", respecType.getPointsToRefund())
                        .withStyle(ChatFormatting.YELLOW));
            }
            case FULL -> {
                tooltipComponents.add(Component.translatable("tooltip.anvil.respec.full")
                        .withStyle(ChatFormatting.GRAY));
                tooltipComponents.add(Component.translatable("tooltip.anvil.respec.full.warning")
                        .withStyle(ChatFormatting.RED));
            }
            case KEYSTONE_ONLY -> {
                tooltipComponents.add(Component.translatable("tooltip.anvil.respec.keystone")
                        .withStyle(ChatFormatting.GRAY));
                tooltipComponents.add(Component.translatable("tooltip.anvil.respec.keystone.info")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }

        // 使用方法
        tooltipComponents.add(Component.empty());
        tooltipComponents.add(Component.translatable("tooltip.anvil.respec.usage")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // 完全リスペックアイテムは輝きを付ける
        return respecType == RespecType.FULL || respecType == RespecType.KEYSTONE_ONLY;
    }

    // ============================================
    // 結果レコード
    // ============================================

    /**
     * リスペック結果
     */
    private record RespecResult(
            boolean success,
            SkillAllocation newAllocation,
            int pointsRefunded
    ) {}
}
