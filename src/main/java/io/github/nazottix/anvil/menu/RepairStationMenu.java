package io.github.nazottix.anvil.menu;

import io.github.nazottix.anvil.block.entity.RepairStationBlockEntity;
import io.github.nazottix.anvil.data.AnvilDataComponents;
import io.github.nazottix.anvil.item.PartItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * リペアステーションメニュー
 *
 * ツール修理画面のサーバー側ロジックを管理します。
 * スロットの配置、アイテムの移動、修理処理を行います。
 *
 * スロット構成:
 * - 0: ツール入力スロット（修理対象）
 * - 1: パーツ入力スロット（修理材料）
 * - 2-28: プレイヤーインベントリ
 * - 29-37: ホットバー
 */
public class RepairStationMenu extends AbstractContainerMenu {

    // ブロックエンティティのコンテナ
    private final Container container;

    /**
     * クライアント側コンストラクタ（ネットワークから）
     */
    public RepairStationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(RepairStationBlockEntity.INVENTORY_SIZE));
    }

    /**
     * サーバー側コンストラクタ
     */
    public RepairStationMenu(int containerId, Inventory playerInventory, Container container) {
        super(AnvilMenuTypes.REPAIR_STATION.get(), containerId);
        this.container = container;

        // コンテナのサイズをチェック
        checkContainerSize(container, RepairStationBlockEntity.INVENTORY_SIZE);

        // ============================================
        // スロット配置
        // ============================================

        // ツール入力スロット（修理対象） - 位置 (26, 35)
        this.addSlot(new ToolInputSlot(container, RepairStationBlockEntity.SLOT_TOOL_INPUT, 26, 35));

        // パーツ入力スロット（修理材料） - 位置 (80, 35)
        this.addSlot(new PartInputSlot(container, RepairStationBlockEntity.SLOT_PART_INPUT, 80, 35));

        // プレイヤーインベントリ（3行9列）- y=84から
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // ホットバー（1行9列）- y=142
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    // ============================================
    // メニュー操作
    // ============================================

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    /**
     * カスタムボタンクリック処理（サーバー側で実行）
     * ボタンID 0: 修理実行
     */
    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        // ボタンID 0 = 修理ボタン
        if (buttonId == 0) {
            repairTool();
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            // リペアステーションスロット（0-1）からの移動
            if (index < 2) {
                // プレイヤーインベントリへ移動
                if (!this.moveItemStackTo(slotStack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // プレイヤーインベントリ（2-28）からの移動
            else if (index < 29) {
                // まずツールスロットへ、次にパーツスロットへ移動を試みる
                if (!this.moveItemStackTo(slotStack, 0, 2, false)) {
                    // ホットバーへ移動
                    if (!this.moveItemStackTo(slotStack, 29, 38, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
            // ホットバー（29-37）からの移動
            else {
                // まずツールスロットへ、次にパーツスロットへ移動を試みる
                if (!this.moveItemStackTo(slotStack, 0, 2, false)) {
                    // プレイヤーインベントリへ移動
                    if (!this.moveItemStackTo(slotStack, 2, 29, false)) {
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
    // 修理処理
    // ============================================

    /**
     * ツールを修理
     * パーツを消費してツールの耐久値を回復
     */
    public void repairTool() {
        ItemStack toolStack = container.getItem(RepairStationBlockEntity.SLOT_TOOL_INPUT);
        ItemStack partStack = container.getItem(RepairStationBlockEntity.SLOT_PART_INPUT);

        // ツールが入っていない場合
        if (toolStack.isEmpty() || !toolStack.has(AnvilDataComponents.TOOL_DATA.get())) {
            return;
        }

        // パーツが入っていない場合
        if (partStack.isEmpty() || !PartItem.hasValidPartData(partStack)) {
            return;
        }

        // 耐久値が最大でない場合のみ修理
        if (toolStack.isDamaged()) {
            // 耐久値を回復（パーツ1つで25%回復）
            int maxDamage = toolStack.getMaxDamage();
            int repairAmount = Math.max(1, maxDamage / 4);
            int currentDamage = toolStack.getDamageValue();
            int newDamage = Math.max(0, currentDamage - repairAmount);

            toolStack.setDamageValue(newDamage);

            // パーツを1つ消費
            partStack.shrink(1);

            // コンテナを更新
            container.setChanged();
        }
    }

    /**
     * 修理可能かどうかをチェック
     */
    public boolean canRepair() {
        ItemStack toolStack = container.getItem(RepairStationBlockEntity.SLOT_TOOL_INPUT);
        ItemStack partStack = container.getItem(RepairStationBlockEntity.SLOT_PART_INPUT);

        // ツールがあり、ダメージを受けていて、パーツがある場合に修理可能
        return !toolStack.isEmpty()
                && toolStack.has(AnvilDataComponents.TOOL_DATA.get())
                && toolStack.isDamaged()
                && !partStack.isEmpty()
                && PartItem.hasValidPartData(partStack);
    }

    // ============================================
    // カスタムスロットクラス
    // ============================================

    /**
     * ツール入力スロット
     * ANVILツールのみ受け入れる
     */
    private static class ToolInputSlot extends Slot {
        public ToolInputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // ANVILツールのみ受け入れる
            return stack.has(AnvilDataComponents.TOOL_DATA.get());
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    /**
     * パーツ入力スロット
     * パーツアイテムのみ受け入れる
     */
    private static class PartInputSlot extends Slot {
        public PartInputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // パーツアイテムのみ受け入れる
            return PartItem.hasValidPartData(stack);
        }
    }
}
