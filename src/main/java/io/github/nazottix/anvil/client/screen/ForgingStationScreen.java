package io.github.nazottix.anvil.client.screen;

import io.github.nazottix.anvil.client.ui.AnvilColors;
import io.github.nazottix.anvil.client.ui.AnvilPanelRenderer;
import io.github.nazottix.anvil.menu.ForgingStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 鍛造ステーションスクリーン
 *
 * 鍛造ステーションのクライアント側UI描画を担当します。
 * Station Blockスタイルに統一されたUIデザイン。
 *
 * レイアウト:
 * - 左側: 入力スロット（上）+ ハンマースロット（下）
 * - 中央: 進捗矢印
 * - 右側: 出力スロット
 * - 下部: プレイヤーインベントリ
 */
public class ForgingStationScreen extends AbstractContainerScreen<ForgingStationMenu> {

    // スロット位置（Menuと同期）
    private static final int INPUT_SLOT_X = 56;
    private static final int INPUT_SLOT_Y = 35;
    private static final int HAMMER_SLOT_X = 56;
    private static final int HAMMER_SLOT_Y = 71;
    private static final int OUTPUT_SLOT_X = 116;
    private static final int OUTPUT_SLOT_Y = 53;

    /**
     * コンストラクタ
     *
     * GUIサイズをStation Blockスタイル（200）に設定
     */
    public ForgingStationScreen(ForgingStationMenu menu, Inventory playerInventory, Component title) {
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

        // ハンマースロット背景（Station Blockスタイル）
        AnvilPanelRenderer.renderSlot(guiGraphics, x + HAMMER_SLOT_X, y + HAMMER_SLOT_Y);

        // 出力スロット背景（オレンジ枠で強調、Station Blockスタイル）
        guiGraphics.fill(x + OUTPUT_SLOT_X - 3, y + OUTPUT_SLOT_Y - 3,
                x + OUTPUT_SLOT_X + 19, y + OUTPUT_SLOT_Y + 19, AnvilColors.FORGE_ORANGE | 0xFF000000);
        AnvilPanelRenderer.renderSlot(guiGraphics, x + OUTPUT_SLOT_X, y + OUTPUT_SLOT_Y);

        // ハンマーアイコン（ハンマースロット横）
        renderHammerIcon(guiGraphics, x + 36, y + 72);

        // 進捗矢印
        renderProgressArrow(guiGraphics, x + 79, y + 52);

        // プレイヤーインベントリ境界線とスロット背景を描画（Station Blockスタイル）
        AnvilPanelRenderer.renderInventorySlots(guiGraphics, x, y, 117, 175);
    }

    /**
     * ハンマーアイコンを描画（Station Blockスタイル）
     */
    private void renderHammerIcon(GuiGraphics guiGraphics, int x, int y) {
        // シンプルなハンマー形状
        // 柄
        guiGraphics.fill(x + 6, y + 4, x + 8, y + 12, 0xFF8B4513);
        // ヘッド
        guiGraphics.fill(x + 2, y + 2, x + 12, y + 6, 0xFF5B6B7F);
    }

    /**
     * 進捗矢印を描画（Station Blockスタイル）
     */
    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        int arrowWidth = 24;
        int arrowHeight = 17;

        // 矢印背景（Station Blockスタイル）
        guiGraphics.fill(x, y, x + arrowWidth, y + arrowHeight, 0xFF374151);

        // 矢印枠（Station Blockスタイル）
        guiGraphics.fill(x, y, x + arrowWidth, y + 1, 0xFF1F2937);
        guiGraphics.fill(x, y, x + 1, y + arrowHeight, 0xFF1F2937);
        guiGraphics.fill(x, y + arrowHeight - 1, x + arrowWidth, y + arrowHeight, 0xFF5B6B7F);
        guiGraphics.fill(x + arrowWidth - 1, y, x + arrowWidth, y + arrowHeight, 0xFF5B6B7F);

        // 進捗バー
        float progress = menu.getForgeProgressRatio();
        int progressWidth = (int) (22 * progress);

        if (progressWidth > 0) {
            // 進捗部分（オレンジ色 - 鍛造らしい色）
            guiGraphics.fill(x + 1, y + 1, x + 1 + progressWidth, y + arrowHeight - 1, AnvilColors.FORGE_ORANGE);
        }

        // 矢印記号
        int arrowX = x + 17;
        int arrowY = y + 8;
        guiGraphics.fill(arrowX, arrowY - 3, arrowX + 5, arrowY + 4, 0xFF1F2937);
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

        // 鍛造状態を表示
        if (menu.isForging()) {
            int progress = (int) (menu.getForgeProgressRatio() * 100);
            Component progressText = Component.translatable("gui.anvil.forging_station.progress", progress);
            guiGraphics.drawString(this.font, progressText, 100, 6, AnvilColors.FORGE_ORANGE, false);
        }
    }
}
