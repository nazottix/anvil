package io.github.nazottix.anvil.client.screen;

import io.github.nazottix.anvil.client.ui.AnvilColors;
import io.github.nazottix.anvil.menu.RespecStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;

/**
 * リスペックステーションスクリーン
 *
 * ツールのスキルリセット画面を表示します。
 * ツールとリスペックアイテムを配置してリスペックを実行します。
 *
 * レイアウト:
 * - 左側: ツールスロット
 * - 右側: リスペックアイテムスロット
 * - 中央: リスペックボタン
 * - 下部: インベントリ
 *
 * 仕様書参照: docs/10_UI_UXデザイン.md
 */
public class RespecStationScreen extends AbstractContainerScreen<RespecStationMenu> {

    // UI定数
    private static final int TOOL_SLOT_X = 56;
    private static final int TOOL_SLOT_Y = 35;

    private static final int RESPEC_ITEM_SLOT_X = 104;
    private static final int RESPEC_ITEM_SLOT_Y = 35;

    /**
     * コンストラクタ
     */
    public RespecStationScreen(RespecStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // GUIのサイズを設定（上部コンテンツとInventoryラベルの間にスペースを確保）
        this.imageWidth = 176;
        this.imageHeight = 180;
        // インベントリラベルの位置調整
        this.inventoryLabelY = 87;
    }

    @Override
    protected void init() {
        super.init();

        // リスペックボタン
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.anvil.respec_station.respec"),
                button -> {
                    if (this.menu.performRespec()) {
                        // リスペック成功
                    }
                }
        ).bounds(this.leftPos + 62, this.topPos + 55, 52, 16).build());
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

        // 矢印
        guiGraphics.fill(x + 75, y + 42, x + 101, y + 44, 0xFFAAAAAA);

        // リスペックアイテムスロット背景
        renderRespecItemSlot(guiGraphics, x, y);

        // 区切り線
        guiGraphics.fill(x + 8, y + 70, x + this.imageWidth - 8, y + 71, 0xFF444444);

        // プレイヤーインベントリ境界線とスロット背景を描画
        renderInventoryBackground(guiGraphics, x, y);
    }

    /**
     * プレイヤーインベントリ背景とスロット境界線を描画
     */
    private void renderInventoryBackground(GuiGraphics guiGraphics, int guiX, int guiY) {
        // インベントリスロット（3行9列）- y=98から（14ピクセル下にずらした）
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotX = guiX + 8 + col * 18;
                int slotY = guiY + 98 + row * 18;
                // スロット枠
                guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, 0xFF373737);
                // スロット背景
                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF8B8B8B);
                // スロット内側の影
                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 1, 0xFF555555);
                guiGraphics.fill(slotX, slotY, slotX + 1, slotY + 16, 0xFF555555);
            }
        }

        // ホットバー（1行9列）- y=156（14ピクセル下にずらした）
        for (int col = 0; col < 9; col++) {
            int slotX = guiX + 8 + col * 18;
            int slotY = guiY + 156;
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

        // スロット枠（シアン系 - ツール）
        guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, AnvilColors.TECH_CYAN);
        guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, AnvilColors.VOID_BLACK | 0xFF000000);

        // ラベル
        guiGraphics.drawString(this.font, Component.translatable("gui.anvil.respec_station.tool"),
                slotX - 4, slotY - 12, 0x888888, false);
    }

    /**
     * リスペックアイテムスロットを描画
     */
    private void renderRespecItemSlot(GuiGraphics guiGraphics, int guiX, int guiY) {
        int slotX = guiX + RESPEC_ITEM_SLOT_X;
        int slotY = guiY + RESPEC_ITEM_SLOT_Y;

        // スロット枠（オレンジ系 - リスペックアイテム）
        guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, AnvilColors.FORGE_ORANGE);
        guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, AnvilColors.VOID_BLACK | 0xFF000000);

        // ラベル
        guiGraphics.drawString(this.font, Component.translatable("gui.anvil.respec_station.item"),
                slotX - 4, slotY - 12, 0x888888, false);
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
    }
}
