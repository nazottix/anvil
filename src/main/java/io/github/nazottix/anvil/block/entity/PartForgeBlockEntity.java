package io.github.nazottix.anvil.block.entity;

import io.github.nazottix.anvil.item.PartItem;
import io.github.nazottix.anvil.menu.PartForgeMenu;
import io.github.nazottix.anvil.material.Grade;
import io.github.nazottix.anvil.material.Material;
import io.github.nazottix.anvil.material.MaterialRegistry;
import io.github.nazottix.anvil.tool.PartType;
import java.util.Optional;
import java.util.Random;
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
 * パーツ鍛造所ブロックエンティティ
 *
 * バニラ素材（鉄インゴット、ダイヤモンドなど）を直接パーツに変換します。
 * 素材加工の簡略化により、精錬・鍛造・研磨の工程を省略して
 * 原材料から直接パーツを作成できます。
 *
 * スロット構成:
 * - 0: 入力スロット（バニラ素材）
 * - 1: 出力スロット（パーツ）
 */
public class PartForgeBlockEntity extends BlockEntity implements Container, MenuProvider {

    // インベントリサイズ
    public static final int INVENTORY_SIZE = 2;

    // スロットインデックス
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;

    // インベントリ
    private NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

    // 選択中のパーツタイプインデックス（PartType.values()のインデックス）
    private int selectedPartTypeIndex = 0;

    // グレードロール用の乱数ジェネレーター
    private final Random random = new Random();

    // ContainerData（GUI同期用）
    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> selectedPartTypeIndex;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> selectedPartTypeIndex = value;
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
    public PartForgeBlockEntity(BlockPos pos, BlockState state) {
        super(AnvilBlockEntities.PART_FORGE.get(), pos, state);
    }

    // ============================================
    // パーツ鋳造ロジック
    // ============================================

    /**
     * 鋳造可能かチェック
     *
     * @return 鋳造可能な場合true
     */
    public boolean canForge() {
        ItemStack input = items.get(SLOT_INPUT);
        ItemStack output = items.get(SLOT_OUTPUT);

        // 入力チェック: 有効な素材原料が必要
        if (input.isEmpty()) {
            return false;
        }

        // 素材レジストリから対応する素材を検索
        Optional<Material> materialOpt = MaterialRegistry.getInstance().getByRepairItem(input.getItem());
        if (materialOpt.isEmpty()) {
            return false;
        }

        // 選択中のパーツタイプを取得
        PartType partType = getSelectedPartType();
        if (partType == null) {
            return false;
        }

        // 出力スロットチェック: 空または同じパーツでスタック可能
        if (!output.isEmpty()) {
            // 出力が空でない場合、同じパーツかつスタック可能か確認
            // パーツは基本的に64スタック可能だが、異なるパーツ/素材/グレードは別アイテムとして扱う
            return false; // 簡略化のため、出力が空の場合のみ許可
        }

        return true;
    }

    /**
     * 鋳造を実行
     *
     * バニラ素材から直接パーツを作成します。
     * グレードは確率に基づいてランダムに決定されます。
     *
     * @return 鋳造成功した場合true
     */
    public boolean forge() {
        if (!canForge()) {
            return false;
        }

        ItemStack input = items.get(SLOT_INPUT);
        PartType partType = getSelectedPartType();

        // 素材レジストリから対応する素材を検索
        Optional<Material> materialOpt = MaterialRegistry.getInstance().getByRepairItem(input.getItem());
        if (materialOpt.isEmpty()) {
            return false;
        }

        Material material = materialOpt.get();

        // グレードをランダムに決定（確率に基づく）
        Grade grade = Grade.rollGrade(random);

        // パーツを作成
        ItemStack result = PartItem.createPartStack(partType, material.getId().toString(), grade);

        // 結果をセット
        items.set(SLOT_OUTPUT, result);

        // 入力を1つ消費
        input.shrink(1);

        setChanged();
        return true;
    }

    /**
     * 選択中のパーツタイプを取得
     */
    public PartType getSelectedPartType() {
        PartType[] types = PartType.values();
        if (selectedPartTypeIndex >= 0 && selectedPartTypeIndex < types.length) {
            return types[selectedPartTypeIndex];
        }
        return null;
    }

    /**
     * パーツタイプを選択
     *
     * @param index パーツタイプインデックス
     */
    public void setSelectedPartTypeIndex(int index) {
        PartType[] types = PartType.values();
        if (index >= 0 && index < types.length) {
            this.selectedPartTypeIndex = index;
            setChanged();
        }
    }

    /**
     * アイテムが入力スロットに置けるかチェック
     *
     * MaterialRegistryに登録されている素材の原料アイテムのみ受け入れます。
     */
    public static boolean canInsertInput(ItemStack stack) {
        // 有効な素材原料のみ受け入れ
        return MaterialRegistry.getInstance().isValidMaterialIngredient(stack.getItem());
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
        tag.putInt("SelectedPartType", selectedPartTypeIndex);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        selectedPartTypeIndex = tag.getInt("SelectedPartType");
    }

    // ============================================
    // MenuProvider実装
    // ============================================

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.anvil.part_forge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new PartForgeMenu(containerId, playerInventory, this, containerData);
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
     * ContainerDataを取得（外部アクセス用）
     */
    public ContainerData getContainerData() {
        return containerData;
    }
}
