package io.github.nazottix.anvil.client.screen;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.menu.ToolStationMenu;
import io.github.nazottix.anvil.tool.ToolType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * ツールステーションスクリーン
 *
 * ツール作成画面のクライアント側描画を行います。
 *
 * 仕様書参照: docs/10_UI_UXデザイン.md
 */
public class ToolStationScreen extends AbstractContainerScreen<ToolStationMenu> {

    // テクスチャのパス
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "textures/gui/tool_station.png");

    // ツールタイプ選択ボタンの位置
    private static final int TOOL_BUTTON_X = 7;
    private static final int TOOL_BUTTON_Y = 7;
    private static final int TOOL_BUTTON_SIZE = 18;
    private static final int TOOL_BUTTONS_PER_ROW = 4;

    /**
     * コンストラクタ
     */
    public ToolStationScreen(ToolStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // GUI のサイズを設定
        this.imageWidth = 176;
        this.imageHeight = 166;
        // インベントリラベルの位置調整
        this.inventoryLabelY = this.imageHeight - 94;
    }

    // ============================================
    // 描画
    // ============================================

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // 背景テクスチャを描画
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // メインテクスチャ（仮実装：バニラのクラフティングテーブルテクスチャを使用）
        guiGraphics.blit(
                ResourceLocation.withDefaultNamespace("textures/gui/container/crafting_table.png"),
                x, y,
                0, 0,
                this.imageWidth, this.imageHeight
        );

        // 選択されたツールタイプのハイライト
        renderToolTypeHighlight(guiGraphics, x, y);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 背景を描画
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        // ツールチップを描画
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // タイトルを描画
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        // インベントリラベルを描画
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);

        // 選択されたツールタイプを表示
        ToolType selectedType = this.menu.getSelectedToolType();
        if (selectedType != null) {
            String typeName = getToolTypeName(selectedType);
            guiGraphics.drawString(this.font, typeName, 100, 6, 0x404040, false);
        }
    }

    /**
     * ツールタイプの選択ハイライトを描画
     */
    private void renderToolTypeHighlight(GuiGraphics guiGraphics, int guiX, int guiY) {
        // 現在は簡易実装
        // TODO: ツールタイプ選択UIを実装
    }

    /**
     * ツールタイプの表示名を取得
     */
    private String getToolTypeName(ToolType type) {
        return switch (type) {
            case PICKAXE -> "ピッケル";
            case AXE -> "斧";
            case SHOVEL -> "シャベル";
            case SWORD -> "剣";
            case HOE -> "クワ";
            case BOW -> "弓";
            case FISHING_ROD -> "釣り竿";
            case SHEARS -> "ハサミ";
        };
    }

    // ============================================
    // マウス操作
    // ============================================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // ツールタイプ選択ボタンのクリック処理
        int guiX = (this.width - this.imageWidth) / 2;
        int guiY = (this.height - this.imageHeight) / 2;

        // TODO: ツールタイプ選択ボタンのクリック判定を実装

        return super.mouseClicked(mouseX, mouseY, button);
    }

    // ============================================
    // キーボード操作
    // ============================================

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 数字キーでツールタイプを選択
        if (keyCode >= 49 && keyCode <= 56) { // 1-8キー
            int toolIndex = keyCode - 49;
            this.menu.setSelectedToolType(toolIndex);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
