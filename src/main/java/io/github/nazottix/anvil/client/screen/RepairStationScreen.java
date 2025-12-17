package io.github.nazottix.anvil.client.screen;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.client.ui.AnvilButtonRenderer;
import io.github.nazottix.anvil.client.ui.AnvilColors;
import io.github.nazottix.anvil.menu.RepairStationMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

/**
 * リペアステーションスクリーン
 *
 * ツール修理画面のクライアント側描画を行います。
 * ツールスロット、パーツスロット、修理ボタンを提供。
 *
 * レイアウト:
 * - 上部: ツールスロット + パーツスロット + 修理ボタン
 * - 下部: インベントリ
 */
public class RepairStationScreen extends AbstractContainerScreen<RepairStationMenu> {

    // スロット位置
    private static final int TOOL_SLOT_X = 26;
    private static final int TOOL_SLOT_Y = 35;
    private static final int PART_SLOT_X = 80;
    private static final int PART_SLOT_Y = 35;

    // 修理ボタン
    private static final int REPAIR_BUTTON_X = 116;
    private static final int REPAIR_BUTTON_Y = 35;
    private static final int REPAIR_BUTTON_WIDTH = 50;
    private static final int REPAIR_BUTTON_HEIGHT = 16;

    /**
     * コンストラクタ
     */
    public RepairStationScreen(RepairStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // GUI のサイズを設定
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
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

        // スロットエリア背景
        renderSlotAreas(guiGraphics, x, y);

        // 修理ボタンを描画
        renderRepairButton(guiGraphics, x, y, mouseX, mouseY);

        // 矢印描画（ツール + パーツ → 修理）
        renderArrow(guiGraphics, x + 48, y + 39);

        // 区切り線（スロットとインベントリの間）
        guiGraphics.fill(x + 8, y + 70, x + this.imageWidth - 8, y + 71, 0xFF444444);

        // プレイヤーインベントリ境界線とスロット背景を描画
        renderInventoryBackground(guiGraphics, x, y);
    }

    /**
     * スロットエリアを描画
     */
    private void renderSlotAreas(GuiGraphics guiGraphics, int guiX, int guiY) {
        // ツール入力スロット背景
        guiGraphics.fill(guiX + TOOL_SLOT_X - 2, guiY + TOOL_SLOT_Y - 2,
                guiX + TOOL_SLOT_X + 18, guiY + TOOL_SLOT_Y + 18, AnvilColors.FORGE_ORANGE | 0xFF000000);
        guiGraphics.fill(guiX + TOOL_SLOT_X, guiY + TOOL_SLOT_Y,
                guiX + TOOL_SLOT_X + 16, guiY + TOOL_SLOT_Y + 16, AnvilColors.VOID_BLACK | 0xFF000000);

        // パーツ入力スロット背景
        guiGraphics.fill(guiX + PART_SLOT_X - 1, guiY + PART_SLOT_Y - 1,
                guiX + PART_SLOT_X + 17, guiY + PART_SLOT_Y + 17, 0xFF3A3A3A);
        guiGraphics.fill(guiX + PART_SLOT_X, guiY + PART_SLOT_Y,
                guiX + PART_SLOT_X + 16, guiY + PART_SLOT_Y + 16, AnvilColors.VOID_BLACK | 0xFF000000);
    }

    /**
     * 修理ボタンを描画
     * AnvilButtonRendererユーティリティを使用してテーマに統一
     */
    private void renderRepairButton(GuiGraphics guiGraphics, int guiX, int guiY, int mouseX, int mouseY) {
        int btnX = guiX + REPAIR_BUTTON_X;
        int btnY = guiY + REPAIR_BUTTON_Y;

        // AnvilButtonRendererを使用してボタンを描画
        boolean isHovered = AnvilButtonRenderer.isMouseOverButton(mouseX, mouseY, btnX, btnY, REPAIR_BUTTON_WIDTH, REPAIR_BUTTON_HEIGHT);
        boolean canRepair = this.menu.canRepair();
        Component repairText = Component.translatable("gui.anvil.repair_station.repair");
        AnvilButtonRenderer.renderButton(guiGraphics, this.font, btnX, btnY, REPAIR_BUTTON_WIDTH, REPAIR_BUTTON_HEIGHT, repairText, isHovered, canRepair);
    }

    /**
     * 矢印を描画
     */
    private void renderArrow(GuiGraphics guiGraphics, int x, int y) {
        // 簡易矢印（+ 記号）
        guiGraphics.fill(x + 8, y, x + 10, y + 8, 0xFFAAAAAA);
        guiGraphics.fill(x + 4, y + 3, x + 14, y + 5, 0xFFAAAAAA);
    }

    /**
     * プレイヤーインベントリ背景を描画
     */
    private void renderInventoryBackground(GuiGraphics guiGraphics, int guiX, int guiY) {
        // インベントリスロット（3行9列）- y=84から
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotX = guiX + 8 + col * 18;
                int slotY = guiY + 84 + row * 18;
                // スロット枠
                guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, 0xFF373737);
                // スロット背景
                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF8B8B8B);
                // スロット内側の影
                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 1, 0xFF555555);
                guiGraphics.fill(slotX, slotY, slotX + 1, slotY + 16, 0xFF555555);
            }
        }

        // ホットバー（1行9列）- y=142
        for (int col = 0; col < 9; col++) {
            int slotX = guiX + 8 + col * 18;
            int slotY = guiY + 142;
            // スロット枠
            guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, 0xFF373737);
            // スロット背景
            guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF8B8B8B);
            // スロット内側の影
            guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 1, 0xFF555555);
            guiGraphics.fill(slotX, slotY, slotX + 1, slotY + 16, 0xFF555555);
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

        // スロットラベル
        guiGraphics.drawString(this.font, Component.translatable("gui.anvil.repair_station.tool"),
                TOOL_SLOT_X, TOOL_SLOT_Y - 10, 0x888888, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.anvil.repair_station.part"),
                PART_SLOT_X, PART_SLOT_Y - 10, 0x888888, false);
    }

    // ============================================
    // マウス操作
    // ============================================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int guiX = this.leftPos;
        int guiY = this.topPos;
        Minecraft mc = Minecraft.getInstance();

        if (button == 0) {
            // 修理ボタンのクリック判定
            int repairBtnX = guiX + REPAIR_BUTTON_X;
            int repairBtnY = guiY + REPAIR_BUTTON_Y;
            if (AnvilButtonRenderer.isMouseOverButton((int) mouseX, (int) mouseY, repairBtnX, repairBtnY, REPAIR_BUTTON_WIDTH, REPAIR_BUTTON_HEIGHT)) {
                if (this.menu.canRepair()) {
                    // サーバーにボタンクリックを送信（ボタンID 0 = 修理）
                    if (mc.gameMode != null) {
                        mc.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
                    }
                    // AnvilButtonRendererを使用して修理音を再生
                    AnvilButtonRenderer.playButtonSound(AnvilButtonRenderer.ButtonSound.ACTION);
                    ANVIL.LOGGER.info("ツール修理を実行");
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * マウスが指定領域内かチェック
     */
    private boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
