package io.github.nazottix.anvil.menu;

import io.github.nazottix.anvil.block.entity.ModStationBlockEntity;
import io.github.nazottix.anvil.data.AnvilDataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/**
 * MODステーションメニュー
 *
 * MOD装着画面のサーバー側ロジックを管理します。
 * ツールへのMOD装着・取り外しを行います。
 *
 * スロット構成:
 * - 0: ツール入力スロット
 * - 1-8: MOD入力スロット（8スロット）
 * - 9-35: プレイヤーインベントリ
 * - 36-44: ホットバー
 */
public class ModStationMenu extends AbstractContainerMenu {

    // ブロックエンティティのコンテナ
    private final Container container;

    // 同期データ
    private final ContainerData data;

    /**
     * クライアント側コンストラクタ（ネットワークから）
     */
    public ModStationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(ModStationBlockEntity.INVENTORY_SIZE), new SimpleContainerData(0));
    }

    /**
     * サーバー側コンストラクタ
     */
    public ModStationMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(AnvilMenuTypes.MOD_STATION.get(), containerId);
        this.container = container;
        this.data = data;

        // コンテナのサイズをチェック
        checkContainerSize(container, ModStationBlockEntity.INVENTORY_SIZE);

        // コンテナデータを追加（GUI同期用）
        addDataSlots(data);

        // ============================================
        // スロット配置
        // ============================================

        // ツール入力スロット - 位置 (80, 17)
        this.addSlot(new ToolInputSlot(container, ModStationBlockEntity.SLOT_TOOL, 80, 17));

        // MOD入力スロット（8つ、2行4列） - 位置 (26, 53)から
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 4; col++) {
                int slotIndex = ModStationBlockEntity.SLOT_MOD_START + row * 4 + col;
                this.addSlot(new ModInputSlot(container, slotIndex, 26 + col * 36, 53 + row * 18));
            }
        }

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

            // ステーションスロット（0-8）からの移動
            if (index < 9) {
                // プレイヤーインベントリへ移動
                if (!this.moveItemStackTo(slotStack, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // プレイヤーインベントリ（9-35）からの移動
            else if (index < 36) {
                // ツールスロットへ移動を試みる
                if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                    // MODスロットへ移動を試みる
                    if (!this.moveItemStackTo(slotStack, 1, 9, false)) {
                        // ホットバーへ移動
                        if (!this.moveItemStackTo(slotStack, 36, 45, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }
            // ホットバー（36-44）からの移動
            else {
                // ツールスロットへ移動を試みる
                if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                    // MODスロットへ移動を試みる
                    if (!this.moveItemStackTo(slotStack, 1, 9, false)) {
                        // プレイヤーインベントリへ移動
                        if (!this.moveItemStackTo(slotStack, 9, 36, false)) {
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
     * MOD入力スロット
     * MODアイテムのみ受け入れる
     */
    private static class ModInputSlot extends Slot {
        public ModInputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // TODO: MODアイテムのみ受け入れるようにする
            return true;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
