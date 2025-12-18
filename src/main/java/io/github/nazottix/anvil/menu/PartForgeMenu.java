package io.github.nazottix.anvil.menu;

import io.github.nazottix.anvil.block.entity.PartForgeBlockEntity;
import io.github.nazottix.anvil.material.MaterialRegistry;
import io.github.nazottix.anvil.tool.PartType;
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
 * パーツ鍛造所メニュー
 *
 * パーツ鍛造所画面のサーバー側ロジックを管理します。
 * パーツタイプの選択と鋳造処理を行います。
 *
 * 素材加工の簡略化により、バニラ素材（鉄インゴット、ダイヤモンドなど）を
 * 直接パーツに変換できます。
 *
 * スロット構成:
 * - 0: 入力スロット（バニラ素材）
 * - 1: 出力スロット（パーツ）
 * - 2-28: プレイヤーインベントリ
 * - 29-37: ホットバー
 */
public class PartForgeMenu extends AbstractContainerMenu {

    // ブロックエンティティのコンテナ
    private final Container container;

    // 同期データ（選択中のパーツタイプ）
    private final ContainerData data;

    // ブロックエンティティ参照（サーバー側のみ有効）
    private final PartForgeBlockEntity blockEntity;

    /**
     * クライアント側コンストラクタ（ネットワークから）
     */
    public PartForgeMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(PartForgeBlockEntity.INVENTORY_SIZE), new SimpleContainerData(1));
    }

    /**
     * サーバー側コンストラクタ
     * 
     * スロット位置をStation Blockスタイルに合わせて配置
     */
    public PartForgeMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(AnvilMenuTypes.PART_FORGE.get(), containerId);
        this.container = container;
        this.data = data;

        // ブロックエンティティ参照を保存（鋳造実行用）
        if (container instanceof PartForgeBlockEntity be) {
            this.blockEntity = be;
        } else {
            this.blockEntity = null;
        }

        // コンテナのサイズをチェック
        checkContainerSize(container, PartForgeBlockEntity.INVENTORY_SIZE);
        checkContainerDataCount(data, 1);

        // コンテナデータを追加（GUI同期用）
        addDataSlots(data);

        // ============================================
        // スロット配置（Station Blockスタイルに統一）
        // ============================================

        // 入力スロット（加工済み素材） - 位置 (27, 70) - GUIサイズ200に対応
        this.addSlot(new InputSlot(container, PartForgeBlockEntity.SLOT_INPUT, 27, 70));

        // 出力スロット - 位置 (134, 70) - GUIサイズ200に対応
        this.addSlot(new OutputSlot(container, PartForgeBlockEntity.SLOT_OUTPUT, 134, 70));

        // プレイヤーインベントリ（3行9列）- y=117から（Station Blockスタイル）
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

            // 鍛造所スロット（0-1）からの移動
            if (index < 2) {
                // プレイヤーインベントリへ移動
                if (!this.moveItemStackTo(slotStack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // プレイヤーインベントリ（2-28）からの移動
            else if (index < 29) {
                // 有効な素材原料は入力スロットへ
                if (PartForgeBlockEntity.canInsertInput(slotStack)) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        // ホットバーへ移動
                        if (!this.moveItemStackTo(slotStack, 29, 38, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
                // それ以外はホットバーへ
                else if (!this.moveItemStackTo(slotStack, 29, 38, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // ホットバー（29-37）からの移動
            else {
                // 有効な素材原料は入力スロットへ
                if (PartForgeBlockEntity.canInsertInput(slotStack)) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        // プレイヤーインベントリへ移動
                        if (!this.moveItemStackTo(slotStack, 2, 29, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
                // それ以外はプレイヤーインベントリへ
                else if (!this.moveItemStackTo(slotStack, 2, 29, false)) {
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
    // パーツタイプ選択・鋳造
    // ============================================

    /**
     * 選択中のパーツタイプインデックスを取得
     */
    public int getSelectedPartTypeIndex() {
        return data.get(0);
    }

    /**
     * 選択中のパーツタイプを取得
     */
    public PartType getSelectedPartType() {
        PartType[] types = PartType.values();
        int index = getSelectedPartTypeIndex();
        if (index >= 0 && index < types.length) {
            return types[index];
        }
        return null;
    }

    /**
     * パーツタイプを選択（サーバー側で呼び出し）
     *
     * @param index パーツタイプインデックス
     */
    public void selectPartType(int index) {
        data.set(0, index);
        if (blockEntity != null) {
            blockEntity.setSelectedPartTypeIndex(index);
        }
    }

    /**
     * 鋳造を実行（サーバー側で呼び出し）
     *
     * @return 鋳造成功した場合true
     */
    public boolean forge() {
        if (blockEntity != null) {
            return blockEntity.forge();
        }
        return false;
    }

    /**
     * 鋳造可能かチェック
     * 
     * サーバー側ではblockEntityを使用し、クライアント側ではcontainerとdataを直接チェックします。
     * 
     * @return 鋳造可能な場合true
     */
    public boolean canForge() {
        // サーバー側：blockEntityが存在する場合はそちらを使用
        if (blockEntity != null) {
            return blockEntity.canForge();
        }
        
        // クライアント側：containerとdataを直接チェック
        // 入力スロットが空でないかチェック
        ItemStack input = container.getItem(PartForgeBlockEntity.SLOT_INPUT);
        if (input.isEmpty()) {
            return false;
        }
        
        // 素材レジストリから対応する素材を検索
        if (MaterialRegistry.getInstance().getByRepairItem(input.getItem()).isEmpty()) {
            return false;
        }
        
        // 選択中のパーツタイプをチェック（data index 0 = selectedPartTypeIndex）
        int selectedPartTypeIndex = data.get(0);
        PartType[] types = PartType.values();
        if (selectedPartTypeIndex < 0 || selectedPartTypeIndex >= types.length) {
            return false;
        }
        
        // 出力スロットが空かチェック
        ItemStack output = container.getItem(PartForgeBlockEntity.SLOT_OUTPUT);
        if (!output.isEmpty()) {
            return false;
        }
        
        return true;
    }

    // ============================================
    // カスタムスロットクラス
    // ============================================

    /**
     * 入力スロット
     * 有効な素材原料のみ受け入れる
     */
    private static class InputSlot extends Slot {
        public InputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return PartForgeBlockEntity.canInsertInput(stack);
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
