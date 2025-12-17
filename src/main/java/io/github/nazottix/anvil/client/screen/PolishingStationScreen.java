package io.github.nazottix.anvil.client.screen;

import io.github.nazottix.anvil.menu.PolishingStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 研磨ステーションスクリーン
 *
 * 研磨ステーションのクライアント側UI描画を担当します。
 * 鍛造ステーションと同様のレイアウト（入力、研磨剤、出力、進捗バー）を表示します。
 */
public class PolishingStationScreen extends AbstractContainerScreen<PolishingStationMenu> {

    /**
     * コンストラクタ
     */
    public PolishingStationScreen(PolishingStationMenu menu, Inventory playerInventory, Component title) {
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

        // 研磨剤スロット枠
        drawSlot(guiGraphics, x + 55, y + 52);

        // 出力スロット枠
        drawSlot(guiGraphics, x + 115, y + 34);

        // 研磨剤アイコン（研磨剤スロット横）
        drawPolishIcon(guiGraphics, x + 36, y + 53);

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
     * 研磨アイコンを描画
     */
    private void drawPolishIcon(GuiGraphics guiGraphics, int x, int y) {
        // ダイヤ形状（研磨剤らしい形）
        guiGraphics.fill(x + 5, y + 2, x + 9, y + 6, 0xFF00FFFF);
        guiGraphics.fill(x + 4, y + 4, x + 10, y + 8, 0xFF00FFFF);
        guiGraphics.fill(x + 5, y + 6, x + 9, y + 12, 0xFF00AAAA);
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
        float progress = menu.getPolishProgressRatio();
        int progressWidth = (int) (22 * progress);

        if (progressWidth > 0) {
            // 進捗部分（水色 - 研磨らしい色）
            guiGraphics.fill(x + 1, y + 1, x + 1 + progressWidth, y + 16, 0xFF00FFFF);
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
