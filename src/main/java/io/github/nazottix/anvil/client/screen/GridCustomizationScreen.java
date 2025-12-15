package io.github.nazottix.anvil.client.screen;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.client.ui.AnvilColors;
import io.github.nazottix.anvil.grid.*;
import io.github.nazottix.anvil.menu.GridCustomizationMenu;
import io.github.nazottix.anvil.mod.ModEffect;
import io.github.nazottix.anvil.mod.ModRarity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * グリッドカスタマイズスクリーン
 *
 * Path of Exile風のテトリス配置UI。
 * コアボックスにモジュールをドラッグ＆ドロップで配置。
 *
 * 仕様書参照: docs/04_グリッド配置システム.md, docs/10_UI_UXデザイン.md
 */
public class GridCustomizationScreen extends AbstractContainerScreen<GridCustomizationMenu> {

    // UI定数
    private static final int CELL_SIZE = 16;
    private static final int GRID_X = 60;
    private static final int GRID_Y = 20;
    private static final int MODULE_LIST_X = 200;
    private static final int MODULE_LIST_Y = 20;
    private static final int MODULE_LIST_WIDTH = 100;
    private static final int MODULE_LIST_HEIGHT = 110;

    // ドラッグ中のモジュール
    private GridModule draggedModule = null;
    private int draggedRotation = 0;

    // ホバー中のグリッド座標
    private int hoverGridX = -1;
    private int hoverGridY = -1;

    // モジュールリストのスクロール位置
    private int moduleListScrollOffset = 0;

    // フィルタリングされたモジュールリスト
    private List<GridModule> filteredModules = new ArrayList<>();

    /**
     * コンストラクタ
     */
    public GridCustomizationScreen(GridCustomizationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // GUIサイズを拡大
        this.imageWidth = 320;
        this.imageHeight = 232;
        this.inventoryLabelY = this.imageHeight - 82;
    }

