package io.github.nazottix.anvil.client.screen;

import io.github.nazottix.anvil.client.ui.AnvilColors;
import io.github.nazottix.anvil.menu.JewelStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * ジュエルステーションスクリーン
 *
 * ツールへのジュエル装着画面を表示します。
 * ツールと最大4つのジュエルをスロットに配置します。
 *
 * レイアウト:
 * - 上部中央: ツールスロット
 * - 中央: ジュエルスロット（4つ横並び）
 * - 下部: インベントリ
 *
 * 仕様書参照: docs/10_UI_UXデザイン.md
 */
public class JewelStationScreen extends AbstractContainerScreen<JewelStationMenu> {

    // UI定数
    private static final int TOOL_SLOT_X = 80;
    private static final int TOOL_SLOT_Y = 35;

    // ジュエルスロット
    private static final int JEWEL_SLOT_START_X = 26;
    private static final int JEWEL_SLOT_Y = 70;
    private static final int JEWEL_SLOT_SPACING = 36;
    private static final int JEWEL_SLOT_COUNT = 4;

    /**
     * コンストラクタ
     */
    public JewelStationScreen(JewelStationMenu menu, Inventory playerInventory, Component title) {
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
        // TODO: ジュエル操作ボタンを追加
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

        // 矢印（ツール → ジュエル）
        guiGraphics.fill(x + 80, y + 55, x + 96, y + 57, 0xFFAAAAAA);

        // ジュエルスロット背景
        renderJewelSlots(guiGraphics, x, y);

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

        // スロット枠（紫系）
        guiGraphics.fill(slotX - 2, slotY - 2, slotX + 18, slotY + 18, AnvilColors.ARCANE_PURPLE);
        guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, AnvilColors.VOID_BLACK | 0xFF000000);
    }

    /**
     * ジュエルスロットを描画
     */
    private void renderJewelSlots(GuiGraphics guiGraphics, int guiX, int guiY) {
        for (int i = 0; i < JEWEL_SLOT_COUNT; i++) {
            int slotX = guiX + JEWEL_SLOT_START_X + i * JEWEL_SLOT_SPACING;
            int slotY = guiY + JEWEL_SLOT_Y;

            // ジュエルスロット背景（紫系グラデーション風）
            int color = getJewelSlotColor(i);
            guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, color);
            guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, AnvilColors.VOID_BLACK | 0xFF000000);

            // スロット番号
            String label = String.valueOf(i + 1);
            guiGraphics.drawString(this.font, label, slotX + 5, slotY - 10, 0x888888, false);
        }
    }

    /**
     * ジュエルスロットの色を取得
     */
    private int getJewelSlotColor(int index) {
        // 各スロットで微妙に異なる色調
        return switch (index) {
            case 0 -> 0xFFFF6B6B; // 赤系
            case 1 -> 0xFF6BCB77; // 緑系
            case 2 -> 0xFF4D96FF; // 青系
            case 3 -> 0xFFFFD93D; // 黄系
            default -> AnvilColors.ARCANE_PURPLE;
        };
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

        // 説明テキスト
        Component description = Component.translatable("gui.anvil.jewel_station.desc");
        guiGraphics.drawString(this.font, description, 8, 20, 0x888888, false);
    }
}
