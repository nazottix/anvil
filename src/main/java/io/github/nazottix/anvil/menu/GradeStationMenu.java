package io.github.nazottix.anvil.menu;

import io.github.nazottix.anvil.block.entity.GradeStationBlockEntity;
import io.github.nazottix.anvil.data.AnvilDataComponents;
import io.github.nazottix.anvil.data.component.ToolPart;
import io.github.nazottix.anvil.item.AnvilItems;
import io.github.nazottix.anvil.item.PartItem;
import io.github.nazottix.anvil.material.Grade;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * グレードステーションメニュー
 *
 * グレードアップグレード画面のサーバー側ロジックを管理します。
 * スロットの配置、アイテムの移動、アップグレード処理を行います。
 *
 * スロット構成:
 * - 0: パーツ入力スロット（アップグレード対象）
 * - 1: エッセンス入力スロット（グレードエッセンス）
 * - 2-28: プレイヤーインベントリ
 * - 29-37: ホットバー
 */
public class GradeStationMenu extends AbstractContainerMenu {

    // ブロックエンティティのコンテナ
    private final Container container;

    // ブロックエンティティ参照（サーバー側のみ有効）
    private final GradeStationBlockEntity blockEntity;

    /**
     * クライアント側コンストラクタ（ネットワークから）
     */
    public GradeStationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(GradeStationBlockEntity.INVENTORY_SIZE));
    }

    /**
     * サーバー側コンストラクタ
     */
    public GradeStationMenu(int containerId, Inventory playerInventory, Container container) {
        super(AnvilMenuTypes.GRADE_STATION.get(), containerId);
        this.container = container;

        // ブロックエンティティ参照を保存（アップグレード実行用）
        if (container instanceof GradeStationBlockEntity be) {
            this.blockEntity = be;
        } else {
            this.blockEntity = null;
        }

        // コンテナのサイズをチェック
        checkContainerSize(container, GradeStationBlockEntity.INVENTORY_SIZE);

        // ============================================
        // スロット配置
        // ============================================

        // パーツ入力スロット（アップグレード対象） - 位置 (26, 35)
        this.addSlot(new PartInputSlot(container, GradeStationBlockEntity.SLOT_PART_INPUT, 26, 35));

        // エッセンス入力スロット - 位置 (80, 35)
        this.addSlot(new EssenceInputSlot(container, GradeStationBlockEntity.SLOT_ESSENCE_INPUT, 80, 35));

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
     * ボタンID 0: アップグレード実行
     */
    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        // ボタンID 0 = アップグレードボタン
        if (buttonId == 0) {
            upgrade();
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

            // グレードステーションスロット（0-1）からの移動
            if (index < 2) {
                // プレイヤーインベントリへ移動
                if (!this.moveItemStackTo(slotStack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // プレイヤーインベントリ（2-28）からの移動
            else if (index < 29) {
                // パーツはパーツスロットへ、エッセンスはエッセンススロットへ
                if (PartItem.hasValidPartData(slotStack)) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        // ホットバーへ移動
                        if (!this.moveItemStackTo(slotStack, 29, 38, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                } else if (slotStack.getItem() == AnvilItems.GRADE_ESSENCE.get()) {
                    if (!this.moveItemStackTo(slotStack, 1, 2, false)) {
                        // ホットバーへ移動
                        if (!this.moveItemStackTo(slotStack, 29, 38, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                } else {
                    // それ以外はホットバーへ
                    if (!this.moveItemStackTo(slotStack, 29, 38, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
            // ホットバー（29-37）からの移動
            else {
                // パーツはパーツスロットへ、エッセンスはエッセンススロットへ
                if (PartItem.hasValidPartData(slotStack)) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        // プレイヤーインベントリへ移動
                        if (!this.moveItemStackTo(slotStack, 2, 29, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                } else if (slotStack.getItem() == AnvilItems.GRADE_ESSENCE.get()) {
                    if (!this.moveItemStackTo(slotStack, 1, 2, false)) {
                        // プレイヤーインベントリへ移動
                        if (!this.moveItemStackTo(slotStack, 2, 29, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                } else {
                    // それ以外はプレイヤーインベントリへ
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
    // アップグレード処理
    // ============================================

    /**
     * グレードをアップグレード
     */
    public void upgrade() {
        if (blockEntity != null) {
            blockEntity.upgrade();
        }
    }

    /**
     * アップグレード可能かどうかをチェック
     */
    public boolean canUpgrade() {
        if (blockEntity != null) {
            return blockEntity.canUpgrade();
        }
        // クライアント側でのチェック
        ItemStack partStack = container.getItem(GradeStationBlockEntity.SLOT_PART_INPUT);
        ItemStack essenceStack = container.getItem(GradeStationBlockEntity.SLOT_ESSENCE_INPUT);

        if (partStack.isEmpty() || !PartItem.hasValidPartData(partStack)) {
            return false;
        }

        if (essenceStack.isEmpty() || essenceStack.getItem() != AnvilItems.GRADE_ESSENCE.get()) {
            return false;
        }

        ToolPart partData = partStack.get(AnvilDataComponents.PART_DATA.get());
        if (partData == null) {
            return false;
        }

        // StringからGrade enumに変換
        Grade currentGrade = Grade.fromId(partData.grade());
        Grade nextGrade = GradeStationBlockEntity.getNextGrade(currentGrade);

        if (nextGrade == null) {
            return false;
        }

        int requiredEssence = GradeStationBlockEntity.getRequiredEssence(currentGrade);
        return essenceStack.getCount() >= requiredEssence;
    }

    /**
     * 現在のパーツのグレードを取得
     */
    @Nullable
    public Grade getCurrentGrade() {
        ItemStack partStack = container.getItem(GradeStationBlockEntity.SLOT_PART_INPUT);
        if (partStack.isEmpty() || !PartItem.hasValidPartData(partStack)) {
            return null;
        }
        ToolPart partData = partStack.get(AnvilDataComponents.PART_DATA.get());
        // StringからGrade enumに変換
        return partData != null ? Grade.fromId(partData.grade()) : null;
    }

    /**
     * 必要なエッセンス数を取得
     */
    public int getRequiredEssence() {
        Grade currentGrade = getCurrentGrade();
        if (currentGrade == null) {
            return 0;
        }
        return GradeStationBlockEntity.getRequiredEssence(currentGrade);
    }

    /**
     * 現在のエッセンス数を取得
     */
    public int getCurrentEssenceCount() {
        ItemStack essenceStack = container.getItem(GradeStationBlockEntity.SLOT_ESSENCE_INPUT);
        if (essenceStack.isEmpty() || essenceStack.getItem() != AnvilItems.GRADE_ESSENCE.get()) {
            return 0;
        }
        return essenceStack.getCount();
    }

    // ============================================
    // カスタムスロットクラス
    // ============================================

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

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    /**
     * エッセンス入力スロット
     * グレードエッセンスのみ受け入れる
     */
    private static class EssenceInputSlot extends Slot {
        public EssenceInputSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // グレードエッセンスのみ受け入れる
            return stack.getItem() == AnvilItems.GRADE_ESSENCE.get();
        }
    }
}
