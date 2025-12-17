package io.github.nazottix.anvil.menu;

import io.github.nazottix.anvil.block.entity.RefineryBlockEntity;
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
 * 精錬所メニュー
 *
 * 精錬所画面のサーバー側ロジックを管理します。
 * スロット配置、アイテム移動、進捗データの同期を行います。
 *
 * スロット構成:
 * - 0: 入力スロット（バニラ素材）
 * - 1: 燃料スロット
 * - 2: 出力スロット
 * - 3-29: プレイヤーインベントリ
 * - 30-38: ホットバー
 */
public class RefineryMenu extends AbstractContainerMenu {

    // ブロックエンティティのコンテナ
    private final Container container;

    // 同期データ（進捗、燃料燃焼時間）
    private final ContainerData data;

    /**
     * クライアント側コンストラクタ（ネットワークから）
     */
    public RefineryMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(RefineryBlockEntity.INVENTORY_SIZE), new SimpleContainerData(3));
    }

    /**
     * サーバー側コンストラクタ
     */
    public RefineryMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(AnvilMenuTypes.REFINERY.get(), containerId);
        this.container = container;
        this.data = data;

        // コンテナのサイズをチェック
        checkContainerSize(container, RefineryBlockEntity.INVENTORY_SIZE);
        checkContainerDataCount(data, 3);

        // コンテナデータを追加（GUI同期用）
        addDataSlots(data);

        // ============================================
        // スロット配置
        // かまど風レイアウト
        // ============================================

        // 入力スロット（バニラ素材） - 位置 (56, 17)
        this.addSlot(new InputSlot(container, RefineryBlockEntity.SLOT_INPUT, 56, 17));

        // 燃料スロット - 位置 (56, 53)
        this.addSlot(new FuelSlot(container, RefineryBlockEntity.SLOT_FUEL, 56, 53));

        // 出力スロット - 位置 (116, 35)
        this.addSlot(new OutputSlot(container, RefineryBlockEntity.SLOT_OUTPUT, 116, 35));

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

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            // 精錬所スロット（0-2）からの移動
            if (index < 3) {
                // プレイヤーインベントリへ移動
                if (!this.moveItemStackTo(slotStack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // プレイヤーインベントリ（3-29）からの移動
            else if (index < 30) {
                // 精錬可能なアイテムは入力スロットへ
                if (RefineryBlockEntity.canRefine(slotStack)) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        // ホットバーへ移動
                        if (!this.moveItemStackTo(slotStack, 30, 39, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
                // 燃料は燃料スロットへ
                else if (isFuel(slotStack)) {
                    if (!this.moveItemStackTo(slotStack, 1, 2, false)) {
                        // ホットバーへ移動
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
                // 精錬可能なアイテムは入力スロットへ
                if (RefineryBlockEntity.canRefine(slotStack)) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        // プレイヤーインベントリへ移動
                        if (!this.moveItemStackTo(slotStack, 3, 30, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
                // 燃料は燃料スロットへ
                else if (isFuel(slotStack)) {
                    if (!this.moveItemStackTo(slotStack, 1, 2, false)) {
                        // プレイヤーインベントリへ移動
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

    /**
     * アイテムが燃料として使用可能かチェック
     */
    private boolean isFuel(ItemStack stack) {
        return RefineryBlockEntity.isFuel(stack);
    }

    // ============================================
    // データアクセス（GUI用）
    // ============================================

    /**
     * 精錬進捗を取得（0-200）
     */
    public int getRefineProgress() {
        return data.get(0);
    }

    /**
     * 燃料残り燃焼時間を取得
     */
    public int getFuelBurnTime() {
        return data.get(1);
    }

    /**
     * 現在の燃料の最大燃焼時間を取得
     */
    public int getCurrentFuelMaxTime() {
        return data.get(2);
    }

    /**
     * 精錬進捗率を取得（0.0-1.0）
     * GUI描画用
     */
    public float getRefineProgressRatio() {
        int progress = getRefineProgress();
        return (float) progress / RefineryBlockEntity.DEFAULT_REFINE_TIME;
    }

    /**
     * 燃料残量率を取得（0.0-1.0）
     * GUI描画用
     */
    public float getFuelBurnRatio() {
        int maxTime = getCurrentFuelMaxTime();
        if (maxTime <= 0) {
            return 0.0f;
        }
        return (float) getFuelBurnTime() / maxTime;
    }

    /**
     * 精錬中かどうか
     */
    public boolean isRefining() {
        return getRefineProgress() > 0;
    }

    /**
     * 燃料が燃焼中かどうか
     */
    public boolean isBurning() {
        return getFuelBurnTime() > 0;
    }

    // ============================================
    // カスタムスロットクラス
    // ============================================

    /**
     * 入力スロット
     * 精錬可能な素材のみ受け入れる
     */
    private static class InputSlot extends Slot {
        public InputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // 精錬可能なアイテムのみ受け入れる
            return RefineryBlockEntity.canRefine(stack);
        }
    }

    /**
     * 燃料スロット
     * 燃料アイテムのみ受け入れる
     */
    private static class FuelSlot extends Slot {
        public FuelSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // 燃料として使用可能なアイテムのみ受け入れる
            // NeoForge 1.21ではItemStack.getBurnTime()を使用
            return RefineryBlockEntity.isFuel(stack);
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
            // 出力スロットにはアイテムを直接置けない
            return false;
        }
    }
}
