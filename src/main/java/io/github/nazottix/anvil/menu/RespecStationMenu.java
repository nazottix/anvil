package io.github.nazottix.anvil.menu;

import io.github.nazottix.anvil.block.entity.RespecStationBlockEntity;
import io.github.nazottix.anvil.data.AnvilDataComponents;
// TODO: AnvilItems.RESPEC_ITEMが実装されたらインポートを復活
// import io.github.nazottix.anvil.item.AnvilItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/**
 * リスペックステーションメニュー
 *
 * リスペック画面のサーバー側ロジックを管理します。
 * ツールのスキルポイントリセットを行います。
 *
 * スロット構成:
 * - 0: ツール入力スロット
 * - 1: リスペックアイテムスロット
 * - 2-28: プレイヤーインベントリ
 * - 29-37: ホットバー
 */
public class RespecStationMenu extends AbstractContainerMenu {

    // ブロックエンティティのコンテナ
    private final Container container;

    // 同期データ
    private final ContainerData data;

    /**
     * クライアント側コンストラクタ（ネットワークから）
     */
    public RespecStationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(RespecStationBlockEntity.INVENTORY_SIZE), new SimpleContainerData(0));
    }

    /**
     * サーバー側コンストラクタ
     */
    public RespecStationMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(AnvilMenuTypes.RESPEC_STATION.get(), containerId);
        this.container = container;
        this.data = data;

        // コンテナのサイズをチェック
        checkContainerSize(container, RespecStationBlockEntity.INVENTORY_SIZE);

        // コンテナデータを追加（GUI同期用）
        addDataSlots(data);

        // ============================================
        // スロット配置
        // ============================================

        // ツール入力スロット - 位置 (56, 35)
        this.addSlot(new ToolInputSlot(container, RespecStationBlockEntity.SLOT_TOOL, 56, 35));

        // リスペックアイテムスロット - 位置 (104, 35)
        this.addSlot(new RespecItemSlot(container, RespecStationBlockEntity.SLOT_RESPEC_ITEM, 104, 35));

        // プレイヤーインベントリ（3行9列）- y=98から（14ピクセル下にずらした）
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 98 + row * 18));
            }
        }

        // ホットバー（1行9列）- y=156（14ピクセル下にずらした）
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 156));
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

            // ステーションスロット（0-1）からの移動
            if (index < 2) {
                // プレイヤーインベントリへ移動
                if (!this.moveItemStackTo(slotStack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // プレイヤーインベントリ（2-28）からの移動
            else if (index < 29) {
                // ツールスロットへ移動を試みる
                if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                    // リスペックアイテムスロットへ移動を試みる
                    if (!this.moveItemStackTo(slotStack, 1, 2, false)) {
                        // ホットバーへ移動
                        if (!this.moveItemStackTo(slotStack, 29, 38, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }
            // ホットバー（29-37）からの移動
            else {
                // ツールスロットへ移動を試みる
                if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                    // リスペックアイテムスロットへ移動を試みる
                    if (!this.moveItemStackTo(slotStack, 1, 2, false)) {
                        // プレイヤーインベントリへ移動
                        if (!this.moveItemStackTo(slotStack, 2, 29, false)) {
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
    // リスペック処理
    // ============================================

    /**
     * リスペックを実行
     * ツールのスキルポイントをリセットします
     *
     * @return 成功したかどうか
     */
    public boolean performRespec() {
        ItemStack toolStack = container.getItem(RespecStationBlockEntity.SLOT_TOOL);
        ItemStack respecItem = container.getItem(RespecStationBlockEntity.SLOT_RESPEC_ITEM);

        // ツールとリスペックアイテムが必要
        if (toolStack.isEmpty() || respecItem.isEmpty()) {
            return false;
        }

        // ANVILツールかチェック
        if (!toolStack.has(AnvilDataComponents.TOOL_DATA.get())) {
            return false;
        }

        // TODO: リスペック処理を実装
        // - スキルポイントをリセット
        // - リスペックアイテムを消費

        // リスペックアイテムを1つ消費
        respecItem.shrink(1);

        return true;
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
     * リスペックアイテムスロット
     * リスペックアイテムのみ受け入れる
     */
    private static class RespecItemSlot extends Slot {
        public RespecItemSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // TODO: リスペックアイテムが実装されたら、そのアイテムのみ受け入れるようにする
            // return stack.is(AnvilItems.RESPEC_ITEM.get());
            return true;
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }
    }
}
