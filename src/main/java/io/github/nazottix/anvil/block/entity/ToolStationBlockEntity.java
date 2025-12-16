package io.github.nazottix.anvil.block.entity;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.menu.ToolStationMenu;
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
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * ツールステーションブロックエンティティ
 *
 * ツール作成用のインベントリとデータを管理します。
 *
 * スロット構成:
 * - スロット0-3: パーツ入力スロット（最大4パーツ対応、弓等）
 * - スロット4: 出力スロット（完成したツール）
 * - スロット5: ツール入力スロット（修理・改造用）
 *
 * 仕様書参照: docs/08_入手_リスペックシステム.md
 */
public class ToolStationBlockEntity extends BlockEntity implements Container, MenuProvider {

    // インベントリサイズ（4パーツスロット対応に拡張）
    public static final int SLOT_PART_1 = 0;       // パーツスロット1（ヘッド/ボウリム等）
    public static final int SLOT_PART_2 = 1;       // パーツスロット2（ハンドル等）
    public static final int SLOT_PART_3 = 2;       // パーツスロット3（バインディング/ボウリム等）
    public static final int SLOT_PART_4 = 3;       // パーツスロット4（ボウストリング等）
    public static final int SLOT_OUTPUT = 4;       // 出力スロット
    public static final int SLOT_TOOL_INPUT = 5;   // ツール入力スロット（修理用）
    public static final int INVENTORY_SIZE = 6;    // 合計6スロット

    // インベントリ
    private NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

    // 選択されているツールタイプ（0-7: ピッケル、斧、シャベル、剣、クワ、弓、釣り竿、ハサミ）
    private int selectedToolType = 0;

    // コンテナデータ（GUIとの同期用）
    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> selectedToolType;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> selectedToolType = value;
            }
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    /**
     * コンストラクタ
     */
    public ToolStationBlockEntity(BlockPos pos, BlockState state) {
        super(AnvilBlockEntities.TOOL_STATION.get(), pos, state);
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
        // 選択されたツールタイプを保存
        tag.putInt("SelectedToolType", selectedToolType);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        // インベントリを読み込み
        items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        // 選択されたツールタイプを読み込み
        selectedToolType = tag.getInt("SelectedToolType");
    }

    // ============================================
    // MenuProvider実装
    // ============================================

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.anvil.tool_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ToolStationMenu(containerId, playerInventory, this, containerData);
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

    /**
     * 選択されているツールタイプを取得
     */
    public int getSelectedToolType() {
        return selectedToolType;
    }

    /**
     * ツールタイプを設定
     */
    public void setSelectedToolType(int toolType) {
        this.selectedToolType = toolType;
        setChanged();
    }

    /**
     * コンテナデータを取得
     */
    public ContainerData getContainerData() {
        return containerData;
    }
}
