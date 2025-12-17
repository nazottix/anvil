package io.github.nazottix.anvil.client.screen;

import io.github.nazottix.anvil.client.ui.AnvilColors;
import io.github.nazottix.anvil.client.ui.AnvilPanelRenderer;
import io.github.nazottix.anvil.menu.SkillTreeStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * スキルツリーステーションスクリーン
 *
 * ツールのスキルツリー画面を表示します。
 * ツールを入れてスキルポイントを割り振ります。
 *
 * レイアウト:
 * - 左側: ツールスロット
 * - 中央: スキルツリー表示エリア（スクロール可能）
 * - 下部: インベントリ
 *
 * 仕様書参照: docs/10_UI_UXデザイン.md
 */
public class SkillTreeStationScreen extends AbstractContainerScreen<SkillTreeStationMenu> {

    // UI定数
    private static final int TOOL_SLOT_X = 8;
    private static final int TOOL_SLOT_Y = 70;

    // スキルツリー表示エリア
    private static final int TREE_AREA_X = 30;
    private static final int TREE_AREA_Y = 18;
    private static final int TREE_AREA_WIDTH = 138;
    private static final int TREE_AREA_HEIGHT = 80;

    /**
     * コンストラクタ
     */
    public SkillTreeStationScreen(SkillTreeStationMenu menu, Inventory playerInventory, Component title) {
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
        // TODO: スキルツリー操作ボタンを追加
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

        // スキルツリー表示エリア（凹んだ内側パネル）
        AnvilPanelRenderer.renderInnerPanel(guiGraphics, x + TREE_AREA_X, y + TREE_AREA_Y,
                TREE_AREA_WIDTH, TREE_AREA_HEIGHT);

        // スキルツリーエリア紫枠
        guiGraphics.renderOutline(x + TREE_AREA_X - 1, y + TREE_AREA_Y - 1,
                TREE_AREA_WIDTH + 2, TREE_AREA_HEIGHT + 2, AnvilColors.ARCANE_PURPLE);

        // ツールスロット背景
        renderToolSlot(guiGraphics, x, y);


        // プレイヤーインベントリ境界線とスロット背景を描画（マイクラ標準風）
        AnvilPanelRenderer.renderInventorySlots(guiGraphics, x, y, 117, 175);

        // スキルツリープレビュー（ツールがセットされている場合）
        renderSkillTreePreview(guiGraphics, x + TREE_AREA_X, y + TREE_AREA_Y);
    }

    /**
     * ツールスロットを描画（マイクラ標準風スロット + 紫枠で強調）
     */
    private void renderToolSlot(GuiGraphics guiGraphics, int guiX, int guiY) {
        int slotX = guiX + TOOL_SLOT_X;
        int slotY = guiY + TOOL_SLOT_Y;

        // 紫枠で強調
        guiGraphics.fill(slotX - 2, slotY - 2, slotX + 18, slotY + 18, AnvilColors.ARCANE_PURPLE);
        // 内側に標準スロットを描画
        AnvilPanelRenderer.renderSlot(guiGraphics, slotX, slotY);
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
     * スキルツリープレビューを描画
     */
    private void renderSkillTreePreview(GuiGraphics guiGraphics, int areaX, int areaY) {
        // ツールがセットされていない場合はメッセージを表示
        if (this.menu.getSlot(0).getItem().isEmpty()) {
            Component message = Component.translatable("gui.anvil.skill_tree_station.insert_tool");
            int textWidth = this.font.width(message);
            guiGraphics.drawString(this.font, message,
                    areaX + (TREE_AREA_WIDTH - textWidth) / 2,
                    areaY + TREE_AREA_HEIGHT / 2 - 4,
                    0x888888, false);
        } else {
            // TODO: スキルツリーのプレビューを描画
            Component placeholder = Component.literal("Skill Tree");
            guiGraphics.drawCenteredString(this.font, placeholder,
                    areaX + TREE_AREA_WIDTH / 2,
                    areaY + TREE_AREA_HEIGHT / 2 - 4,
                    AnvilColors.ARCANE_PURPLE);
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
    }
}
