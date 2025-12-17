package io.github.nazottix.anvil.block.entity;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.block.RefineryBlock;
import io.github.nazottix.anvil.data.component.ProcessedMaterialData;
import io.github.nazottix.anvil.item.ProcessedMaterialItem;
import io.github.nazottix.anvil.menu.RefineryMenu;
import io.github.nazottix.anvil.processing.ProcessingLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 精錬所ブロックエンティティ
 *
 * バニラ素材を加工素材（ProcessedMaterial, REFINED）に変換する処理を行います。
 * 燃料を消費して時間経過で精錬します。
 *
 * スロット構成:
 * - スロット0: 入力スロット（バニラ素材）
 * - スロット1: 燃料スロット
 * - スロット2: 出力スロット（精錬済み素材）
 *
 * 精錬時間: デフォルト200tick（10秒）
 */
public class RefineryBlockEntity extends BlockEntity implements Container, MenuProvider {

    // ============================================
    // 定数
    // ============================================

    // スロットインデックス
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int INVENTORY_SIZE = 3;

    // 精錬時間（tick単位、200tick = 10秒）
    public static final int DEFAULT_REFINE_TIME = 200;

    // ============================================
    // バニラアイテム→素材IDマッピング
    // ============================================

    /**
     * バニラアイテムから素材IDへのマッピング
     * 精錬可能なアイテムを定義します
     */
    private static final Map<Item, ResourceLocation> ITEM_TO_MATERIAL = new HashMap<>();

