package io.github.nazottix.anvil.menu;

import io.github.nazottix.anvil.data.AnvilDataComponents;
import io.github.nazottix.anvil.data.component.AnvilToolData;
import io.github.nazottix.anvil.grid.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * グリッドカスタマイズメニュー
 *
 * ツールのコアボックスにモジュールを配置する画面のサーバー側ロジック。
 * Path of Exile風のテトリス配置システムを実装。
 *
 * スロット構成:
 * - 0: ツール入力スロット（カスタマイズ対象）
 * - 1-36: プレイヤーインベントリ + ホットバー
 *
 * 仕様書参照: docs/04_グリッド配置システム.md, docs/10_UI_UXデザイン.md
 */
public class GridCustomizationMenu extends AbstractContainerMenu {

    // コンテナ（ツールスロット用）
    private final Container toolContainer;

    // 同期データ
    // 0: 選択中のモジュールインデックス（-1=なし）
    // 1: 現在の回転（0, 90, 180, 270）
    // 2: カテゴリフィルタ
    private final ContainerData data;

    // 現在編集中のグリッド構成
    private GridConfiguration editingConfig;

    // 利用可能なモジュールリスト
    private List<GridModule> availableModules;

    /**
     * クライアント側コンストラクタ
     */
    public GridCustomizationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(1), new SimpleContainerData(3));
    }

    /**
     * サーバー側コンストラクタ
     */
    public GridCustomizationMenu(int containerId, Inventory playerInventory, Container toolContainer, ContainerData data) {
        super(AnvilMenuTypes.GRID_CUSTOMIZATION.get(), containerId);
        this.toolContainer = toolContainer;
        this.data = data;

        checkContainerSize(toolContainer, 1);
        checkContainerDataCount(data, 3);

        addDataSlots(data);

        // ツール入力スロット
        this.addSlot(new ToolSlot(toolContainer, 0, 26, 35));

        // プレイヤーインベントリ
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 150 + row * 18));
            }
        }

        // ホットバー
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 208));
        }

        // 初期化
        this.editingConfig = GridConfiguration.DEFAULT;
        this.availableModules = new ArrayList<>(GridModuleRegistry.getAll().values());

        // 選択インデックス初期化
        data.set(0, -1);
        data.set(1, 0);
        data.set(2, 0);
    }

    // ============================================
    // ツール変更検知
    // ============================================

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);

        // ツールが変更されたらグリッド構成を読み込み
        ItemStack toolStack = toolContainer.getItem(0);
        if (!toolStack.isEmpty() && toolStack.has(AnvilDataComponents.GRID_CONFIG.get())) {
            this.editingConfig = toolStack.get(AnvilDataComponents.GRID_CONFIG.get());
        } else {
            this.editingConfig = GridConfiguration.DEFAULT;
        }

        // コアボックスサイズをツールレベルに応じて更新
        updateCoreBoxSize();
    }

    /**
     * ツールレベルに応じてコアボックスサイズを更新
     */
    private void updateCoreBoxSize() {
        int level = getToolLevel();
        // getMaxForLevelを使用してレベルに応じた最大サイズを取得
        CoreBoxSize appropriateSize = CoreBoxSize.getMaxForLevel(level);

        if (editingConfig.size().ordinal() < appropriateSize.ordinal()) {
            editingConfig = editingConfig.withSize(appropriateSize);
        }
    }

    // ============================================
    // モジュール配置操作
    // ============================================

    /**
     * モジュールを配置
     *
     * @param moduleId モジュールID
     * @param x X座標
     * @param y Y座標
     * @param rotation 回転（0, 90, 180, 270）
     * @return 成功した場合true
     */
    public boolean placeModule(ResourceLocation moduleId, int x, int y, int rotation) {
        GridModule module = GridModuleRegistry.get(moduleId);
        if (module == null) {
            return false;
        }

        // 配置可能かチェック
        if (!editingConfig.canPlace(module, x, y, rotation)) {
            return false;
        }

        // モジュールを配置
        PlacedModule placed = new PlacedModule(moduleId, x, y, rotation);
        editingConfig = editingConfig.withAddedModule(placed);

        return true;
    }

    /**
     * モジュールを削除
     *
     * @param index 削除するモジュールのインデックス
     * @return 削除されたモジュール（なければnull）
     */
    public PlacedModule removeModule(int index) {
        if (index < 0 || index >= editingConfig.placedModules().size()) {
            return null;
        }

        PlacedModule removed = editingConfig.placedModules().get(index);
        editingConfig = editingConfig.withRemovedModule(index);

        return removed;
    }

    /**
     * 指定座標にあるモジュールを削除
     *
     * @param x X座標
     * @param y Y座標
     * @return 削除されたモジュール（なければnull）
     */
    public PlacedModule removeModuleAt(int x, int y) {
        int index = getModuleIndexAt(x, y);
        if (index >= 0) {
            return removeModule(index);
        }
        return null;
    }

    /**
     * 指定座標にあるモジュールのインデックスを取得
     *
     * @param x X座標
     * @param y Y座標
     * @return モジュールインデックス（-1=なし）
     */
    public int getModuleIndexAt(int x, int y) {
        for (int i = 0; i < editingConfig.placedModules().size(); i++) {
            PlacedModule placed = editingConfig.placedModules().get(i);
            GridModule module = GridModuleRegistry.get(placed.moduleId());
            if (module != null) {
                int[][] cells = module.getOccupiedCells(placed.x(), placed.y(), placed.rotation());
                for (int[] cell : cells) {
                    if (cell[0] == x && cell[1] == y) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * モジュールを回転
     *
     * @param index モジュールインデックス
     * @param clockwise 時計回りかどうか
     * @return 成功した場合true
     */
    public boolean rotateModule(int index, boolean clockwise) {
        if (index < 0 || index >= editingConfig.placedModules().size()) {
            return false;
        }

        PlacedModule placed = editingConfig.placedModules().get(index);
        int newRotation = (placed.rotation() + (clockwise ? 90 : 270)) % 360;

        // 回転後に配置可能かチェック
        GridModule module = GridModuleRegistry.get(placed.moduleId());
        if (module == null) {
            return false;
        }

        // 一旦削除してから配置し直す
        GridConfiguration tempConfig = editingConfig.withRemovedModule(index);
        if (module.canPlaceAt(tempConfig.size().getGridSize(), placed.x(), placed.y(), newRotation, tempConfig.getOccupiedCells())) {
            PlacedModule rotated = new PlacedModule(placed.moduleId(), placed.x(), placed.y(), newRotation);
            editingConfig = tempConfig.withAddedModule(rotated);
            return true;
        }

        return false;
    }

    /**
     * 全モジュールをクリア
     */
    public void clearAllModules() {
        editingConfig = editingConfig.clear();
    }

    /**
     * 変更を適用してツールに保存
     */
    public void applyChanges() {
        ItemStack toolStack = toolContainer.getItem(0);
        if (!toolStack.isEmpty() && toolStack.has(AnvilDataComponents.TOOL_DATA.get())) {
            toolStack.set(AnvilDataComponents.GRID_CONFIG.get(), editingConfig);
        }
    }

    /**
     * 変更をリセット
     */
    public void resetChanges() {
        ItemStack toolStack = toolContainer.getItem(0);
        if (!toolStack.isEmpty() && toolStack.has(AnvilDataComponents.GRID_CONFIG.get())) {
            this.editingConfig = toolStack.get(AnvilDataComponents.GRID_CONFIG.get());
        } else {
            this.editingConfig = GridConfiguration.DEFAULT;
        }
        updateCoreBoxSize();
    }

    // ============================================
    // 情報取得
    // ============================================

    /**
     * 現在のグリッド構成を取得
     */
    public GridConfiguration getEditingConfig() {
        return editingConfig;
    }

    /**
     * ツールのレベルを取得
     */
    public int getToolLevel() {
        ItemStack toolStack = toolContainer.getItem(0);
        if (!toolStack.isEmpty() && toolStack.has(AnvilDataComponents.TOOL_DATA.get())) {
            AnvilToolData data = toolStack.get(AnvilDataComponents.TOOL_DATA.get());
            return data.level();
        }
        return 1;
    }

    /**
     * 現在の総重量を取得
     */
    public int getTotalWeight() {
        return editingConfig.getTotalWeight();
    }

    /**
     * 速度ランクを取得
     */
    public WeightSystem.SpeedRank getSpeedRank() {
        return WeightSystem.calculateRank(editingConfig);
    }

    /**
     * 使用セル数を取得
     */
    public int getUsedCells() {
        return editingConfig.getUsedCellCount();
    }

    /**
     * 総セル数を取得
     */
    public int getTotalCells() {
        return editingConfig.size().getTotalCells();
    }

    /**
     * 利用可能なモジュールリストを取得
     */
    public List<GridModule> getAvailableModules() {
        return availableModules;
    }

    /**
     * 選択中のモジュールインデックスを取得
     */
    public int getSelectedModuleIndex() {
        return data.get(0);
    }

    /**
     * モジュールを選択
     */
    public void setSelectedModuleIndex(int index) {
        data.set(0, index);
    }

    /**
     * 現在の回転を取得
     */
    public int getCurrentRotation() {
        return data.get(1);
    }

    /**
     * 回転を設定
     */
    public void setCurrentRotation(int rotation) {
        data.set(1, rotation % 360);
    }

    /**
     * 回転を進める
     */
    public void rotateCurrentSelection(boolean clockwise) {
        int current = getCurrentRotation();
        int newRotation = (current + (clockwise ? 90 : 270)) % 360;
        setCurrentRotation(newRotation);
    }

    /**
     * カテゴリフィルタを取得
     */
    public int getCategoryFilter() {
        return data.get(2);
    }

    /**
     * カテゴリフィルタを設定
     */
    public void setCategoryFilter(int filter) {
        data.set(2, filter);
    }

    /**
     * 占有セルのセットを取得
     */
    public Set<Long> getOccupiedCells() {
        return editingConfig.getOccupiedCells();
    }

    // ============================================
    // AbstractContainerMenu実装
    // ============================================

    @Override
    public boolean stillValid(Player player) {
        return toolContainer.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            // ツールスロット(0)からの移動
            if (index == 0) {
                if (!this.moveItemStackTo(slotStack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // プレイヤーインベントリ/ホットバーからの移動
            else {
                // ANVILツールならツールスロットへ
                if (slotStack.has(AnvilDataComponents.TOOL_DATA.get())) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // それ以外はインベントリ/ホットバー間で移動
                else if (index < 28) {
                    if (!this.moveItemStackTo(slotStack, 28, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.moveItemStackTo(slotStack, 1, 28, false)) {
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
    // カスタムスロット
    // ============================================

    /**
     * ツールスロット（ANVILツールのみ受け入れ）
     */
    private static class ToolSlot extends Slot {
        public ToolSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.has(AnvilDataComponents.TOOL_DATA.get());
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
