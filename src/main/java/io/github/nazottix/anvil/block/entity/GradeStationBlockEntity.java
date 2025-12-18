package io.github.nazottix.anvil.block.entity;

import io.github.nazottix.anvil.data.AnvilDataComponents;
import io.github.nazottix.anvil.data.component.ToolPart;
import io.github.nazottix.anvil.item.AnvilItems;
import io.github.nazottix.anvil.item.PartItem;
import io.github.nazottix.anvil.material.Grade;
import io.github.nazottix.anvil.menu.GradeStationMenu;
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
 * グレードステーションブロックエンティティ
 *
 * パーツのグレードアップグレード用のインベントリとデータを管理します。
 *
 * スロット構成:
 * - スロット0: パーツ入力スロット（アップグレード対象のパーツ）
 * - スロット1: エッセンス入力スロット（グレードエッセンス）
 *
 * グレードアップグレードに必要なエッセンス数:
 * - E → D: 1個
 * - D → C: 2個
 * - C → B: 3個
 * - B → A: 4個
 * - A → S: 5個
 * - S → SS: 6個
 * - SS → SSS: 8個
 * - SSS → MAX: 10個
 *
 * 仕様書参照: docs/01_パーツ_素材システム.md - 3.5 グレードシステム
 */
public class GradeStationBlockEntity extends BlockEntity implements Container, MenuProvider {

    // スロットインデックス
    public static final int SLOT_PART_INPUT = 0;     // パーツ入力スロット（アップグレード対象）
    public static final int SLOT_ESSENCE_INPUT = 1;  // エッセンス入力スロット（グレードエッセンス）
    public static final int INVENTORY_SIZE = 2;      // 合計2スロット

    // インベントリ
    private NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

    /**
     * コンストラクタ
     */
    public GradeStationBlockEntity(BlockPos pos, BlockState state) {
        super(AnvilBlockEntities.GRADE_STATION.get(), pos, state);
    }

    // ============================================
    // グレードアップグレードロジック
    // ============================================

    /**
     * 現在のグレードから次のグレードに必要なエッセンス数を取得
     *
     * @param currentGrade 現在のグレード
     * @return 必要なエッセンス数、MAXの場合は-1
     */
    public static int getRequiredEssence(Grade currentGrade) {
        return switch (currentGrade) {
            case E -> 1;
            case D -> 2;
            case C -> 3;
            case B -> 4;
            case A -> 5;
            case S -> 6;
            case SS -> 8;
            case SSS -> 10;
            case MAX -> -1; // MAXは上げられない
        };
    }

    /**
     * 次のグレードを取得
     *
     * @param currentGrade 現在のグレード
     * @return 次のグレード、MAXの場合はnull
     */
    public static Grade getNextGrade(Grade currentGrade) {
        Grade[] grades = Grade.values();
        int currentIndex = currentGrade.ordinal();
        if (currentIndex < grades.length - 1) {
            return grades[currentIndex + 1];
        }
        return null; // MAXの場合
    }

    /**
     * アップグレード可能かどうかをチェック
     *
     * @return アップグレード可能な場合true
     */
    public boolean canUpgrade() {
        ItemStack partStack = items.get(SLOT_PART_INPUT);
        ItemStack essenceStack = items.get(SLOT_ESSENCE_INPUT);

        // パーツがない場合
        if (partStack.isEmpty() || !PartItem.hasValidPartData(partStack)) {
            return false;
        }

        // グレードエッセンスがない場合
        if (essenceStack.isEmpty() || essenceStack.getItem() != AnvilItems.GRADE_ESSENCE.get()) {
            return false;
        }

        // パーツのグレードを取得
        ToolPart partData = partStack.get(AnvilDataComponents.PART_DATA.get());
        if (partData == null) {
            return false;
        }

        // StringからGrade enumに変換
        Grade currentGrade = Grade.fromId(partData.grade());
        Grade nextGrade = getNextGrade(currentGrade);

        // MAXの場合はアップグレード不可
        if (nextGrade == null) {
            return false;
        }

        // 必要なエッセンス数を確認
        int requiredEssence = getRequiredEssence(currentGrade);
        return essenceStack.getCount() >= requiredEssence;
    }

    /**
     * グレードをアップグレード
     *
     * @return アップグレード成功した場合true
     */
    public boolean upgrade() {
        if (!canUpgrade()) {
            return false;
        }

        ItemStack partStack = items.get(SLOT_PART_INPUT);
        ItemStack essenceStack = items.get(SLOT_ESSENCE_INPUT);

        // パーツのグレードを取得（StringからGrade enumに変換）
        ToolPart partData = partStack.get(AnvilDataComponents.PART_DATA.get());
        Grade currentGrade = Grade.fromId(partData.grade());
        Grade nextGrade = getNextGrade(currentGrade);

        // 必要なエッセンスを消費
        int requiredEssence = getRequiredEssence(currentGrade);
        essenceStack.shrink(requiredEssence);

        // 新しいパーツデータを作成（グレードを更新、次のグレードをStringに変換）
        ToolPart newPartData = new ToolPart(
                partData.materialId(),
                partData.partType(),
                nextGrade.getId()
        );

        // パーツにセット
        partStack.set(AnvilDataComponents.PART_DATA.get(), newPartData);

        // コンテナを更新
        setChanged();

        return true;
    }

    /**
     * 現在のパーツのグレードを取得
     *
     * @return グレード、パーツがない場合はnull
     */
    @Nullable
    public Grade getCurrentGrade() {
        ItemStack partStack = items.get(SLOT_PART_INPUT);
        if (partStack.isEmpty() || !PartItem.hasValidPartData(partStack)) {
            return null;
        }
        ToolPart partData = partStack.get(AnvilDataComponents.PART_DATA.get());
        // StringからGrade enumに変換
        return partData != null ? Grade.fromId(partData.grade()) : null;
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
        return Component.translatable("block.anvil.grade_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new GradeStationMenu(containerId, playerInventory, this);
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
