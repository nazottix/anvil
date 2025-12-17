package io.github.nazottix.anvil.block.entity;

import io.github.nazottix.anvil.menu.RepairStationMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * リペアステーションブロックエンティティ
 *
 * ツール修理用のインベントリとデータを管理します。
 *
 * スロット構成:
 * - スロット0: ツール入力スロット（修理対象のツール）
 * - スロット1: パーツ入力スロット（修理用パーツ）
 *
 * 仕様書参照: docs/08_入手_リスペックシステム.md
 */
public class RepairStationBlockEntity extends BlockEntity implements Container, MenuProvider {

    // インベントリサイズ
    public static final int SLOT_TOOL_INPUT = 0;   // ツール入力スロット（修理対象）
    public static final int SLOT_PART_INPUT = 1;   // パーツ入力スロット（修理材料）
    public static final int INVENTORY_SIZE = 2;    // 合計2スロット

    // インベントリ
    private NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

    /**
     * コンストラクタ
     */
    public RepairStationBlockEntity(BlockPos pos, BlockState state) {
        super(AnvilBlockEntities.REPAIR_STATION.get(), pos, state);
    }

    // ============================================
    // Container実装
    // ============================================

    @Override
    public int getContainerSize() {
        return INVENTORY_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        // プレイヤーが8ブロック以内にいるかチェック
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(
                this.worldPosition.getX() + 0.5,
                this.worldPosition.getY() + 0.5,
                this.worldPosition.getZ() + 0.5
        ) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    // ============================================
    // データ保存/読み込み
    // ============================================

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        // インベントリを保存
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        // インベントリを読み込み
        items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    // ============================================
    // MenuProvider実装
    // ============================================

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.anvil.repair_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new RepairStationMenu(containerId, playerInventory, this);
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * インベントリの内容をワールドにドロップ
     *
     * ブロック破壊時に呼び出されます。
     *
     * @param level ワールド
     * @param pos ブロック位置
     */
    public void dropContents(Level level, BlockPos pos) {
        Containers.dropContents(level, pos, items);
    }
}
