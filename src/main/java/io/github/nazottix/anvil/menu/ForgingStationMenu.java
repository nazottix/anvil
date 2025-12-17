package io.github.nazottix.anvil.menu;

import io.github.nazottix.anvil.block.entity.ForgingStationBlockEntity;
import io.github.nazottix.anvil.item.SmithingHammerItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 鍛造ステーションメニュー
 *
 * 鍛造ステーション画面のサーバー側ロジックを管理します。
 * スロット配置、アイテム移動、進捗データの同期を行います。
 *
 * スロット構成:
 * - 0: 入力スロット（精錬素材）
 * - 1: ハンマースロット
 * - 2: 出力スロット
 * - 3-29: プレイヤーインベントリ
 * - 30-38: ホットバー
 */
public class ForgingStationMenu extends AbstractContainerMenu {

    // ブロックエンティティのコンテナ
    private final Container container;

    // 同期データ（進捗）
    private final ContainerData data;

    /**
     * クライアント側コンストラクタ（ネットワークから）
     */
    public ForgingStationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(ForgingStationBlockEntity.INVENTORY_SIZE), new SimpleContainerData(1));
    }

    /**
     * サーバー側コンストラクタ
     * 
     * スロット位置をStation Blockスタイルに合わせて配置
     */
    public ForgingStationMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(AnvilMenuTypes.FORGING_STATION.get(), containerId);
        this.container = container;
        this.data = data;

        checkContainerSize(container, ForgingStationBlockEntity.INVENTORY_SIZE);
        checkContainerDataCount(data, 1);

        addDataSlots(data);

        // ============================================
        // スロット配置（Station Blockスタイルに統一）
        // ============================================

        // 入力スロット（精錬素材） - 位置 (56, 35) - GUIサイズ200に対応
        this.addSlot(new InputSlot(container, ForgingStationBlockEntity.SLOT_INPUT, 56, 35));

        // ハンマースロット - 位置 (56, 71) - GUIサイズ200に対応
        this.addSlot(new HammerSlot(container, ForgingStationBlockEntity.SLOT_HAMMER, 56, 71));

        // 出力スロット - 位置 (116, 53) - GUIサイズ200に対応
        this.addSlot(new OutputSlot(container, ForgingStationBlockEntity.SLOT_OUTPUT, 116, 53));

        // プレイヤーインベントリ（3行9列）- y=117（Station Blockスタイル）
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 117 + row * 18));
            }
        }

        // ホットバー（1行9列）- y=175（Station Blockスタイル）
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

            // 鍛造ステーションスロット（0-2）からの移動
            if (index < 3) {
                if (!this.moveItemStackTo(slotStack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // プレイヤーインベントリ（3-29）からの移動
            else if (index < 30) {
                // 精錬素材は入力スロットへ
                if (ForgingStationBlockEntity.canInsertInput(slotStack)) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        if (!this.moveItemStackTo(slotStack, 30, 39, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
                // ハンマーはハンマースロットへ
                else if (ForgingStationBlockEntity.canInsertHammer(slotStack)) {
                    if (!this.moveItemStackTo(slotStack, 1, 2, false)) {
                        if (!this.moveItemStackTo(slotStack, 30, 39, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
                // それ以外はホットバーへ
                else if (!this.moveItemStackTo(slotStack, 30, 39, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // ホットバー（30-38）からの移動
            else {
                // 精錬素材は入力スロットへ
                if (ForgingStationBlockEntity.canInsertInput(slotStack)) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        if (!this.moveItemStackTo(slotStack, 3, 30, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
                // ハンマーはハンマースロットへ
                else if (ForgingStationBlockEntity.canInsertHammer(slotStack)) {
                    if (!this.moveItemStackTo(slotStack, 1, 2, false)) {
                        if (!this.moveItemStackTo(slotStack, 3, 30, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
                // それ以外はプレイヤーインベントリへ
                else if (!this.moveItemStackTo(slotStack, 3, 30, false)) {
                    return ItemStack.EMPTY;
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
    // データアクセス（GUI用）
    // ============================================

    /**
     * 鍛造進捗を取得（0-200）
     */
    public int getForgeProgress() {
        return data.get(0);
    }

    /**
     * 鍛造進捗率を取得（0.0-1.0）
     */
    public float getForgeProgressRatio() {
        int progress = getForgeProgress();
        return (float) progress / ForgingStationBlockEntity.DEFAULT_FORGE_TIME;
    }

    /**
     * 鍛造中かどうか
     */
    public boolean isForging() {
        return getForgeProgress() > 0;
    }

    // ============================================
    // カスタムスロットクラス
    // ============================================

    /**
     * 入力スロット
     * 精錬素材（REFINED）のみ受け入れる
     */
    private static class InputSlot extends Slot {
        public InputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return ForgingStationBlockEntity.canInsertInput(stack);
        }
    }

    /**
     * ハンマースロット
     * SmithingHammerItemのみ受け入れる
     */
    private static class HammerSlot extends Slot {
        public HammerSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return ForgingStationBlockEntity.canInsertHammer(stack);
        }
    }

    /**
     * 出力スロット
     */
    private static class OutputSlot extends Slot {
        public OutputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
