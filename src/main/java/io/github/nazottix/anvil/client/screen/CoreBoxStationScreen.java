package io.github.nazottix.anvil.client.screen;

import io.github.nazottix.anvil.client.ui.AnvilColors;
import io.github.nazottix.anvil.client.ui.AnvilPanelRenderer;
import io.github.nazottix.anvil.menu.CoreBoxStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * コアボックスステーションスクリーン
 *
 * グリッドモジュール配置画面を表示します。
 * テトリス風のモジュール配置システムを提供します。
 *
 * レイアウト:
 * - 左側: ツールスロット
 * - 中央: グリッド表示エリア
 * - 右側: モジュール入力スロット（4つ縦並び）
 * - 下部: インベントリ
 *
 * 仕様書参照: docs/10_UI_UXデザイン.md
 */
public class CoreBoxStationScreen extends AbstractContainerScreen<CoreBoxStationMenu> {

    // UI定数
    private static final int TOOL_SLOT_X = 8;
    private static final int TOOL_SLOT_Y = 35;

    // グリッド表示エリア
    private static final int GRID_AREA_X = 30;
    private static final int GRID_AREA_Y = 18;
    private static final int GRID_AREA_SIZE = 70; // 5x5グリッド用
    private static final int GRID_CELL_SIZE = 14;

    // モジュールスロット
    private static final int MODULE_SLOT_X = 152;
    private static final int MODULE_SLOT_START_Y = 17;
    private static final int MODULE_SLOT_SPACING = 18;
    private static final int MODULE_SLOT_COUNT = 4;

    /**
     * コンストラクタ
     */
    public CoreBoxStationScreen(CoreBoxStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // GUIのサイズを設定（上部コンテンツとInventoryラベルの間にスペースを確保）
        this.imageWidth = 176;
        this.imageHeight = 200;
        // インベントリラベルの位置調整
        this.inventoryLabelY = 106;
    }

    @Override
    protected void init() {
        super.init();
        // TODO: グリッド操作ボタンを追加（回転、クリア等）
    }

    // ============================================
    // 描画
    // ============================================

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // 立体的なメインパネルを描画（マイクラ従来の奥行きあるデザイン）
        AnvilPanelRenderer.renderMainPanel(guiGraphics, x, y, this.imageWidth, this.imageHeight);

        // ツールスロット背景
        renderToolSlot(guiGraphics, x, y);

        // グリッド表示エリア
        renderGridArea(guiGraphics, x, y);

        // モジュールスロット背景
        renderModuleSlots(guiGraphics, x, y);

        // 区切り線（立体的）
        AnvilPanelRenderer.renderHorizontalSeparator(guiGraphics, x + 8, y + 88, this.imageWidth - 16);

