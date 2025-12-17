package io.github.nazottix.anvil.client.screen;

import io.github.nazottix.anvil.client.ui.AnvilColors;
import io.github.nazottix.anvil.client.ui.AnvilPanelRenderer;
import io.github.nazottix.anvil.menu.RefineryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 精錬所スクリーン
 *
 * 精錬所画面のクライアント側描画を行います。
 * Station Blockスタイルに統一されたUIデザイン。
 *
 * レイアウト:
 * - 左側: 入力スロット（上）+ 燃料スロット（下）
 * - 中央: 精錬進捗矢印 + 燃料ゲージ
 * - 右側: 出力スロット
 * - 下部: プレイヤーインベントリ
 */
public class RefineryScreen extends AbstractContainerScreen<RefineryMenu> {

    // スロット位置（Menuと同期）
    private static final int INPUT_SLOT_X = 56;
    private static final int INPUT_SLOT_Y = 35;
    private static final int FUEL_SLOT_X = 56;
    private static final int FUEL_SLOT_Y = 71;
    private static final int OUTPUT_SLOT_X = 116;
    private static final int OUTPUT_SLOT_Y = 53;

    /**
     * コンストラクタ
     *
     * GUIサイズをStation Blockスタイル（200）に設定
     */
    public RefineryScreen(RefineryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // GUIサイズをStation Blockスタイルに統一
        this.imageWidth = 176;
        this.imageHeight = 200;
        // インベントリラベル位置をStation Blockスタイルに合わせる
        this.inventoryLabelY = 106;
    }

    @Override
    protected void init() {
        super.init();
        // タイトルラベル位置
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    // ============================================
    // 描画
    // ============================================

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // 立体的なメインパネルを描画（Station Blockスタイル）
        AnvilPanelRenderer.renderMainPanel(guiGraphics, x, y, this.imageWidth, this.imageHeight);

        // 入力スロット背景（Station Blockスタイル）
        AnvilPanelRenderer.renderSlot(guiGraphics, x + INPUT_SLOT_X, y + INPUT_SLOT_Y);

        // 燃料スロット背景（Station Blockスタイル）
        AnvilPanelRenderer.renderSlot(guiGraphics, x + FUEL_SLOT_X, y + FUEL_SLOT_Y);

        // 出力スロット背景（オレンジ枠で強調、Station Blockスタイル）
        guiGraphics.fill(x + OUTPUT_SLOT_X - 3, y + OUTPUT_SLOT_Y - 3,
                x + OUTPUT_SLOT_X + 19, y + OUTPUT_SLOT_Y + 19, AnvilColors.FORGE_ORANGE | 0xFF000000);
        AnvilPanelRenderer.renderSlot(guiGraphics, x + OUTPUT_SLOT_X, y + OUTPUT_SLOT_Y);

        // 燃料ゲージを描画
        renderFuelGauge(guiGraphics, x, y);

        // 精錬進捗矢印を描画
        renderProgressArrow(guiGraphics, x, y);

        // プレイヤーインベントリ境界線とスロット背景を描画（Station Blockスタイル）
        AnvilPanelRenderer.renderInventorySlots(guiGraphics, x, y, 117, 175);
    }

    /**
     * 燃料ゲージを描画（Station Blockスタイル）
     */
    private void renderFuelGauge(GuiGraphics guiGraphics, int x, int y) {
        // ゲージ位置（入力スロットの下）
        int gaugeX = x + 57;
        int gaugeY = y + 54;
        int gaugeWidth = 14;
        int gaugeHeight = 14;

        // ゲージ背景（暗い灰色）
        guiGraphics.fill(gaugeX, gaugeY, gaugeX + gaugeWidth, gaugeY + gaugeHeight, 0xFF1F2937);

        // 燃焼中の場合、炎ゲージを描画
        if (menu.isBurning()) {
            float burnRatio = menu.getFuelBurnRatio();
            int filledHeight = (int) (gaugeHeight * burnRatio);
            if (filledHeight > 0) {
                // 炎の色（オレンジ→黄色のグラデーション風）
                int flameY = gaugeY + gaugeHeight - filledHeight;
                guiGraphics.fill(gaugeX, flameY, gaugeX + gaugeWidth, gaugeY + gaugeHeight, AnvilColors.FORGE_ORANGE);
                // 中心部分を明るく
                if (filledHeight > 2) {
                    guiGraphics.fill(gaugeX + 3, flameY + 1, gaugeX + gaugeWidth - 3, gaugeY + gaugeHeight - 1, AnvilColors.MOLTEN_GOLD);
                }
            }
        }

        // ゲージ枠（Station Blockスタイル）
        guiGraphics.fill(gaugeX - 1, gaugeY - 1, gaugeX + gaugeWidth + 1, gaugeY, 0xFF374151);
        guiGraphics.fill(gaugeX - 1, gaugeY - 1, gaugeX, gaugeY + gaugeHeight + 1, 0xFF374151);
        guiGraphics.fill(gaugeX, gaugeY + gaugeHeight, gaugeX + gaugeWidth + 1, gaugeY + gaugeHeight + 1, 0xFF5B6B7F);
        guiGraphics.fill(gaugeX + gaugeWidth, gaugeY, gaugeX + gaugeWidth + 1, gaugeY + gaugeHeight, 0xFF5B6B7F);
    }

    /**
     * 精錬進捗矢印を描画（Station Blockスタイル）
     */
    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        // 矢印位置
        int arrowX = x + 79;
        int arrowY = y + 52;
        int arrowWidth = 24;
        int arrowHeight = 17;

        // 矢印背景（暗い灰色）
        guiGraphics.fill(arrowX, arrowY + 6, arrowX + arrowWidth, arrowY + 10, 0xFF1F2937);
        // 矢印の先端
        guiGraphics.fill(arrowX + arrowWidth - 4, arrowY + 4, arrowX + arrowWidth, arrowY + 12, 0xFF1F2937);

        // 精錬中の場合、進捗を描画
        if (menu.isRefining()) {
            float progressRatio = menu.getRefineProgressRatio();
            int filledWidth = (int) (arrowWidth * progressRatio);
            if (filledWidth > 0) {
                // 進捗バー（オレンジ色）
                guiGraphics.fill(arrowX, arrowY + 6, arrowX + filledWidth, arrowY + 10, AnvilColors.FORGE_ORANGE);
                // 先端部分
                if (filledWidth >= arrowWidth - 4) {
                    guiGraphics.fill(arrowX + arrowWidth - 4, arrowY + 4, arrowX + arrowWidth, arrowY + 12, AnvilColors.FORGE_ORANGE);
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
        // タイトル（Station Blockスタイルの白色）
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY,
                AnvilColors.ETHER_WHITE, false);

        // インベントリラベル（Station Blockスタイルの白色）
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY,
                AnvilColors.ETHER_WHITE, false);

        // 精錬状態を表示
        if (menu.isRefining()) {
            int progress = (int) (menu.getRefineProgressRatio() * 100);
            Component progressText = Component.translatable("gui.anvil.refinery.progress", progress);
            guiGraphics.drawString(this.font, progressText, 100, 6, AnvilColors.FORGE_ORANGE, false);
        }
    }
}
