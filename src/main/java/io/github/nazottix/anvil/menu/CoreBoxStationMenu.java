package io.github.nazottix.anvil.menu;

import io.github.nazottix.anvil.block.entity.CoreBoxStationBlockEntity;
import io.github.nazottix.anvil.data.AnvilDataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/**
 * コアボックスステーションメニュー
 *
 * グリッドモジュール配置画面のサーバー側ロジックを管理します。
 * テトリス風のモジュール配置を行います。
 *
 * スロット構成:
 * - 0: ツール入力スロット
 * - 1-4: モジュール入力スロット（4スロット）
 * - 5-31: プレイヤーインベントリ
 * - 32-40: ホットバー
 */
public class CoreBoxStationMenu extends AbstractContainerMenu {

    // ブロックエンティティのコンテナ
    private final Container container;

    // 同期データ
    private final ContainerData data;

    /**
     * クライアント側コンストラクタ（ネットワークから）
     */
    public CoreBoxStationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(CoreBoxStationBlockEntity.INVENTORY_SIZE), new SimpleContainerData(0));
    }

    /**
     * サーバー側コンストラクタ
     */
    public CoreBoxStationMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(AnvilMenuTypes.CORE_BOX_STATION.get(), containerId);
        this.container = container;
        this.data = data;

        // コンテナのサイズをチェック
        checkContainerSize(container, CoreBoxStationBlockEntity.INVENTORY_SIZE);

        // コンテナデータを追加（GUI同期用）
        addDataSlots(data);

        // ============================================
        // スロット配置
        // ============================================

        // ツール入力スロット - 位置 (8, 35)
        this.addSlot(new ToolInputSlot(container, CoreBoxStationBlockEntity.SLOT_TOOL, 8, 35));

        // モジュール入力スロット（4つ縦並び） - 位置 (152, 17)から
        this.addSlot(new ModuleInputSlot(container, CoreBoxStationBlockEntity.SLOT_MODULE_1, 152, 17));
        this.addSlot(new ModuleInputSlot(container, CoreBoxStationBlockEntity.SLOT_MODULE_2, 152, 35));
        this.addSlot(new ModuleInputSlot(container, CoreBoxStationBlockEntity.SLOT_MODULE_3, 152, 53));
        this.addSlot(new ModuleInputSlot(container, CoreBoxStationBlockEntity.SLOT_MODULE_4, 152, 71));

        // プレイヤーインベントリ（3行9列）- y=117から（14ピクセル下にずらした）
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 117 + row * 18));
            }
        }

        // ホットバー（1行9列）- y=175（14ピクセル下にずらした）
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 175));
        }
    }

    // ============================================
    // メニュー操作
    // ============================================

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            // ステーションスロット（0-4）からの移動
            if (index < 5) {
                // プレイヤーインベントリへ移動
                if (!this.moveItemStackTo(slotStack, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // プレイヤーインベントリ（5-31）からの移動
            else if (index < 32) {
                // ツールスロットへ移動を試みる
                if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                    // モジュールスロットへ移動を試みる
                    if (!this.moveItemStackTo(slotStack, 1, 5, false)) {
                        // ホットバーへ移動
                        if (!this.moveItemStackTo(slotStack, 32, 41, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }
            // ホットバー（32-40）からの移動
            else {
                // ツールスロットへ移動を試みる
                if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                    // モジュールスロットへ移動を試みる
                    if (!this.moveItemStackTo(slotStack, 1, 5, false)) {
                        // プレイヤーインベントリへ移動
                        if (!this.moveItemStackTo(slotStack, 5, 32, false)) {
                            return ItemStack.EMPTY;
                        }
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
     * モジュール入力スロット
     * グリッドモジュールアイテムのみ受け入れる
     */
    private static class ModuleInputSlot extends Slot {
        public ModuleInputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // TODO: グリッドモジュールアイテムのみ受け入れるようにする
            return true;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
