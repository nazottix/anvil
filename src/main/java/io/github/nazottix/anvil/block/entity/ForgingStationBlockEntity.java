package io.github.nazottix.anvil.block.entity;

import io.github.nazottix.anvil.block.ForgingStationBlock;
import io.github.nazottix.anvil.data.AnvilDataComponents;
import io.github.nazottix.anvil.data.component.ProcessedMaterialData;
import io.github.nazottix.anvil.item.ProcessedMaterialItem;
import io.github.nazottix.anvil.item.SmithingHammerItem;
import io.github.nazottix.anvil.menu.ForgingStationMenu;
import io.github.nazottix.anvil.processing.ProcessingLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
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
 * 鍛造ステーションブロックエンティティ
 *
 * 精錬素材(REFINED)を鍛造素材(FORGED)に変換します。
 * ハンマーの耐久力を消費して時間経過で自動処理します。
 *
 * スロット構成:
 * - 0: 入力スロット（精錬素材、ProcessingLevel.REFINED以上）
 * - 1: ハンマースロット（SmithingHammerItem）
 * - 2: 出力スロット（鍛造素材）
 */
public class ForgingStationBlockEntity extends BlockEntity implements Container, MenuProvider {

    // インベントリサイズ
    public static final int INVENTORY_SIZE = 3;

    // スロットインデックス
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_HAMMER = 1;
    public static final int SLOT_OUTPUT = 2;

    // 鍛造にかかるtick数（200tick = 10秒）
    public static final int DEFAULT_FORGE_TIME = 200;

    // インベントリ
    private NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

    // 鍛造進捗（0～DEFAULT_FORGE_TIME）
    private int forgeProgress = 0;

    // ContainerData（GUI同期用）
    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> forgeProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> forgeProgress = value;
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
    public ForgingStationBlockEntity(BlockPos pos, BlockState state) {
        super(AnvilBlockEntities.FORGING_STATION.get(), pos, state);
    }

    // ============================================
    // サーバーティック処理
    // ============================================

    /**
     * サーバー側でのティック処理
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, ForgingStationBlockEntity entity) {
        boolean wasForging = entity.forgeProgress > 0;
        boolean canForge = entity.canForge();

        if (canForge) {
            // 鍛造進行
            entity.forgeProgress++;

            if (entity.forgeProgress >= DEFAULT_FORGE_TIME) {
                // 鍛造完了
                entity.forge();
                entity.forgeProgress = 0;
            }

            entity.setChanged();
        } else {
            // 鍛造できない場合は進捗リセット
            if (entity.forgeProgress > 0) {
                entity.forgeProgress = 0;
                entity.setChanged();
            }
        }

        // LIT状態の更新
        boolean isForging = entity.forgeProgress > 0;
        if (wasForging != isForging || state.getValue(ForgingStationBlock.LIT) != isForging) {
            level.setBlock(pos, state.setValue(ForgingStationBlock.LIT, isForging), 3);
        }
    }

    // ============================================
    // 鍛造ロジック
    // ============================================

    /**
     * 鍛造可能かチェック
     */
    private boolean canForge() {
        ItemStack input = items.get(SLOT_INPUT);
        ItemStack hammer = items.get(SLOT_HAMMER);
        ItemStack output = items.get(SLOT_OUTPUT);

        // 入力チェック: 精錬素材（REFINED以上）が必要
        if (input.isEmpty() || !(input.getItem() instanceof ProcessedMaterialItem)) {
            return false;
        }

        ProcessedMaterialData inputData = input.get(AnvilDataComponents.PROCESSED_MATERIAL_DATA.get());
        if (inputData == null) {
            return false;
        }

        // 加工レベルがREFINED（1）であることを確認
        // FORGED以上は鍛造不可（すでに鍛造済み）
        ProcessingLevel inputLevel = ProcessingLevel.fromLevel(inputData.processingLevel());
        if (inputLevel != ProcessingLevel.REFINED) {
            return false;
        }

        // ハンマーチェック: SmithingHammerItemで耐久が残っている
        if (hammer.isEmpty() || !(hammer.getItem() instanceof SmithingHammerItem)) {
            return false;
        }
        if (hammer.getDamageValue() >= hammer.getMaxDamage()) {
            return false;
        }

        // 出力チェック: 空であること
        if (!output.isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * 鍛造を実行
     */
    private void forge() {
        ItemStack input = items.get(SLOT_INPUT);
        ItemStack hammer = items.get(SLOT_HAMMER);

        ProcessedMaterialData inputData = input.get(AnvilDataComponents.PROCESSED_MATERIAL_DATA.get());
        if (inputData == null) return;

        // 出力を作成: レベルをFORGEDに上げる（upgrade()メソッドを使用）
        ProcessedMaterialData outputData = inputData.upgrade();
        ItemStack result = ProcessedMaterialItem.createStack(outputData);

        // 結果をセット
        items.set(SLOT_OUTPUT, result);

        // 入力を1つ消費
        input.shrink(1);

        // ハンマー耐久を1消費
        hammer.setDamageValue(hammer.getDamageValue() + 1);
        if (hammer.getDamageValue() >= hammer.getMaxDamage()) {
            // ハンマー破壊
            items.set(SLOT_HAMMER, ItemStack.EMPTY);
        }

        setChanged();
    }

    /**
     * 入力スロットに置けるかチェック
     */
    public static boolean canInsertInput(ItemStack stack) {
        if (!(stack.getItem() instanceof ProcessedMaterialItem)) {
            return false;
        }
        ProcessedMaterialData data = stack.get(AnvilDataComponents.PROCESSED_MATERIAL_DATA.get());
        if (data == null) {
            return false;
        }
        // REFINEDレベルのみ受け入れ
        return data.processingLevel() == ProcessingLevel.REFINED.getLevel();
    }

    /**
     * ハンマースロットに置けるかチェック
     */
    public static boolean canInsertHammer(ItemStack stack) {
        return stack.getItem() instanceof SmithingHammerItem;
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
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    // ============================================
    // NBT保存・読み込み
    // ============================================

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("ForgeProgress", forgeProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        forgeProgress = tag.getInt("ForgeProgress");
    }

    // ============================================
    // MenuProvider実装
    // ============================================

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.anvil.forging_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ForgingStationMenu(containerId, playerInventory, this, containerData);
    }

    /**
     * インベントリ内容をドロップ
     */
    public void dropContents(Level level, BlockPos pos) {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
    }

    /**
     * ContainerDataを取得
     */
    public ContainerData getContainerData() {
        return containerData;
    }
}
