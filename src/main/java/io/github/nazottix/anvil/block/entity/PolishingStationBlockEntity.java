package io.github.nazottix.anvil.block.entity;

import io.github.nazottix.anvil.block.PolishingStationBlock;
import io.github.nazottix.anvil.data.AnvilDataComponents;
import io.github.nazottix.anvil.data.component.ProcessedMaterialData;
import io.github.nazottix.anvil.item.PolishingAgentItem;
import io.github.nazottix.anvil.item.ProcessedMaterialItem;
import io.github.nazottix.anvil.menu.PolishingStationMenu;
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
 * 研磨ステーションブロックエンティティ
 *
 * 鍛造素材(FORGED)を研磨素材(POLISHED)に変換します。
 * 研磨剤を消費して品質ボーナスを付与します。
 *
 * スロット構成:
 * - 0: 入力スロット（鍛造素材、ProcessingLevel.FORGED以上）
 * - 1: 研磨剤スロット（PolishingAgentItem）
 * - 2: 出力スロット（研磨素材）
 */
public class PolishingStationBlockEntity extends BlockEntity implements Container, MenuProvider {

    // インベントリサイズ
    public static final int INVENTORY_SIZE = 3;

    // スロットインデックス
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_AGENT = 1;
    public static final int SLOT_OUTPUT = 2;

    // 研磨にかかるtick数（160tick = 8秒）
    public static final int DEFAULT_POLISH_TIME = 160;

    // インベントリ
    private NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

    // 研磨進捗（0～DEFAULT_POLISH_TIME）
    private int polishProgress = 0;

    // ContainerData（GUI同期用）
    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> polishProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> polishProgress = value;
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
    public PolishingStationBlockEntity(BlockPos pos, BlockState state) {
        super(AnvilBlockEntities.POLISHING_STATION.get(), pos, state);
    }

    // ============================================
    // サーバーティック処理
    // ============================================

    /**
     * サーバー側でのティック処理
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, PolishingStationBlockEntity entity) {
        boolean wasPolishing = entity.polishProgress > 0;
        boolean canPolish = entity.canPolish();

        if (canPolish) {
            // 研磨進行
            entity.polishProgress++;

            if (entity.polishProgress >= DEFAULT_POLISH_TIME) {
                // 研磨完了
                entity.polish();
                entity.polishProgress = 0;
            }

            entity.setChanged();
        } else {
            // 研磨できない場合は進捗リセット
            if (entity.polishProgress > 0) {
                entity.polishProgress = 0;
                entity.setChanged();
            }
        }

        // LIT状態の更新
        boolean isPolishing = entity.polishProgress > 0;
        if (wasPolishing != isPolishing || state.getValue(PolishingStationBlock.LIT) != isPolishing) {
            level.setBlock(pos, state.setValue(PolishingStationBlock.LIT, isPolishing), 3);
        }
    }

    // ============================================
    // 研磨ロジック
    // ============================================

    /**
     * 研磨可能かチェック
     */
    private boolean canPolish() {
        ItemStack input = items.get(SLOT_INPUT);
        ItemStack agent = items.get(SLOT_AGENT);
        ItemStack output = items.get(SLOT_OUTPUT);

        // 入力チェック: 鍛造素材（FORGED以上）が必要
        if (input.isEmpty() || !(input.getItem() instanceof ProcessedMaterialItem)) {
            return false;
        }

        ProcessedMaterialData inputData = input.get(AnvilDataComponents.PROCESSED_MATERIAL_DATA.get());
        if (inputData == null) {
            return false;
        }

        // 加工レベルがFORGED（2）であることを確認
        // POLISHED以上は研磨不可（すでに研磨済み）
        ProcessingLevel inputLevel = ProcessingLevel.fromLevel(inputData.processingLevel());
        if (inputLevel != ProcessingLevel.FORGED) {
            return false;
        }

        // 研磨剤チェック
        if (agent.isEmpty() || !PolishingAgentItem.isPolishingAgent(agent)) {
            return false;
        }

        // 出力チェック: 空であること
        if (!output.isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * 研磨を実行
     */
    private void polish() {
        ItemStack input = items.get(SLOT_INPUT);
        ItemStack agent = items.get(SLOT_AGENT);

        ProcessedMaterialData inputData = input.get(AnvilDataComponents.PROCESSED_MATERIAL_DATA.get());
        if (inputData == null) return;

        // 研磨剤の品質値を取得
        float agentQuality = PolishingAgentItem.getQualityValue(agent);

        // 出力を作成: レベルをPOLISHEDに上げ、品質ボーナスを付与
        ProcessedMaterialData outputData = inputData.upgrade().addQualityBonus(agentQuality);
        ItemStack result = ProcessedMaterialItem.createStack(outputData);

        // 結果をセット
        items.set(SLOT_OUTPUT, result);

        // 入力を1つ消費
        input.shrink(1);

        // 研磨剤を1つ消費
        agent.shrink(1);

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
        // FORGEDレベルのみ受け入れ
        return data.processingLevel() == ProcessingLevel.FORGED.getLevel();
    }

    /**
     * 研磨剤スロットに置けるかチェック
     */
    public static boolean canInsertAgent(ItemStack stack) {
        return PolishingAgentItem.isPolishingAgent(stack);
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
        tag.putInt("PolishProgress", polishProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        polishProgress = tag.getInt("PolishProgress");
    }

    // ============================================
    // MenuProvider実装
    // ============================================

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.anvil.polishing_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new PolishingStationMenu(containerId, playerInventory, this, containerData);
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