    @Override
    protected void init() {
        super.init();

        // 適用ボタン
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.anvil.apply"),
                button -> {
                    this.menu.applyChanges();
                    ANVIL.LOGGER.info("グリッド構成を適用しました");
                }
        ).bounds(this.leftPos + 8, this.topPos + 115, 45, 18).build());

        // クリアボタン
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.anvil.clear"),
                button -> {
                    this.menu.clearAllModules();
                    ANVIL.LOGGER.info("グリッドをクリアしました");
                }
        ).bounds(this.leftPos + 8, this.topPos + 135, 45, 18).build());

        // 回転ボタン（R）
        this.addRenderableWidget(Button.builder(
                Component.literal("R"),
                button -> {
                    this.menu.rotateCurrentSelection(true);
                    if (draggedModule != null) {
                        draggedRotation = (draggedRotation + 90) % 360;
                    }
                }
        ).bounds(this.leftPos + MODULE_LIST_X + MODULE_LIST_WIDTH - 20, this.topPos + MODULE_LIST_Y - 2, 20, 18).build());

        // モジュールリスト更新
        updateFilteredModules();
    }

    // ============================================
    // 描画
    // ============================================

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // 背景色
        guiGraphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight,
                AnvilColors.VOID_BLACK | 0xFF000000);

        // パネル背景
        guiGraphics.fill(this.leftPos + 2, this.topPos + 2, this.leftPos + this.imageWidth - 2, this.topPos + this.imageHeight - 2,
                AnvilColors.ANVIL_STEEL | 0xFF000000);

        // グリッド描画
        renderGrid(guiGraphics, mouseX, mouseY);

        // 配置済みモジュール描画
        renderPlacedModules(guiGraphics);

        // ドラッグプレビュー描画
        renderDragPreview(guiGraphics, mouseX, mouseY);

        // モジュールリスト描画
        renderModuleList(guiGraphics, mouseX, mouseY);

        // 重量・速度ランク表示
        renderWeightInfo(guiGraphics);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // ドラッグ中のモジュール描画
        if (draggedModule != null) {
            renderDraggedModule(guiGraphics, mouseX, mouseY);
        }

        // モジュールツールチップ
        renderModuleTooltips(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // タイトル
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY,
                AnvilColors.ETHER_WHITE, false);

        // インベントリラベル
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY,
                AnvilColors.ETHER_WHITE, false);

        // コアボックスサイズ表示
        GridConfiguration config = this.menu.getEditingConfig();
        String sizeText = String.format("コアボックス (%dx%d)",
                config.size().getGridSize(), config.size().getGridSize());
        guiGraphics.drawString(this.font, sizeText, GRID_X, GRID_Y - 12, AnvilColors.TECH_CYAN, false);
    }

    /**
     * グリッドを描画
     */
    private void renderGrid(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        GridConfiguration config = this.menu.getEditingConfig();
        int gridSize = config.size().getGridSize();
        int guiX = this.leftPos + GRID_X;
        int guiY = this.topPos + GRID_Y;

        Set<Long> occupiedCells = config.getOccupiedCells();

        // グリッド枠
        int gridPixelSize = gridSize * CELL_SIZE;
        guiGraphics.fill(guiX - 1, guiY - 1, guiX + gridPixelSize + 1, guiY + gridPixelSize + 1, 0xFF444444);

        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                int cellX = guiX + x * CELL_SIZE;
                int cellY = guiY + y * CELL_SIZE;

                // セル背景
                long cellKey = ((long) x << 32) | (y & 0xFFFFFFFFL);
                int cellColor = occupiedCells.contains(cellKey) ? 0xFF333333 : AnvilColors.VOID_BLACK | 0xFF000000;
                guiGraphics.fill(cellX, cellY, cellX + CELL_SIZE - 1, cellY + CELL_SIZE - 1, cellColor);

                // ホバーハイライト
                if (isMouseOverCell(mouseX, mouseY, cellX, cellY)) {
                    hoverGridX = x;
                    hoverGridY = y;
                    guiGraphics.fill(cellX, cellY, cellX + CELL_SIZE - 1, cellY + CELL_SIZE - 1, 0x44FFFFFF);
                }
            }
        }
    }

    /**
     * 配置済みモジュールを描画
     */
    private void renderPlacedModules(GuiGraphics guiGraphics) {
        GridConfiguration config = this.menu.getEditingConfig();
        int guiX = this.leftPos + GRID_X;
        int guiY = this.topPos + GRID_Y;

        for (PlacedModule placed : config.placedModules()) {
            GridModule module = GridModuleRegistry.get(placed.moduleId());
            if (module == null) continue;

            int[][] cells = module.getOccupiedCells(placed.x(), placed.y(), placed.rotation());

            // モジュールの色（カテゴリ別）
            int moduleColor = getCategoryColor(module.category()) | 0xCC000000;

            // 各セルを描画
            for (int[] cell : cells) {
                int cellX = guiX + cell[0] * CELL_SIZE;
                int cellY = guiY + cell[1] * CELL_SIZE;
                guiGraphics.fill(cellX + 1, cellY + 1, cellX + CELL_SIZE - 2, cellY + CELL_SIZE - 2, moduleColor);
            }

            // モジュール名（最初のセルに表示）
            int firstCellX = guiX + placed.x() * CELL_SIZE;
            int firstCellY = guiY + placed.y() * CELL_SIZE;
            String shortName = module.id().getPath().substring(0, Math.min(3, module.id().getPath().length()));
            guiGraphics.drawString(this.font, shortName, firstCellX + 2, firstCellY + 4, 0xFFFFFF, true);
        }
    }

    /**
     * ドラッグプレビューを描画
     */
    private void renderDragPreview(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (draggedModule == null || hoverGridX < 0 || hoverGridY < 0) return;

        GridConfiguration config = this.menu.getEditingConfig();
        int guiX = this.leftPos + GRID_X;
        int guiY = this.topPos + GRID_Y;

        // 配置可能かチェック
        boolean canPlace = config.canPlace(draggedModule, hoverGridX, hoverGridY, draggedRotation);
        int previewColor = canPlace ? 0x4400FF00 : 0x44FF0000;

        int[][] cells = draggedModule.shape().getRotatedCells(draggedRotation);
        for (int[] cell : cells) {
            int cellX = hoverGridX + cell[0];
            int cellY = hoverGridY + cell[1];

            if (cellX >= 0 && cellX < config.size().getGridSize() &&
                    cellY >= 0 && cellY < config.size().getGridSize()) {
                int pixelX = guiX + cellX * CELL_SIZE;
                int pixelY = guiY + cellY * CELL_SIZE;
                guiGraphics.fill(pixelX, pixelY, pixelX + CELL_SIZE - 1, pixelY + CELL_SIZE - 1, previewColor);
            }
        }
    }

    /**
     * モジュールリストを描画
     */
    private void renderModuleList(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = this.leftPos + MODULE_LIST_X;
        int y = this.topPos + MODULE_LIST_Y;

        // リスト背景
        guiGraphics.fill(x, y, x + MODULE_LIST_WIDTH, y + MODULE_LIST_HEIGHT, AnvilColors.VOID_BLACK | 0xFF000000);

        // ヘッダー
        guiGraphics.drawString(this.font, "モジュール", x + 4, y + 4, AnvilColors.TECH_CYAN, false);

        // モジュール一覧
        int itemHeight = 18;
        int visibleItems = (MODULE_LIST_HEIGHT - 16) / itemHeight;
        int startIndex = moduleListScrollOffset;

        for (int i = 0; i < visibleItems && startIndex + i < filteredModules.size(); i++) {
            GridModule module = filteredModules.get(startIndex + i);
            int itemY = y + 16 + i * itemHeight;

            // モジュール背景
            int bgColor = getCategoryColor(module.category()) | 0x66000000;
            guiGraphics.fill(x + 2, itemY, x + MODULE_LIST_WIDTH - 2, itemY + itemHeight - 2, bgColor);

            // モジュール名
            String name = module.id().getPath();
            if (name.length() > 10) {
                name = name.substring(0, 10) + "..";
            }
            guiGraphics.drawString(this.font, name, x + 4, itemY + 2, 0xFFFFFF, true);

            // 重量表示
            String weightText = "W:" + module.weight();
            guiGraphics.drawString(this.font, weightText, x + 4, itemY + 10, 0xAAAAAA, true);

            // 形状サイズ表示
            String shapeText = module.shape().getCellCount() + "セル";
            guiGraphics.drawString(this.font, shapeText, x + 50, itemY + 10, 0xAAAAAA, true);

            // ホバーハイライト
            if (isMouseOver(mouseX, mouseY, x + 2, itemY, MODULE_LIST_WIDTH - 4, itemHeight - 2)) {
                guiGraphics.fill(x + 2, itemY, x + MODULE_LIST_WIDTH - 2, itemY + itemHeight - 2, 0x44FFFFFF);
            }
        }
    }

    /**
     * 重量・速度ランク情報を描画
     */
    private void renderWeightInfo(GuiGraphics guiGraphics) {
        int x = this.leftPos + GRID_X;
        int y = this.topPos + GRID_Y + (this.menu.getEditingConfig().size().getGridSize() * CELL_SIZE) + 8;

        // 重量
        int weight = this.menu.getTotalWeight();
        String weightText = String.format("重量: %d", weight);
        guiGraphics.drawString(this.font, weightText, x, y, AnvilColors.ETHER_WHITE, false);

        // 速度ランク
        WeightSystem.SpeedRank rank = this.menu.getSpeedRank();
        int rankColor = getRankColor(rank);
        String rankText = String.format("速度ランク: %s (%s)", rank.getId(), rank.getSpeedMultiplierPercent());
        guiGraphics.drawString(this.font, rankText, x, y + 12, rankColor, false);

        // セル使用状況
        int usedCells = this.menu.getUsedCells();
        int totalCells = this.menu.getTotalCells();
        String cellsText = String.format("セル: %d / %d", usedCells, totalCells);
        guiGraphics.drawString(this.font, cellsText, x, y + 24, AnvilColors.ETHER_WHITE, false);
    }

    /**
     * ドラッグ中のモジュールを描画
     */
    private void renderDraggedModule(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (draggedModule == null) return;

        int[][] cells = draggedModule.shape().getRotatedCells(draggedRotation);
        int moduleColor = getCategoryColor(draggedModule.category()) | 0xAA000000;

        for (int[] cell : cells) {
            int cellX = mouseX + cell[0] * CELL_SIZE - CELL_SIZE / 2;
            int cellY = mouseY + cell[1] * CELL_SIZE - CELL_SIZE / 2;
            guiGraphics.fill(cellX, cellY, cellX + CELL_SIZE - 1, cellY + CELL_SIZE - 1, moduleColor);
        }
    }

    /**
     * モジュールツールチップを描画
     */
    private void renderModuleTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 配置済みモジュールのツールチップ
        GridConfiguration config = this.menu.getEditingConfig();
        int guiX = this.leftPos + GRID_X;
        int guiY = this.topPos + GRID_Y;

        if (hoverGridX >= 0 && hoverGridY >= 0 && draggedModule == null) {
            int moduleIndex = this.menu.getModuleIndexAt(hoverGridX, hoverGridY);
            if (moduleIndex >= 0 && moduleIndex < config.placedModules().size()) {
                PlacedModule placed = config.placedModules().get(moduleIndex);
                GridModule module = GridModuleRegistry.get(placed.moduleId());
                if (module != null) {
                    renderModuleTooltip(guiGraphics, mouseX, mouseY, module);
                }
            }
        }

        // モジュールリストのツールチップ
        int listX = this.leftPos + MODULE_LIST_X;
        int listY = this.topPos + MODULE_LIST_Y + 16;
        int itemHeight = 18;
        int visibleItems = (MODULE_LIST_HEIGHT - 16) / itemHeight;

        for (int i = 0; i < visibleItems && moduleListScrollOffset + i < filteredModules.size(); i++) {
            int itemY = listY + i * itemHeight;
            if (isMouseOver(mouseX, mouseY, listX + 2, itemY, MODULE_LIST_WIDTH - 4, itemHeight - 2)) {
                GridModule module = filteredModules.get(moduleListScrollOffset + i);
                renderModuleTooltip(guiGraphics, mouseX, mouseY, module);
                break;
            }
        }
    }

    /**
     * モジュールのツールチップを描画
     */
    private void renderModuleTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, GridModule module) {
        List<Component> tooltip = new ArrayList<>();

        tooltip.add(Component.literal(module.id().getPath()).withStyle(s -> s.withColor(getRarityColor(module.rarity()))));
        tooltip.add(Component.literal("カテゴリ: " + module.category().name()));
        tooltip.add(Component.literal("形状: " + module.shape().name() + " (" + module.shape().getCellCount() + "セル)"));
        tooltip.add(Component.literal("重量: " + module.weight()));

        // 効果一覧
        tooltip.add(Component.literal("効果:").withStyle(s -> s.withColor(0xFFFF00)));
        for (var effect : module.effects()) {
            // EffectTypeでMULTIPLICATIVEかどうかを判定
            boolean isMultiplicative = effect.effectType() == ModEffect.EffectType.MULTIPLICATIVE;
            String effectText = "  " + effect.statId() + ": " +
                    (isMultiplicative ? "+" : "") +
                    String.format("%.1f", effect.value()) +
                    (isMultiplicative ? "%" : "");
            tooltip.add(Component.literal(effectText).withStyle(s -> s.withColor(0xAAAAAA)));
        }

        guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
    }

    // ============================================
    // マウス操作
    // ============================================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX;
        int my = (int) mouseY;
        int guiX = this.leftPos;
        int guiY = this.topPos;

        // モジュールリストクリック
        int listX = guiX + MODULE_LIST_X;
        int listY = guiY + MODULE_LIST_Y + 16;
        int itemHeight = 18;
        int visibleItems = (MODULE_LIST_HEIGHT - 16) / itemHeight;

        if (isMouseOver(mx, my, listX, listY, MODULE_LIST_WIDTH, MODULE_LIST_HEIGHT - 16)) {
            int clickedIndex = (my - listY) / itemHeight + moduleListScrollOffset;
            if (clickedIndex >= 0 && clickedIndex < filteredModules.size()) {
                draggedModule = filteredModules.get(clickedIndex);
                draggedRotation = this.menu.getCurrentRotation();
                return true;
            }
        }

        // グリッドクリック
        GridConfiguration config = this.menu.getEditingConfig();
        int gridX = guiX + GRID_X;
        int gridY = guiY + GRID_Y;
        int gridPixelSize = config.size().getGridSize() * CELL_SIZE;

        if (isMouseOver(mx, my, gridX, gridY, gridPixelSize, gridPixelSize)) {
            int cellX = (mx - gridX) / CELL_SIZE;
            int cellY = (my - gridY) / CELL_SIZE;

            if (button == 0) { // 左クリック
                if (draggedModule != null) {
                    // モジュール配置
                    if (this.menu.placeModule(draggedModule.id(), cellX, cellY, draggedRotation)) {
                        ANVIL.LOGGER.info("モジュールを配置: {} at ({}, {})", draggedModule.id(), cellX, cellY);
                    }
                    draggedModule = null;
                }
            } else if (button == 1) { // 右クリック
                // モジュール削除
                PlacedModule removed = this.menu.removeModuleAt(cellX, cellY);
                if (removed != null) {
                    ANVIL.LOGGER.info("モジュールを削除: {} at ({}, {})", removed.moduleId(), cellX, cellY);
                }
            }
            return true;
        }

        // ドラッグキャンセル（他の場所をクリック）
        if (draggedModule != null && button == 1) {
            draggedModule = null;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int mx = (int) mouseX;
        int my = (int) mouseY;
        int guiX = this.leftPos;
        int guiY = this.topPos;

        // モジュールリストのスクロール
        if (isMouseOver(mx, my, guiX + MODULE_LIST_X, guiY + MODULE_LIST_Y, MODULE_LIST_WIDTH, MODULE_LIST_HEIGHT)) {
            int itemHeight = 18;
            int visibleItems = (MODULE_LIST_HEIGHT - 16) / itemHeight;
            int maxScroll = Math.max(0, filteredModules.size() - visibleItems);

            moduleListScrollOffset -= (int) scrollY;
            moduleListScrollOffset = Math.max(0, Math.min(moduleListScrollOffset, maxScroll));
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Rキーで回転
        if (keyCode == 82) { // R
            this.menu.rotateCurrentSelection(true);
            if (draggedModule != null) {
                draggedRotation = (draggedRotation + 90) % 360;
            }
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * モジュールリストを更新
     */
    private void updateFilteredModules() {
        filteredModules.clear();
        int filter = this.menu.getCategoryFilter();

        for (GridModule module : this.menu.getAvailableModules()) {
            if (filter == 0 || module.category().ordinal() + 1 == filter) {
                filteredModules.add(module);
            }
        }

        moduleListScrollOffset = 0;
    }

    /**
     * マウスがセル上にあるかチェック
     */
    private boolean isMouseOverCell(int mouseX, int mouseY, int cellX, int cellY) {
        return mouseX >= cellX && mouseX < cellX + CELL_SIZE &&
                mouseY >= cellY && mouseY < cellY + CELL_SIZE;
    }

    /**
     * マウスが指定領域内かチェック
     */
    private boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    /**
     * カテゴリの色を取得
     */
    private int getCategoryColor(ModuleCategory category) {
        return switch (category) {
            case ATTACK -> 0xFF4444;    // 赤
            case DEFENSE -> 0x4488FF;   // 青
            case SPEED -> 0x44FF44;     // 緑
            case CAPACITY -> 0xFFFF44;  // 黄
            case UTILITY -> 0xFF44FF;   // マゼンタ
            case CONNECTOR -> 0x44FFFF; // シアン
        };
    }

    /**
     * レアリティの色を取得
     */
    private int getRarityColor(ModRarity rarity) {
        return switch (rarity) {
            case COMMON -> 0x888888;
            case UNCOMMON -> 0x55AA55;
            case RARE -> 0x5555FF;
            case LEGENDARY -> 0xFFAA00;
        };
    }

    /**
     * 速度ランクの色を取得
     */
    private int getRankColor(WeightSystem.SpeedRank rank) {
        return switch (rank) {
            case S -> 0xFFFF00;  // 金
            case A -> 0x00FF00;  // 緑
            case B -> 0xFFFFFF;  // 白
            case C -> 0xFFAA00;  // オレンジ
            case D -> 0xFF5500;  // 赤橙
            case E -> 0xFF0000;  // 赤
        };
    }

    @Override
    public void containerTick() {
        super.containerTick();
        // ホバー座標をリセット（毎フレーム更新）
        hoverGridX = -1;
        hoverGridY = -1;
    }
}
