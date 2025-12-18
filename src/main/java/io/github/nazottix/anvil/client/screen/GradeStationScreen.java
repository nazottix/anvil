package io.github.nazottix.anvil.client.screen;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.block.entity.GradeStationBlockEntity;
import io.github.nazottix.anvil.client.ui.AnvilButtonRenderer;
import io.github.nazottix.anvil.client.ui.AnvilColors;
import io.github.nazottix.anvil.client.ui.AnvilPanelRenderer;
import io.github.nazottix.anvil.material.Grade;
import io.github.nazottix.anvil.menu.GradeStationMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * グレードステーションスクリーン
 *
 * パーツのグレードアップグレード画面のクライアント側描画を行います。
 * パーツスロット、エッセンススロット、アップグレードボタンを提供。
 *
 * レイアウト:
 * - 上部: パーツスロット + エッセンススロット + アップグレードボタン
 * - 中央: グレード情報表示
 * - 下部: インベントリ
 */
public class GradeStationScreen extends AbstractContainerScreen<GradeStationMenu> {

    // スロット位置
    private static final int PART_SLOT_X = 26;
    private static final int PART_SLOT_Y = 35;
    private static final int ESSENCE_SLOT_X = 80;
    private static final int ESSENCE_SLOT_Y = 35;

    // アップグレードボタン
    private static final int UPGRADE_BUTTON_X = 116;
    private static final int UPGRADE_BUTTON_Y = 35;
    private static final int UPGRADE_BUTTON_WIDTH = 50;
    private static final int UPGRADE_BUTTON_HEIGHT = 16;

    /**
     * コンストラクタ
     */
    public GradeStationScreen(GradeStationMenu menu, Inventory playerInventory, Component title) {
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

        // 立体的なメインパネルを描画（マイクラ従来の奥行きあるデザイン）
        AnvilPanelRenderer.renderMainPanel(guiGraphics, x, y, this.imageWidth, this.imageHeight);

        // スロットエリア背景
        renderSlotAreas(guiGraphics, x, y);

        // グレード情報を描画
        renderGradeInfo(guiGraphics, x, y);

        // アップグレードボタンを描画
        renderUpgradeButton(guiGraphics, x, y, mouseX, mouseY);

        // 矢印描画（パーツ + エッセンス → アップグレード）
        renderArrow(guiGraphics, x + 48, y + 39);

        // プレイヤーインベントリ境界線とスロット背景を描画（マイクラ標準風）
        AnvilPanelRenderer.renderInventorySlots(guiGraphics, x, y, 84, 142);
    }

    /**
     * スロットエリアを描画（マイクラ標準風スロット + 色付き枠で強調）
     */
    private void renderSlotAreas(GuiGraphics guiGraphics, int guiX, int guiY) {
        // パーツ入力スロット背景（紫枠で強調 - グレードを示す）
        guiGraphics.fill(guiX + PART_SLOT_X - 3, guiY + PART_SLOT_Y - 3,
                guiX + PART_SLOT_X + 19, guiY + PART_SLOT_Y + 19, 0xFF9400D3);
        // 内側に標準スロットを描画
        AnvilPanelRenderer.renderSlot(guiGraphics, guiX + PART_SLOT_X, guiY + PART_SLOT_Y);

        // エッセンス入力スロット背景（標準スロット）
        AnvilPanelRenderer.renderSlot(guiGraphics, guiX + ESSENCE_SLOT_X, guiY + ESSENCE_SLOT_Y);
    }

