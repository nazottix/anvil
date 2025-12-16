package io.github.nazottix.anvil.menu;

import io.github.nazottix.anvil.data.AnvilDataComponents;
import io.github.nazottix.anvil.data.component.AnvilToolData;
import io.github.nazottix.anvil.item.JewelItem;
import io.github.nazottix.anvil.skill.*;
import io.github.nazottix.anvil.skill.jewel.JewelData;
import io.github.nazottix.anvil.skill.jewel.JewelRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * スキルツリーメニュー
 *
 * ツールのスキルツリー画面のサーバー側ロジック。
 * Path of Exile風のスキルツリーシステムを実装。
 *
 * スロット構成:
 * - 0: ツール入力スロット（スキル配分対象）
 * - 1-36: プレイヤーインベントリ + ホットバー
 *
 * 仕様書参照: docs/03_スキルツリーシステム.md
 */
public class SkillTreeMenu extends AbstractContainerMenu {

    // コンテナ（ツールスロット用）
    private final Container toolContainer;

    // 同期データ
    // 0: 利用可能スキルポイント
    // 1: 使用済みスキルポイント
    private final ContainerData data;

    // 現在編集中のスキル配分
    private SkillAllocation editingAllocation;

    // 現在のスキルツリー
    private SkillTree currentTree;