    static {
        // 金属系
        ITEM_TO_MATERIAL.put(Items.IRON_INGOT, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "iron"));
        ITEM_TO_MATERIAL.put(Items.GOLD_INGOT, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "gold"));
        ITEM_TO_MATERIAL.put(Items.COPPER_INGOT, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "copper"));
        ITEM_TO_MATERIAL.put(Items.NETHERITE_INGOT, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "netherite"));

        // 宝石系
        ITEM_TO_MATERIAL.put(Items.DIAMOND, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "diamond"));
        ITEM_TO_MATERIAL.put(Items.EMERALD, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "emerald"));
        ITEM_TO_MATERIAL.put(Items.AMETHYST_SHARD, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "amethyst"));
        ITEM_TO_MATERIAL.put(Items.LAPIS_LAZULI, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "lapis"));
        ITEM_TO_MATERIAL.put(Items.QUARTZ, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "quartz"));

        // 石材系
        ITEM_TO_MATERIAL.put(Items.COBBLESTONE, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "stone"));
        ITEM_TO_MATERIAL.put(Items.STONE, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "stone"));
        ITEM_TO_MATERIAL.put(Items.FLINT, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "flint"));
        ITEM_TO_MATERIAL.put(Items.OBSIDIAN, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "obsidian"));
        ITEM_TO_MATERIAL.put(Items.BLACKSTONE, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "blackstone"));
        ITEM_TO_MATERIAL.put(Items.DEEPSLATE, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "deepslate"));
        ITEM_TO_MATERIAL.put(Items.END_STONE, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "end_stone"));

        // 木材系
        ITEM_TO_MATERIAL.put(Items.OAK_PLANKS, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "wood"));
        ITEM_TO_MATERIAL.put(Items.SPRUCE_PLANKS, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "wood"));
        ITEM_TO_MATERIAL.put(Items.BIRCH_PLANKS, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "wood"));
        ITEM_TO_MATERIAL.put(Items.JUNGLE_PLANKS, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "wood"));
        ITEM_TO_MATERIAL.put(Items.ACACIA_PLANKS, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "wood"));
        ITEM_TO_MATERIAL.put(Items.DARK_OAK_PLANKS, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "wood"));
        ITEM_TO_MATERIAL.put(Items.BAMBOO_PLANKS, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "bamboo"));

        // 有機系
        ITEM_TO_MATERIAL.put(Items.BONE, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "bone"));
        ITEM_TO_MATERIAL.put(Items.LEATHER, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "leather"));
        ITEM_TO_MATERIAL.put(Items.STRING, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "string"));

        // その他
        ITEM_TO_MATERIAL.put(Items.PRISMARINE_SHARD, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "prismarine"));
        ITEM_TO_MATERIAL.put(Items.ECHO_SHARD, ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "echo"));
    }

    /**
     * アイテムが精錬可能かチェック
     */
    public static boolean canRefine(ItemStack stack) {
        return ITEM_TO_MATERIAL.containsKey(stack.getItem());
    }

    /**
     * アイテムから素材IDを取得
     */
    public static ResourceLocation getMaterialId(ItemStack stack) {
        return ITEM_TO_MATERIAL.get(stack.getItem());
    }

    // ============================================
    // フィールド
    // ============================================

    // インベントリ
    private NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

    // 精錬進捗（0 → DEFAULT_REFINE_TIME）
    private int refineProgress = 0;

    // 燃料残り燃焼時間
    private int fuelBurnTime = 0;

    // 現在の燃料の最大燃焼時間（進捗バー表示用）
    private int currentFuelMaxTime = 0;

    // コンテナデータ（GUIとの同期用）
    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> refineProgress;
                case 1 -> fuelBurnTime;
                case 2 -> currentFuelMaxTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> refineProgress = value;
                case 1 -> fuelBurnTime = value;
                case 2 -> currentFuelMaxTime = value;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    // ============================================
    // コンストラクタ
    // ============================================

    public RefineryBlockEntity(BlockPos pos, BlockState state) {
        super(AnvilBlockEntities.REFINERY.get(), pos, state);
    }

    // ============================================
    // サーバーtick処理
    // ============================================

    /**
     * サーバー側でのtick処理
     * 精錬ロジックを実行します
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, RefineryBlockEntity refinery) {
        boolean wasLit = state.getValue(RefineryBlock.LIT);
        boolean changed = false;

        // 燃料を消費
        if (refinery.fuelBurnTime > 0) {
            refinery.fuelBurnTime--;
        }

        ItemStack inputStack = refinery.items.get(SLOT_INPUT);
        ItemStack outputStack = refinery.items.get(SLOT_OUTPUT);

        // 精錬可能かチェック
        if (canRefine(inputStack) && refinery.canOutput(inputStack, outputStack)) {
            // 燃料が切れていたら補充を試みる
            if (refinery.fuelBurnTime <= 0) {
                refinery.tryConsumeFuel();
            }

            // 燃料があれば精錬を進める
            if (refinery.fuelBurnTime > 0) {
                refinery.refineProgress++;
                changed = true;

                // 精錬完了
                if (refinery.refineProgress >= DEFAULT_REFINE_TIME) {
                    refinery.finishRefining();
                    refinery.refineProgress = 0;
                }
            }
        } else {
            // 精錬できない場合は進捗をリセット
            if (refinery.refineProgress > 0) {
                refinery.refineProgress = 0;
                changed = true;
            }
        }

        // LIT状態を更新
        boolean isLit = refinery.fuelBurnTime > 0;
        if (wasLit != isLit) {
            level.setBlock(pos, state.setValue(RefineryBlock.LIT, isLit), 3);
            changed = true;
        }

        if (changed) {
            refinery.setChanged();
        }
    }

    /**
     * 出力スロットに精錬結果を配置可能かチェック
     */
    private boolean canOutput(ItemStack inputStack, ItemStack outputStack) {
        if (outputStack.isEmpty()) {
            return true;
        }

        // 出力スタックがProcessedMaterialでない場合は不可
        if (!(outputStack.getItem() instanceof ProcessedMaterialItem)) {
            return false;
        }

        // 既存の出力と同じ素材かチェック
        ProcessedMaterialData outputData = ProcessedMaterialItem.getMaterialData(outputStack);
        if (outputData == null) {
            return true;
        }

        ResourceLocation inputMaterial = getMaterialId(inputStack);
        if (inputMaterial == null) {
            return false;
        }

        // 同じ素材で、スタック数が上限未満
        return outputData.materialId().equals(inputMaterial.toString())
                && outputData.processingLevel() == ProcessingLevel.REFINED.getLevel()
                && outputStack.getCount() < outputStack.getMaxStackSize();
    }

    /**
     * 燃料を消費して燃焼時間を取得
     */
    private void tryConsumeFuel() {
        ItemStack fuelStack = items.get(SLOT_FUEL);
        if (!fuelStack.isEmpty()) {
            int burnTime = getBurnTime(fuelStack);
            if (burnTime > 0) {
                fuelBurnTime = burnTime;
                currentFuelMaxTime = burnTime;
                fuelStack.shrink(1);
                setChanged();
            }
        }
    }

    /**
     * アイテムの燃焼時間を取得
     * NeoForge 1.21ではFuelHandlerを使用
     */
    private static int getBurnTime(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        // FuelHandlerからバーンタイムを取得
        return stack.getBurnTime(null);
    }

    /**
     * アイテムが燃料として使用可能かチェック
     */
    public static boolean isFuel(ItemStack stack) {
        return getBurnTime(stack) > 0;
    }

    /**
     * 精錬を完了し、出力を生成
     */
    private void finishRefining() {
        ItemStack inputStack = items.get(SLOT_INPUT);
        ItemStack outputStack = items.get(SLOT_OUTPUT);

        ResourceLocation materialId = getMaterialId(inputStack);
        if (materialId == null) {
            return;
        }

        // 入力を消費
        inputStack.shrink(1);

        // 出力を生成
        if (outputStack.isEmpty()) {
            // 新しい精錬素材を作成
            ItemStack result = ProcessedMaterialItem.createStack(materialId.toString(), ProcessingLevel.REFINED);
            items.set(SLOT_OUTPUT, result);
        } else {
            // 既存のスタックに追加
            outputStack.grow(1);
        }

        setChanged();
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
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("RefineProgress", refineProgress);
        tag.putInt("FuelBurnTime", fuelBurnTime);
        tag.putInt("CurrentFuelMaxTime", currentFuelMaxTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        refineProgress = tag.getInt("RefineProgress");
        fuelBurnTime = tag.getInt("FuelBurnTime");
        currentFuelMaxTime = tag.getInt("CurrentFuelMaxTime");
    }

    // ============================================
    // MenuProvider実装
    // ============================================

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.anvil.refinery");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new RefineryMenu(containerId, playerInventory, this, containerData);
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * インベントリの内容をワールドにドロップ
     */
    public void dropContents(Level level, BlockPos pos) {
        Containers.dropContents(level, pos, items);
    }

    /**
     * コンテナデータを取得
     */
    public ContainerData getContainerData() {
        return containerData;
    }

    /**
     * 精錬進捗率を取得（0.0-1.0）
     */
    public float getRefineProgressRatio() {
        return (float) refineProgress / DEFAULT_REFINE_TIME;
    }

    /**
     * 燃料残量率を取得（0.0-1.0）
     */
    public float getFuelBurnRatio() {
        if (currentFuelMaxTime <= 0) {
            return 0.0f;
        }
        return (float) fuelBurnTime / currentFuelMaxTime;
    }
}
