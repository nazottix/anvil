package io.github.nazottix.anvil.client.screen;

import io.github.nazottix.anvil.menu.ForgingStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 鍛造ステーションスクリーン
 *
 * 鍛造ステーションのクライアント側UI描画を担当します。
 * 精錬所と同様のレイアウト（入力、ハンマー、出力、進捗バー）を表示します。
 */
public class ForgingStationScreen extends AbstractContainerScreen<ForgingStationMenu> {

    /**
     * コンストラクタ
     */
    public ForgingStationScreen(ForgingStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();

        // タイトルラベル位置
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // メインパネル背景
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFFC6C6C6);

        // パネル枠
        guiGraphics.fill(x, y, x + imageWidth, y + 1, 0xFFFFFFFF);
        guiGraphics.fill(x, y, x + 1, y + imageHeight, 0xFFFFFFFF);
        guiGraphics.fill(x + imageWidth - 1, y, x + imageWidth, y + imageHeight, 0xFF555555);
        guiGraphics.fill(x, y + imageHeight - 1, x + imageWidth, y + imageHeight, 0xFF555555);

        // 入力スロット枠
        drawSlot(guiGraphics, x + 55, y + 16);

        // ハンマースロット枠
        drawSlot(guiGraphics, x + 55, y + 52);

        // 出力スロット枠
        drawSlot(guiGraphics, x + 115, y + 34);

        // ハンマーアイコン（ハンマースロット横）
        drawHammerIcon(guiGraphics, x + 36, y + 53);

        // 進捗矢印
        drawProgressArrow(guiGraphics, x + 79, y + 34);
    }

    /**
     * スロット枠を描画
     */
    private void drawSlot(GuiGraphics guiGraphics, int x, int y) {
        // 凹み効果
        guiGraphics.fill(x - 1, y - 1, x + 17, y, 0xFF373737);
        guiGraphics.fill(x - 1, y - 1, x, y + 17, 0xFF373737);
        guiGraphics.fill(x, y + 16, x + 17, y + 17, 0xFFFFFFFF);
        guiGraphics.fill(x + 16, y, x + 17, y + 17, 0xFFFFFFFF);
        guiGraphics.fill(x, y, x + 16, y + 16, 0xFF8B8B8B);
    }

    /**
     * ハンマーアイコンを描画
     */
    private void drawHammerIcon(GuiGraphics guiGraphics, int x, int y) {
        // シンプルなハンマー形状
        // 柄
        guiGraphics.fill(x + 6, y + 4, x + 8, y + 12, 0xFF8B4513);
        // ヘッド
        guiGraphics.fill(x + 2, y + 2, x + 12, y + 6, 0xFF808080);
    }

    /**
     * 進捗矢印を描画
     */
    private void drawProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        // 矢印背景
        guiGraphics.fill(x, y, x + 24, y + 17, 0xFF8B8B8B);

        // 矢印枠
        guiGraphics.fill(x, y, x + 24, y + 1, 0xFF373737);
        guiGraphics.fill(x, y, x + 1, y + 17, 0xFF373737);
        guiGraphics.fill(x, y + 16, x + 24, y + 17, 0xFFFFFFFF);
        guiGraphics.fill(x + 23, y, x + 24, y + 17, 0xFFFFFFFF);

        // 進捗バー
        float progress = menu.getForgeProgressRatio();
        int progressWidth = (int) (22 * progress);

        if (progressWidth > 0) {
            // 進捗部分（オレンジ色 - 鍛造らしい色）
            guiGraphics.fill(x + 1, y + 1, x + 1 + progressWidth, y + 16, 0xFFFF8C00);
        }

        // 矢印記号
        int arrowX = x + 17;
        int arrowY = y + 8;
        guiGraphics.fill(arrowX, arrowY - 3, arrowX + 5, arrowY + 4, 0xFF404040);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
