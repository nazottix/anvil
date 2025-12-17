package io.github.nazottix.anvil.client.screen;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.client.ui.AnvilButtonRenderer;
import io.github.nazottix.anvil.client.ui.AnvilColors;
import io.github.nazottix.anvil.client.ui.AnvilPanelRenderer;
import io.github.nazottix.anvil.menu.RespecStationMenu;
import net.minecraft.client.gui.GuiGraphics;
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

    // リスペックボタン定数（AnvilButtonRendererを使用）
    private static final int RESPEC_BUTTON_X = 62;
    private static final int RESPEC_BUTTON_Y = 55;
    private static final int RESPEC_BUTTON_WIDTH = 52;
    private static final int RESPEC_BUTTON_HEIGHT = 16;

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
        // リスペックボタンはAnvilButtonRendererでカスタム描画するため、標準Buttonは使用しない
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

        // 矢印
        guiGraphics.fill(x + 75, y + 42, x + 101, y + 44, 0xFFAAAAAA);

        // リスペックアイテムスロット背景
        renderRespecItemSlot(guiGraphics, x, y);

        // リスペックボタンを描画（AnvilButtonRendererを使用）
        renderRespecButton(guiGraphics, x, y, mouseX, mouseY);

        // 区切り線（立体的）
        AnvilPanelRenderer.renderHorizontalSeparator(guiGraphics, x + 8, y + 70, this.imageWidth - 16);

        // プレイヤーインベントリ境界線とスロット背景を描画（マイクラ標準風）
        AnvilPanelRenderer.renderInventorySlots(guiGraphics, x, y, 98, 156);
    }

    /**
     * リスペックボタンを描画
     * AnvilButtonRendererユーティリティを使用してテーマに統一
     */
    private void renderRespecButton(GuiGraphics guiGraphics, int guiX, int guiY, int mouseX, int mouseY) {
        int btnX = guiX + RESPEC_BUTTON_X;
        int btnY = guiY + RESPEC_BUTTON_Y;

        // AnvilButtonRendererを使用してボタンを描画
        boolean isHovered = AnvilButtonRenderer.isMouseOverButton(mouseX, mouseY, btnX, btnY, RESPEC_BUTTON_WIDTH, RESPEC_BUTTON_HEIGHT);
        boolean canRespec = true; // 常に有効（クリック時にperformRespecで判定）
        Component respecText = Component.translatable("gui.anvil.respec_station.respec");
        AnvilButtonRenderer.renderButton(guiGraphics, this.font, btnX, btnY, RESPEC_BUTTON_WIDTH, RESPEC_BUTTON_HEIGHT, respecText, isHovered, canRespec);
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
     * ツールスロットを描画（マイクラ標準風スロット + シアン枠で強調）
     */
    private void renderToolSlot(GuiGraphics guiGraphics, int guiX, int guiY) {
        int slotX = guiX + TOOL_SLOT_X;
        int slotY = guiY + TOOL_SLOT_Y;

        // シアン枠で強調
        guiGraphics.fill(slotX - 2, slotY - 2, slotX + 18, slotY + 18, AnvilColors.TECH_CYAN);
        // 内側に標準スロットを描画
        AnvilPanelRenderer.renderSlot(guiGraphics, slotX, slotY);

        // ラベル
        guiGraphics.drawString(this.font, Component.translatable("gui.anvil.respec_station.tool"),
                slotX - 4, slotY - 12, 0x888888, false);
    }

    /**
     * リスペックアイテムスロットを描画（マイクラ標準風スロット + オレンジ枠で強調）
     */
    private void renderRespecItemSlot(GuiGraphics guiGraphics, int guiX, int guiY) {
        int slotX = guiX + RESPEC_ITEM_SLOT_X;
        int slotY = guiY + RESPEC_ITEM_SLOT_Y;

        // オレンジ枠で強調
        guiGraphics.fill(slotX - 2, slotY - 2, slotX + 18, slotY + 18, AnvilColors.FORGE_ORANGE);
        // 内側に標準スロットを描画
        AnvilPanelRenderer.renderSlot(guiGraphics, slotX, slotY);

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

    // ============================================
    // マウス操作
    // ============================================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int guiX = this.leftPos;
        int guiY = this.topPos;

        if (button == 0) {
            // リスペックボタンのクリック判定
            int respecBtnX = guiX + RESPEC_BUTTON_X;
            int respecBtnY = guiY + RESPEC_BUTTON_Y;
            if (AnvilButtonRenderer.isMouseOverButton((int) mouseX, (int) mouseY, respecBtnX, respecBtnY, RESPEC_BUTTON_WIDTH, RESPEC_BUTTON_HEIGHT)) {
                if (this.menu.performRespec()) {
                    // AnvilButtonRendererを使用してリスペック音を再生
                    AnvilButtonRenderer.playButtonSound(AnvilButtonRenderer.ButtonSound.ACTION);
                    ANVIL.LOGGER.info("リスペックを実行");
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
