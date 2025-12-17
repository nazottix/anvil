package io.github.nazottix.anvil.client.screen;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.menu.RefineryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 精錬所スクリーン
 *
 * 精錬所画面のクライアント側描画を行います。
 * かまど風のシンプルなUIで、入力スロット、燃料スロット、出力スロットを配置。
 *
 * レイアウト:
 * - 左側: 入力スロット（上）+ 燃料スロット（下）
 * - 中央: 精錬進捗矢印 + 燃料ゲージ
 * - 右側: 出力スロット
 * - 下部: プレイヤーインベントリ
 */
public class RefineryScreen extends AbstractContainerScreen<RefineryMenu> {

    // テクスチャリソース（かまど風レイアウト）
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "textures/gui/refinery.png");

    /**
     * コンストラクタ
     */
    public RefineryScreen(RefineryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // GUI のサイズを設定（かまどと同じサイズ）
        this.imageWidth = 176;
        this.imageHeight = 166;
        // ラベル位置
        this.titleLabelY = 6;
        this.inventoryLabelY = this.imageHeight - 94;
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

        // メインパネル背景を描画（カスタムデザイン）
        renderMainPanel(guiGraphics, x, y);

        // スロット背景を描画
        renderSlots(guiGraphics, x, y);

        // 燃料ゲージを描画
        renderFuelGauge(guiGraphics, x, y);

        // 精錬進捗矢印を描画
        renderProgressArrow(guiGraphics, x, y);

        // プレイヤーインベントリスロットを描画
        renderInventorySlots(guiGraphics, x, y);
    }

    /**
     * メインパネル背景を描画
     */
    private void renderMainPanel(GuiGraphics guiGraphics, int x, int y) {
        // 外枠（濃いグレー）
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFFC6C6C6);

        // 外枠の立体感（明るい縁）
        guiGraphics.fill(x, y, x + imageWidth - 1, y + 1, 0xFFFFFFFF);
        guiGraphics.fill(x, y, x + 1, y + imageHeight - 1, 0xFFFFFFFF);

        // 外枠の立体感（暗い縁）
        guiGraphics.fill(x + 1, y + imageHeight - 1, x + imageWidth, y + imageHeight, 0xFF555555);
        guiGraphics.fill(x + imageWidth - 1, y + 1, x + imageWidth, y + imageHeight, 0xFF555555);

        // 内側の背景（ダークグレー）
        guiGraphics.fill(x + 4, y + 4, x + imageWidth - 4, y + 70, 0xFF373737);
    }

    /**
     * スロット背景を描画
     */
    private void renderSlots(GuiGraphics guiGraphics, int x, int y) {
        // 入力スロット（上、位置 56,17）
        renderSlot(guiGraphics, x + 56, y + 17);

        // 燃料スロット（下、位置 56,53）
        renderSlot(guiGraphics, x + 56, y + 53);

        // 出力スロット（右、位置 116,35、少し大きめ）
        renderOutputSlot(guiGraphics, x + 116, y + 35);
    }

    /**
     * 標準スロットを描画
     */
    private void renderSlot(GuiGraphics guiGraphics, int x, int y) {
        // スロット枠（暗い）
        guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF373737);
        // スロット背景
        guiGraphics.fill(x, y, x + 16, y + 16, 0xFF8B8B8B);
        // 内側の影（上と左）
        guiGraphics.fill(x, y, x + 16, y + 1, 0xFF555555);
        guiGraphics.fill(x, y, x + 1, y + 16, 0xFF555555);
    }

    /**
     * 出力スロットを描画（オレンジ枠で強調）
     */
    private void renderOutputSlot(GuiGraphics guiGraphics, int x, int y) {
        // オレンジ外枠
        guiGraphics.fill(x - 3, y - 3, x + 19, y + 19, 0xFFFF8C00);
        // 内側の標準スロット
        renderSlot(guiGraphics, x, y);
    }

    /**
     * 燃料ゲージを描画
     */
    private void renderFuelGauge(GuiGraphics guiGraphics, int x, int y) {
        // ゲージ位置（入力スロットの下）
        int gaugeX = x + 57;
        int gaugeY = y + 36;
        int gaugeWidth = 14;
        int gaugeHeight = 14;

        // ゲージ背景（暗い灰色）
        guiGraphics.fill(gaugeX, gaugeY, gaugeX + gaugeWidth, gaugeY + gaugeHeight, 0xFF2A2A2A);

        // 燃焼中の場合、炎ゲージを描画
        if (menu.isBurning()) {
            float burnRatio = menu.getFuelBurnRatio();
            int filledHeight = (int) (gaugeHeight * burnRatio);
            if (filledHeight > 0) {
                // 炎の色（オレンジ→黄色のグラデーション風）
                int flameY = gaugeY + gaugeHeight - filledHeight;
                guiGraphics.fill(gaugeX, flameY, gaugeX + gaugeWidth, gaugeY + gaugeHeight, 0xFFFF6600);
                // 中心部分を明るく
                if (filledHeight > 2) {
                    guiGraphics.fill(gaugeX + 3, flameY + 1, gaugeX + gaugeWidth - 3, gaugeY + gaugeHeight - 1, 0xFFFFCC00);
                }
            }
        }

        // ゲージ枠
        guiGraphics.fill(gaugeX - 1, gaugeY - 1, gaugeX + gaugeWidth + 1, gaugeY, 0xFF555555);
        guiGraphics.fill(gaugeX - 1, gaugeY - 1, gaugeX, gaugeY + gaugeHeight + 1, 0xFF555555);
        guiGraphics.fill(gaugeX, gaugeY + gaugeHeight, gaugeX + gaugeWidth + 1, gaugeY + gaugeHeight + 1, 0xFFAAAAAA);
        guiGraphics.fill(gaugeX + gaugeWidth, gaugeY, gaugeX + gaugeWidth + 1, gaugeY + gaugeHeight, 0xFFAAAAAA);
    }

    /**
     * 精錬進捗矢印を描画
     */
    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        // 矢印位置
        int arrowX = x + 79;
        int arrowY = y + 34;
        int arrowWidth = 24;
        int arrowHeight = 17;

        // 矢印背景（暗い灰色）
        guiGraphics.fill(arrowX, arrowY + 6, arrowX + arrowWidth, arrowY + 10, 0xFF3A3A3A);
        // 矢印の先端
        guiGraphics.fill(arrowX + arrowWidth - 4, arrowY + 4, arrowX + arrowWidth, arrowY + 12, 0xFF3A3A3A);

        // 精錬中の場合、進捗を描画
        if (menu.isRefining()) {
            float progressRatio = menu.getRefineProgressRatio();
            int filledWidth = (int) (arrowWidth * progressRatio);
            if (filledWidth > 0) {
                // 進捗バー（緑色）
                guiGraphics.fill(arrowX, arrowY + 6, arrowX + filledWidth, arrowY + 10, 0xFF00FF00);
                // 先端部分
                if (filledWidth >= arrowWidth - 4) {
                    guiGraphics.fill(arrowX + arrowWidth - 4, arrowY + 4, arrowX + arrowWidth, arrowY + 12, 0xFF00FF00);
                }
            }
        }
    }

    /**
     * プレイヤーインベントリスロットを描画
     */
    private void renderInventorySlots(GuiGraphics guiGraphics, int x, int y) {
        // インベントリ背景領域
        guiGraphics.fill(x + 7, y + 83, x + 169, y + 141, 0xFF373737);
        guiGraphics.fill(x + 7, y + 143, x + 169, y + 161, 0xFF373737);

        // インベントリスロット（3行9列）
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotX = x + 8 + col * 18;
                int slotY = y + 84 + row * 18;
                renderSlot(guiGraphics, slotX, slotY);
            }
        }

        // ホットバー（1行9列）
        for (int col = 0; col < 9; col++) {
            int slotX = x + 8 + col * 18;
            int slotY = y + 142;
            renderSlot(guiGraphics, slotX, slotY);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // タイトル（白色）
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFF, false);

        // インベントリラベル（灰色）
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);

        // 精錬状態を表示
        if (menu.isRefining()) {
            int progress = (int) (menu.getRefineProgressRatio() * 100);
            Component progressText = Component.translatable("gui.anvil.refinery.progress", progress);
            guiGraphics.drawString(this.font, progressText, 100, 6, 0x00FF00, false);
        }
    }
}
