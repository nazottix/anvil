package io.github.nazottix.anvil.client.screen;

import io.github.nazottix.anvil.client.ui.AnvilColors;
import io.github.nazottix.anvil.menu.ModStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * MODステーションスクリーン
 *
 * ツールへのMOD装着画面を表示します。
 * Warframe風のMODスロットシステムを提供します。
 *
 * レイアウト:
 * - 上部中央: ツールスロット
 * - 中央: MODスロット（8つ、2行4列）
 * - 容量バー表示
 * - 下部: インベントリ
 *
 * 仕様書参照: docs/10_UI_UXデザイン.md
 */
public class ModStationScreen extends AbstractContainerScreen<ModStationMenu> {

    // UI定数
    private static final int TOOL_SLOT_X = 80;
    private static final int TOOL_SLOT_Y = 17;

    // MODスロット
    private static final int MOD_SLOT_START_X = 26;
    private static final int MOD_SLOT_START_Y = 53;
    private static final int MOD_SLOT_SPACING_X = 36;
    private static final int MOD_SLOT_SPACING_Y = 18;
    private static final int MOD_SLOTS_PER_ROW = 4;
    private static final int MOD_SLOT_ROWS = 2;

    // 容量バー
    private static final int CAPACITY_BAR_X = 26;
    private static final int CAPACITY_BAR_Y = 40;
    private static final int CAPACITY_BAR_WIDTH = 124;
    private static final int CAPACITY_BAR_HEIGHT = 8;

    /**
     * コンストラクタ
     */
    public ModStationScreen(ModStationMenu menu, Inventory playerInventory, Component title) {
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
        // TODO: MOD操作ボタンを追加
    }

    // ============================================
    // 描画
    // ============================================

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // 背景色で塗りつぶし
        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight,
                AnvilColors.VOID_BLACK | 0xFF000000);

        // パネル背景
        guiGraphics.fill(x + 2, y + 2, x + this.imageWidth - 2, y + this.imageHeight - 2,
                AnvilColors.ANVIL_STEEL | 0xFF000000);

        // ツールスロット背景
        renderToolSlot(guiGraphics, x, y);

        // 容量バー
        renderCapacityBar(guiGraphics, x, y);

        // MODスロット背景
        renderModSlots(guiGraphics, x, y);

        // 区切り線
        guiGraphics.fill(x + 8, y + 88, x + this.imageWidth - 8, y + 89, 0xFF444444);

        // プレイヤーインベントリ境界線とスロット背景を描画
        renderInventoryBackground(guiGraphics, x, y);
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
     * ツールスロットを描画
     */
    private void renderToolSlot(GuiGraphics guiGraphics, int guiX, int guiY) {
        int slotX = guiX + TOOL_SLOT_X;
        int slotY = guiY + TOOL_SLOT_Y;

        // スロット枠（オレンジ系）
        guiGraphics.fill(slotX - 2, slotY - 2, slotX + 18, slotY + 18, AnvilColors.FORGE_ORANGE);
        guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, AnvilColors.VOID_BLACK | 0xFF000000);
    }

    /**
     * 容量バーを描画
     */
    private void renderCapacityBar(GuiGraphics guiGraphics, int guiX, int guiY) {
        int barX = guiX + CAPACITY_BAR_X;
        int barY = guiY + CAPACITY_BAR_Y;

        // バー背景
        guiGraphics.fill(barX, barY, barX + CAPACITY_BAR_WIDTH, barY + CAPACITY_BAR_HEIGHT, 0xFF1A1A1A);
        guiGraphics.renderOutline(barX - 1, barY - 1, CAPACITY_BAR_WIDTH + 2, CAPACITY_BAR_HEIGHT + 2, 0xFF444444);

        // TODO: 実際の容量に基づいてバーを描画
        int currentCapacity = 0;
        int maxCapacity = 100;
        int filledWidth = maxCapacity > 0 ? (currentCapacity * CAPACITY_BAR_WIDTH) / maxCapacity : 0;

        // 容量バー（オレンジ系）
        if (filledWidth > 0) {
            guiGraphics.fill(barX, barY, barX + filledWidth, barY + CAPACITY_BAR_HEIGHT,
                    AnvilColors.FORGE_ORANGE);
        }
    }

    /**
     * MODスロットを描画
     */
    private void renderModSlots(GuiGraphics guiGraphics, int guiX, int guiY) {
        // 極性シンボル
        String[] polaritySymbols = {"V", "D", "-", "O", "V", "D", "~", "<>"};
        int[] polarityColors = {
                0xFFFF4444, // Madurai (攻撃) - 赤
                0xFF44FF44, // Vazarin (防御) - 緑
                0xFF4444FF, // Naramon (汎用) - 青
                0xFFFFFF44, // Zenurik (特殊) - 黄
                0xFFFF4444,
                0xFF44FF44,
                0xFF44FFFF, // Unairu (属性) - シアン
                0xFFFF44FF  // 複合 - マゼンタ
        };

        for (int row = 0; row < MOD_SLOT_ROWS; row++) {
            for (int col = 0; col < MOD_SLOTS_PER_ROW; col++) {
                int slotIndex = row * MOD_SLOTS_PER_ROW + col;
                int slotX = guiX + MOD_SLOT_START_X + col * MOD_SLOT_SPACING_X;
                int slotY = guiY + MOD_SLOT_START_Y + row * MOD_SLOT_SPACING_Y;

                // スロット枠（極性色）
                int borderColor = polarityColors[slotIndex % polarityColors.length];
                guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, borderColor);
                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, AnvilColors.VOID_BLACK | 0xFF000000);

                // 極性シンボル（スロットが空の場合）
                if (this.menu.getSlot(slotIndex + 1).getItem().isEmpty()) {
                    String symbol = polaritySymbols[slotIndex % polaritySymbols.length];
                    int symbolColor = borderColor & 0x88FFFFFF; // 半透明
                    guiGraphics.drawCenteredString(this.font, symbol, slotX + 8, slotY + 4, symbolColor);
                }
            }
        }
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

        // 容量ラベル
        Component capacityLabel = Component.translatable("gui.anvil.mod_station.capacity", 0, 100);
        guiGraphics.drawString(this.font, capacityLabel, CAPACITY_BAR_X, CAPACITY_BAR_Y - 10,
                0xAAAAAA, false);
    }
}