    /**
     * クライアント側コンストラクタ
     */
    public SkillTreeMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(1), new SimpleContainerData(2));
    }

    /**
     * サーバー側コンストラクタ
     */
    public SkillTreeMenu(int containerId, Inventory playerInventory, Container toolContainer, ContainerData data) {
        super(AnvilMenuTypes.SKILL_TREE.get(), containerId);
        this.toolContainer = toolContainer;
        this.data = data;

        checkContainerSize(toolContainer, 1);
        checkContainerDataCount(data, 2);

        addDataSlots(data);

        // ツール入力スロット - 画面左下
        this.addSlot(new ToolSlot(toolContainer, 0, 20, 200));

        // プレイヤーインベントリ
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 48 + col * 18, 174 + row * 18));
            }
        }

        // ホットバー
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 48 + col * 18, 232));
        }

        // 初期化
        this.editingAllocation = SkillAllocation.EMPTY;
        this.currentTree = null;
    }

    // ============================================
    // ツール変更検知
    // ============================================

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);

        ItemStack toolStack = toolContainer.getItem(0);
        if (!toolStack.isEmpty() && toolStack.has(AnvilDataComponents.TOOL_DATA.get())) {
            // ツールデータからツールタイプを取得してスキルツリーを設定
            AnvilToolData toolData = toolStack.get(AnvilDataComponents.TOOL_DATA.get());

            // ツールタイプを取得
            // AnvilToolItemの場合は直接取得、それ以外はAnvilToolDataから取得
            String toolType;
            if (toolStack.getItem() instanceof io.github.nazottix.anvil.item.AnvilToolItem anvilTool) {
                toolType = anvilTool.getToolType().getId();
            } else {
                toolType = toolData.toolType();
            }
            this.currentTree = SkillTreeRegistry.getForToolType(toolType);

            // スキル配分を読み込み
            if (toolStack.has(AnvilDataComponents.SKILL_ALLOCATION.get())) {
                this.editingAllocation = toolStack.get(AnvilDataComponents.SKILL_ALLOCATION.get());
            } else if (currentTree != null) {
                // 新規作成
                this.editingAllocation = SkillAllocation.create(currentTree.id());
            }

            // スキルポイントを更新
            updateSkillPoints();
        } else {
            this.editingAllocation = SkillAllocation.EMPTY;
            this.currentTree = null;
            data.set(0, 0);
            data.set(1, 0);
        }
    }

    /**
     * スキルポイントを更新
     */
    private void updateSkillPoints() {
        int level = getToolLevel();
        int totalPoints = level; // レベル = スキルポイント
        int usedPoints = editingAllocation.usedPoints();

        data.set(0, totalPoints - usedPoints); // 利用可能
        data.set(1, usedPoints); // 使用済み
    }

    // ============================================
    // スキル操作
    // ============================================

    /**
     * ノードを解放
     *
     * @param nodeId 解放するノードID
     * @return 成功した場合true
     */
    public boolean allocateNode(ResourceLocation nodeId) {
        if (currentTree == null) {
            return false;
        }

        Optional<SkillNode> nodeOpt = currentTree.getNode(nodeId);
        if (nodeOpt.isEmpty()) {
            return false;
        }

        SkillNode node = nodeOpt.get();

        // 既に解放済み
        if (editingAllocation.isAllocated(nodeId)) {
            return false;
        }

        // ポイントが足りない
        int cost = node.getActualCost();
        if (getAvailablePoints() < cost) {
            return false;
        }

        // 接続チェック（ルートまたは隣接ノードが解放済み）
        if (!canAllocate(nodeId)) {
            return false;
        }

        // 解放条件チェック
        SkillNode.NodeRequirements req = node.requirements();
        if (getToolLevel() < req.minLevel()) {
            return false;
        }
        if (editingAllocation.getAllocatedCount() < req.prerequisiteNodes()) {
            return false;
        }
        for (ResourceLocation reqNode : req.requiredNodes()) {
            if (!editingAllocation.isAllocated(reqNode)) {
                return false;
            }
        }

        // 解放
        editingAllocation = editingAllocation.withAllocatedNode(nodeId, cost);
        updateSkillPoints();

        return true;
    }

    /**
     * ノードを解放解除（リスペック）
     *
     * @param nodeId 解除するノードID
     * @return 成功した場合true
     */
    public boolean deallocateNode(ResourceLocation nodeId) {
        if (currentTree == null) {
            return false;
        }

        // 解放されていない
        if (!editingAllocation.isAllocated(nodeId)) {
            return false;
        }

        Optional<SkillNode> nodeOpt = currentTree.getNode(nodeId);
        if (nodeOpt.isEmpty()) {
            return false;
        }

        SkillNode node = nodeOpt.get();

        // ルートは解除不可
        if (node.isRoot()) {
            return false;
        }

        // 他のノードの接続元になっていないかチェック
        // （このノードを解除すると孤立するノードがあれば解除不可）
        if (!canDeallocate(nodeId)) {
            return false;
        }

        int refund = node.getActualCost();
        editingAllocation = editingAllocation.withDeallocatedNode(nodeId, refund);
        updateSkillPoints();

        return true;
    }

    /**
     * 全リセット
     */
    public void resetAllocation() {
        if (currentTree != null) {
            editingAllocation = editingAllocation.reset();
            updateSkillPoints();
        }
    }

    /**
     * 変更を適用してツールに保存
     */
    public void applyChanges() {
        ItemStack toolStack = toolContainer.getItem(0);
        if (!toolStack.isEmpty() && toolStack.has(AnvilDataComponents.TOOL_DATA.get())) {
            toolStack.set(AnvilDataComponents.SKILL_ALLOCATION.get(), editingAllocation);
        }
    }

    /**
     * 変更をリセット
     */
    public void resetChanges() {
        ItemStack toolStack = toolContainer.getItem(0);
        if (!toolStack.isEmpty() && toolStack.has(AnvilDataComponents.SKILL_ALLOCATION.get())) {
            this.editingAllocation = toolStack.get(AnvilDataComponents.SKILL_ALLOCATION.get());
        } else if (currentTree != null) {
            this.editingAllocation = SkillAllocation.create(currentTree.id());
        }
        updateSkillPoints();
    }

    // ============================================
    // ジュエル操作
    // ============================================

    /**
     * ジュエルをソケットに装着
     *
     * @param socketId ソケットノードID
     * @param jewelStack ジュエルアイテムスタック
     * @return 成功した場合true
     */
    public boolean equipJewel(ResourceLocation socketId, ItemStack jewelStack) {
        if (currentTree == null || jewelStack.isEmpty()) {
            return false;
        }

        // ジュエルアイテムかチェック
        if (!(jewelStack.getItem() instanceof JewelItem)) {
            return false;
        }

        // ジュエルIDを取得
        ResourceLocation jewelId = JewelItem.getJewelId(jewelStack);
        if (jewelId == null) {
            return false;
        }

        // ソケットノードが存在するかチェック
        Optional<SkillNode> socketNodeOpt = currentTree.getNode(socketId);
        if (socketNodeOpt.isEmpty()) {
            return false;
        }

        SkillNode socketNode = socketNodeOpt.get();

        // ジュエルソケットタイプかチェック
        if (socketNode.type() != SkillNodeType.JEWEL_SOCKET) {
            return false;
        }

        // ソケットが解放済みかチェック
        if (!editingAllocation.isAllocated(socketId)) {
            return false;
        }

        // ジュエルがツールタイプに装着可能かチェック
        Optional<JewelData> jewelDataOpt = JewelRegistry.get(jewelId);
        if (jewelDataOpt.isEmpty()) {
            return false;
        }

        JewelData jewelData = jewelDataOpt.get();
        if (!jewelData.canEquipTo(currentTree.toolType())) {
            return false;
        }

        // ジュエルを装着
        editingAllocation = editingAllocation.withEquippedJewel(socketId, jewelId);
        return true;
    }

    /**
     * ジュエルをソケットから取り外す
     *
     * @param socketId ソケットノードID
     * @return 取り外したジュエルID（なければempty）
     */
    public Optional<ResourceLocation> unequipJewel(ResourceLocation socketId) {
        if (currentTree == null) {
            return Optional.empty();
        }

        // 装着されているジュエルを取得
        Optional<ResourceLocation> equippedJewel = editingAllocation.getEquippedJewel(socketId);
        if (equippedJewel.isEmpty()) {
            return Optional.empty();
        }

        // ジュエルを取り外す
        editingAllocation = editingAllocation.withRemovedJewel(socketId);
        return equippedJewel;
    }

    /**
     * 解放済みのジュエルソケット一覧を取得
     *
     * @return ジュエルソケットノードのリスト
     */
    public List<SkillNode> getUnlockedJewelSockets() {
        if (currentTree == null) {
            return List.of();
        }

        List<SkillNode> sockets = new ArrayList<>();
        for (SkillNode node : currentTree.nodes()) {
            if (node.type() == SkillNodeType.JEWEL_SOCKET && editingAllocation.isAllocated(node.id())) {
                sockets.add(node);
            }
        }
        return sockets;
    }

    /**
     * 指定ソケットに装着されているジュエルデータを取得
     *
     * @param socketId ソケットノードID
     * @return ジュエルデータ（装着されていなければempty）
     */
    public Optional<JewelData> getEquippedJewelData(ResourceLocation socketId) {
        return editingAllocation.getEquippedJewel(socketId)
                .flatMap(JewelRegistry::get);
    }

    // ============================================
    // 判定ヘルパー
    // ============================================

    /**
     * ノードを解放可能かチェック
     */
    private boolean canAllocate(ResourceLocation nodeId) {
        Optional<SkillNode> nodeOpt = currentTree.getNode(nodeId);
        if (nodeOpt.isEmpty()) {
            return false;
        }

        SkillNode node = nodeOpt.get();

        // ルートノードは常に解放可能
        if (node.isRoot()) {
            return true;
        }

        // 隣接ノードのいずれかが解放済みなら解放可能
        for (SkillNode connected : currentTree.getConnectedNodes(nodeId)) {
            if (editingAllocation.isAllocated(connected.id())) {
                return true;
            }
        }

        return false;
    }

    /**
     * ノードを解除可能かチェック
     */
    private boolean canDeallocate(ResourceLocation nodeId) {
        // このノードを解除した場合、他の解放済みノードがルートから到達可能かチェック
        // 簡易実装: 接続先に他の解放済みノードがあれば解除不可
        for (SkillNode connected : currentTree.getConnectedNodes(nodeId)) {
            if (editingAllocation.isAllocated(connected.id())) {
                // 接続先が他のノードと繋がっているかチェック
                boolean hasOtherConnection = false;
                for (SkillNode otherConnected : currentTree.getConnectedNodes(connected.id())) {
                    if (!otherConnected.id().equals(nodeId) && editingAllocation.isAllocated(otherConnected.id())) {
                        hasOtherConnection = true;
                        break;
                    }
                }
                if (!hasOtherConnection && !connected.isRoot()) {
                    return false; // このノードを解除すると孤立するノードがある
                }
            }
        }
        return true;
    }

    // ============================================
    // 情報取得
    // ============================================

    /**
     * 現在のスキル配分を取得
     */
    public SkillAllocation getEditingAllocation() {
        return editingAllocation;
    }

    /**
     * 現在のスキルツリーを取得
     */
    public SkillTree getCurrentTree() {
        return currentTree;
    }

    /**
     * ツールのレベルを取得
     */
    public int getToolLevel() {
        ItemStack toolStack = toolContainer.getItem(0);
        if (!toolStack.isEmpty() && toolStack.has(AnvilDataComponents.TOOL_DATA.get())) {
            AnvilToolData toolData = toolStack.get(AnvilDataComponents.TOOL_DATA.get());
            return toolData.level();
        }
        return 1;
    }

    /**
     * 利用可能スキルポイントを取得
     */
    public int getAvailablePoints() {
        return data.get(0);
    }

    /**
     * 使用済みスキルポイントを取得
     */
    public int getUsedPoints() {
        return data.get(1);
    }

    /**
     * 総スキルポイントを取得
     */
    public int getTotalPoints() {
        return getAvailablePoints() + getUsedPoints();
    }

    // ============================================
    // AbstractContainerMenu実装
    // ============================================

    @Override
    public boolean stillValid(Player player) {
        return toolContainer.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            // ツールスロット(0)からの移動
            if (index == 0) {
                if (!this.moveItemStackTo(slotStack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // プレイヤーインベントリ/ホットバーからの移動
            else {
                // ANVILツールならツールスロットへ
                if (slotStack.has(AnvilDataComponents.TOOL_DATA.get())) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // それ以外はインベントリ/ホットバー間で移動
                else if (index < 28) {
                    if (!this.moveItemStackTo(slotStack, 28, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.moveItemStackTo(slotStack, 1, 28, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return result;
    }

    // ============================================
    // カスタムスロット
    // ============================================

    /**
     * ツールスロット（ANVILツールのみ受け入れ）
     */
    private static class ToolSlot extends Slot {
        public ToolSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.has(AnvilDataComponents.TOOL_DATA.get());
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