    /**
     * グレード情報を描画
     */
    private void renderGradeInfo(GuiGraphics guiGraphics, int guiX, int guiY) {
        Grade currentGrade = this.menu.getCurrentGrade();

        if (currentGrade != null) {
            Grade nextGrade = GradeStationBlockEntity.getNextGrade(currentGrade);
            int requiredEssence = this.menu.getRequiredEssence();
            int currentEssence = this.menu.getCurrentEssenceCount();

            // 現在のグレード
            int gradeColor = currentGrade.getColor();
            Component currentText = Component.literal(currentGrade.getDisplayName());
            guiGraphics.drawString(this.font, currentText, guiX + 26, guiY + 55, gradeColor, true);

            // 矢印
            guiGraphics.drawString(this.font, "→", guiX + 48, guiY + 55, 0xFFFFFF, false);

            // 次のグレード
            if (nextGrade != null) {
                int nextGradeColor = nextGrade.getColor();
                Component nextText = Component.literal(nextGrade.getDisplayName());
                guiGraphics.drawString(this.font, nextText, guiX + 60, guiY + 55, nextGradeColor, true);

                // 必要なエッセンス数
                int essenceColor = currentEssence >= requiredEssence ? 0x55FF55 : 0xFF5555;
                Component essenceText = Component.translatable("gui.anvil.grade_station.essence_cost",
                        currentEssence, requiredEssence);
                guiGraphics.drawString(this.font, essenceText, guiX + 80, guiY + 55, essenceColor, false);
            } else {
                // MAXの場合
                Component maxText = Component.translatable("gui.anvil.grade_station.max_grade");
                guiGraphics.drawString(this.font, maxText, guiX + 60, guiY + 55, 0xFFD700, false);
            }
        }
    }

    /**
     * アップグレードボタンを描画
     * AnvilButtonRendererユーティリティを使用してテーマに統一
     */
    private void renderUpgradeButton(GuiGraphics guiGraphics, int guiX, int guiY, int mouseX, int mouseY) {
        int btnX = guiX + UPGRADE_BUTTON_X;
        int btnY = guiY + UPGRADE_BUTTON_Y;

        // AnvilButtonRendererを使用してボタンを描画
        boolean isHovered = AnvilButtonRenderer.isMouseOverButton(mouseX, mouseY, btnX, btnY, UPGRADE_BUTTON_WIDTH, UPGRADE_BUTTON_HEIGHT);
        boolean canUpgrade = this.menu.canUpgrade();
        Component upgradeText = Component.translatable("gui.anvil.grade_station.upgrade");
        AnvilButtonRenderer.renderButton(guiGraphics, this.font, btnX, btnY, UPGRADE_BUTTON_WIDTH, UPGRADE_BUTTON_HEIGHT, upgradeText, isHovered, canUpgrade);
    }

    /**
     * 矢印を描画
     */
    private void renderArrow(GuiGraphics guiGraphics, int x, int y) {
        // 簡易矢印（+ 記号）
        guiGraphics.fill(x + 8, y, x + 10, y + 8, 0xFFAAAAAA);
        guiGraphics.fill(x + 4, y + 3, x + 14, y + 5, 0xFFAAAAAA);
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
        guiGraphics.drawString(this.font, Component.translatable("gui.anvil.grade_station.part"),
                PART_SLOT_X, PART_SLOT_Y - 10, 0x888888, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.anvil.grade_station.essence"),
                ESSENCE_SLOT_X, ESSENCE_SLOT_Y - 10, 0x888888, false);
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
            // アップグレードボタンのクリック判定
            int upgradeBtnX = guiX + UPGRADE_BUTTON_X;
            int upgradeBtnY = guiY + UPGRADE_BUTTON_Y;
            if (AnvilButtonRenderer.isMouseOverButton((int) mouseX, (int) mouseY, upgradeBtnX, upgradeBtnY, UPGRADE_BUTTON_WIDTH, UPGRADE_BUTTON_HEIGHT)) {
                if (this.menu.canUpgrade()) {
                    // サーバーにボタンクリックを送信（ボタンID 0 = アップグレード）
                    if (mc.gameMode != null) {
                        mc.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
                    }
                    // AnvilButtonRendererを使用してアップグレード音を再生
                    AnvilButtonRenderer.playButtonSound(AnvilButtonRenderer.ButtonSound.ACTION);
                    ANVIL.LOGGER.info("グレードアップグレードを実行");
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