        // プレイヤーインベントリ境界線とスロット背景を描画（マイクラ標準風）
        AnvilPanelRenderer.renderInventorySlots(guiGraphics, x, y, 117, 175);
    }

    /**
     * プレイヤーインベントリ背景とスロット境界線を描画
     */
    private void renderInventoryBackground(GuiGraphics guiGraphics, int guiX, int guiY) {
        // インベントリスロット（3行9列）- y=117から（14ピクセル下にずらした）
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotX = guiX + 8 + col * 18;
                int slotY = guiY + 117 + row * 18;
                // スロット枠
                guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, 0xFF373737);
                // スロット背景
                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF8B8B8B);
                // スロット内側の影
                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 1, 0xFF555555);
                guiGraphics.fill(slotX, slotY, slotX + 1, slotY + 16, 0xFF555555);
            }
        }

        // ホットバー（1行9列）- y=175（14ピクセル下にずらした）
        for (int col = 0; col < 9; col++) {
            int slotX = guiX + 8 + col * 18;
            int slotY = guiY + 175;
            // スロット枠
            guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, 0xFF373737);
            // スロット背景
            guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF8B8B8B);
            // スロット内側の影
            guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 1, 0xFF555555);
            guiGraphics.fill(slotX, slotY, slotX + 1, slotY + 16, 0xFF555555);
        }
    }

    /**
     * ツールスロットを描画（マイクラ標準風スロット + シアン枠で強調）
     */
    private void renderToolSlot(GuiGraphics guiGraphics, int guiX, int guiY) {
        int slotX = guiX + TOOL_SLOT_X;
        int slotY = guiY + TOOL_SLOT_Y;

        // シアン枠で強調
        guiGraphics.fill(slotX - 2, slotY - 2, slotX + 18, slotY + 18, AnvilColors.TECH_CYAN);
        // 内側に標準スロットを描画
        AnvilPanelRenderer.renderSlot(guiGraphics, slotX, slotY);
    }

    /**
     * グリッドエリアを描画
     */
    private void renderGridArea(GuiGraphics guiGraphics, int guiX, int guiY) {
        int areaX = guiX + GRID_AREA_X;
        int areaY = guiY + GRID_AREA_Y;

        // グリッド背景
        guiGraphics.fill(areaX, areaY, areaX + GRID_AREA_SIZE, areaY + GRID_AREA_SIZE, 0xFF1A2A3A);

        // グリッド枠
        guiGraphics.renderOutline(areaX - 1, areaY - 1, GRID_AREA_SIZE + 2, GRID_AREA_SIZE + 2,
                AnvilColors.TECH_CYAN);

        // 5x5グリッドセルを描画
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                int cellX = areaX + col * GRID_CELL_SIZE;
                int cellY = areaY + row * GRID_CELL_SIZE;

                // セル背景
                guiGraphics.fill(cellX + 1, cellY + 1,
                        cellX + GRID_CELL_SIZE - 1, cellY + GRID_CELL_SIZE - 1,
                        0xFF0A1A2A);

                // セル枠
                guiGraphics.renderOutline(cellX, cellY, GRID_CELL_SIZE, GRID_CELL_SIZE, 0xFF2A4A5A);
            }
        }

        // ツールがセットされていない場合はメッセージ表示
        if (this.menu.getSlot(0).getItem().isEmpty()) {
            Component message = Component.translatable("gui.anvil.core_box_station.insert_tool");
            int textWidth = this.font.width(message);
            guiGraphics.drawString(this.font, message,
                    areaX + (GRID_AREA_SIZE - textWidth) / 2,
                    areaY + GRID_AREA_SIZE / 2 - 4,
                    0x888888, false);
        }
    }

    /**
     * モジュールスロットを描画（マイクラ標準風スロット + シアン枠で強調）
     */
    private void renderModuleSlots(GuiGraphics guiGraphics, int guiX, int guiY) {
        for (int i = 0; i < MODULE_SLOT_COUNT; i++) {
            int slotX = guiX + MODULE_SLOT_X;
            int slotY = guiY + MODULE_SLOT_START_Y + i * MODULE_SLOT_SPACING;

            // シアン枠で強調
            guiGraphics.fill(slotX - 2, slotY - 2, slotX + 18, slotY + 18, 0xFF2A6A7A);
            // 内側に標準スロットを描画
            AnvilPanelRenderer.renderSlot(guiGraphics, slotX, slotY);
        }

        // ラベル
        guiGraphics.drawString(this.font, "M", guiX + MODULE_SLOT_X + 5, guiY + 8, 0x888888, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // タイトル
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY,
                AnvilColors.ETHER_WHITE, false);

        // インベントリラベル
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY,
                AnvilColors.ETHER_WHITE, false);

        // 重量情報
        Component weightInfo = Component.translatable("gui.anvil.core_box_station.weight", 0, 50);
        guiGraphics.drawString(this.font, weightInfo, GRID_AREA_X, GRID_AREA_Y + GRID_AREA_SIZE + 4,
                0x888888, false);
    }
}
