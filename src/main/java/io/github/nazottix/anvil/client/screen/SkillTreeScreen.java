package io.github.nazottix.anvil.client.screen;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.client.ui.AnvilColors;
import io.github.nazottix.anvil.menu.SkillTreeMenu;
import io.github.nazottix.anvil.skill.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * スキルツリースクリーン
 *
 * Path of Exile風のスキルツリーUI。
 * ズーム・パン対応の大規模ツリー表示。
 *
 * 仕様書参照: docs/03_スキルツリーシステム.md
 */
public class SkillTreeScreen extends AbstractContainerScreen<SkillTreeMenu> {

    // UI定数
    private static final int TREE_AREA_X = 40;
    private static final int TREE_AREA_Y = 10;
    private static final int TREE_AREA_WIDTH = 280;
    private static final int TREE_AREA_HEIGHT = 150;

    // ビュー変換
    private float viewX = 0;
    private float viewY = 0;
    private float zoom = 1.0f;
    private static final float MIN_ZOOM = 0.5f;
    private static final float MAX_ZOOM = 2.0f;

    // ドラッグ状態
    private boolean isDragging = false;
    private int dragStartX, dragStartY;
    private float dragStartViewX, dragStartViewY;

    // ホバー中のノード
    private SkillNode hoveredNode = null;

    /**
     * コンストラクタ
     */
    public SkillTreeScreen(SkillTreeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // GUIサイズを拡大
        this.imageWidth = 360;
        this.imageHeight = 256;
        this.inventoryLabelY = this.imageHeight - 82;
    }

    @Override
    protected void init() {
        super.init();

        // 適用ボタン
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.anvil.apply"),
                button -> {
                    this.menu.applyChanges();
                    ANVIL.LOGGER.info("スキル配分を適用しました");
                }
        ).bounds(this.leftPos + 8, this.topPos + 165, 40, 16).build());

        // リセットボタン
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.anvil.reset"),
                button -> {
                    this.menu.resetChanges();
                    ANVIL.LOGGER.info("スキル配分をリセットしました");
                }
        ).bounds(this.leftPos + 8, this.topPos + 183, 40, 16).build());

        // ビューをルートノード中心に設定
        centerOnRoot();
    }

    /**
     * ルートノードにビューを中心化
     */
    private void centerOnRoot() {
        SkillTree tree = this.menu.getCurrentTree();
        if (tree != null) {
            tree.getRootNode().ifPresent(root -> {
                viewX = -root.posX();
                viewY = -root.posY() + 50;
            });
        }
    }

    // ============================================
    // 描画
    // ============================================

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // 背景色
        guiGraphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight,
                AnvilColors.VOID_BLACK | 0xFF000000);

        // パネル背景
        guiGraphics.fill(this.leftPos + 2, this.topPos + 2, this.leftPos + this.imageWidth - 2, this.topPos + this.imageHeight - 2,
                AnvilColors.ANVIL_STEEL | 0xFF000000);

        // ツリーエリア背景
        int treeX = this.leftPos + TREE_AREA_X;
        int treeY = this.topPos + TREE_AREA_Y;
        guiGraphics.fill(treeX, treeY, treeX + TREE_AREA_WIDTH, treeY + TREE_AREA_HEIGHT,
                0xFF0A0A15);

        // ツリー枠
        guiGraphics.renderOutline(treeX - 1, treeY - 1, TREE_AREA_WIDTH + 2, TREE_AREA_HEIGHT + 2, 0xFF444444);

        // スキルツリー描画
        renderSkillTree(guiGraphics, mouseX, mouseY);

        // ポイント情報描画
        renderPointInfo(guiGraphics);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // ノードツールチップ
        if (hoveredNode != null) {
            renderNodeTooltip(guiGraphics, mouseX, mouseY, hoveredNode);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // タイトル
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY,
                AnvilColors.ETHER_WHITE, false);

        // ツリー名
        SkillTree tree = this.menu.getCurrentTree();
        if (tree != null) {
            String treeName = tree.toolType().toUpperCase() + " スキルツリー";
            guiGraphics.drawString(this.font, treeName, TREE_AREA_X, TREE_AREA_Y - 10,
                    AnvilColors.TECH_CYAN, false);
        }
    }

    /**
     * スキルツリーを描画
     */
    private void renderSkillTree(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        SkillTree tree = this.menu.getCurrentTree();
        if (tree == null) {
            // ツールをセットしてください
            int centerX = this.leftPos + TREE_AREA_X + TREE_AREA_WIDTH / 2;
            int centerY = this.topPos + TREE_AREA_Y + TREE_AREA_HEIGHT / 2;
            guiGraphics.drawCenteredString(this.font, "ツールをセットしてください",
                    centerX, centerY, 0x888888);
            return;
        }

        SkillAllocation allocation = this.menu.getEditingAllocation();
        int treeX = this.leftPos + TREE_AREA_X;
        int treeY = this.topPos + TREE_AREA_Y;

        // クリッピング領域を設定
        guiGraphics.enableScissor(treeX, treeY, treeX + TREE_AREA_WIDTH, treeY + TREE_AREA_HEIGHT);

        hoveredNode = null;

        // 接続線を描画
        for (SkillNode node : tree.nodes()) {
            int nodeScreenX = worldToScreenX(node.posX());
            int nodeScreenY = worldToScreenY(node.posY());

            for (ResourceLocation connId : node.connections()) {
                tree.getNode(connId).ifPresent(connNode -> {
                    int connScreenX = worldToScreenX(connNode.posX());
                    int connScreenY = worldToScreenY(connNode.posY());

                    // 線の色（両端が解放済みなら明るく）
                    boolean bothAllocated = allocation.isAllocated(node.id()) && allocation.isAllocated(connId);
                    int lineColor = bothAllocated ? 0xFFFFFF00 : 0xFF444444;

                    drawLine(guiGraphics, nodeScreenX, nodeScreenY, connScreenX, connScreenY, lineColor);
                });
            }
        }

        // ノードを描画
        for (SkillNode node : tree.nodes()) {
            int screenX = worldToScreenX(node.posX());
            int screenY = worldToScreenY(node.posY());

            // 画面外はスキップ
            if (screenX < treeX - 20 || screenX > treeX + TREE_AREA_WIDTH + 20 ||
                    screenY < treeY - 20 || screenY > treeY + TREE_AREA_HEIGHT + 20) {
                continue;
            }

            renderNode(guiGraphics, node, screenX, screenY, allocation, mouseX, mouseY);
        }

        guiGraphics.disableScissor();
    }

    /**
     * 単一のノードを描画
     */
    private void renderNode(GuiGraphics guiGraphics, SkillNode node, int x, int y,
                            SkillAllocation allocation, int mouseX, int mouseY) {
        int size = (int) (node.type().getDisplaySize() * zoom);
        int halfSize = size / 2;

        int left = x - halfSize;
        int top = y - halfSize;
        int right = x + halfSize;
        int bottom = y + halfSize;

        // 解放状態によって色を変更
        boolean isAllocated = allocation.isAllocated(node.id());
        int baseColor = node.type().getDisplayColor();

        if (isAllocated) {
            // 解放済み: 明るい色
            guiGraphics.fill(left, top, right, bottom, baseColor | 0xFF000000);
        } else {
            // 未解放: 暗い色
            int darkColor = ((baseColor >> 1) & 0x7F7F7F);
            guiGraphics.fill(left, top, right, bottom, darkColor | 0xFF000000);
        }

        // 枠
        int borderColor = isAllocated ? 0xFFFFFFFF : 0xFF666666;
        guiGraphics.renderOutline(left, top, size, size, borderColor);

        // ホバーチェック
        if (mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom) {
            hoveredNode = node;
            guiGraphics.fill(left, top, right, bottom, 0x44FFFFFF);
        }

        // ノードタイプシンボル
        String symbol = getNodeSymbol(node.type());
        if (size >= 16) {
            guiGraphics.drawCenteredString(this.font, symbol, x, y - 4, 0xFFFFFF);
        }
    }

    /**
     * ノードのシンボルを取得
     */
    private String getNodeSymbol(SkillNodeType type) {
        return switch (type) {
            case MINOR -> "○";
            case NOTABLE -> "◇";
            case KEYSTONE -> "★";
            case JEWEL_SOCKET -> "◈";
            case MASTERY -> "✦";
            case ROOT -> "◉";
        };
    }

    /**
     * 線を描画
     */
    private void drawLine(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
        // 簡易的な線描画（水平/垂直のみ対応、斜め線は点で近似）
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int steps = Math.max(dx, dy);

        if (steps == 0) return;

        for (int i = 0; i <= steps; i++) {
            int x = x1 + (x2 - x1) * i / steps;
            int y = y1 + (y2 - y1) * i / steps;
            guiGraphics.fill(x, y, x + 1, y + 1, color);
        }
    }

    /**
     * ポイント情報を描画
     */
    private void renderPointInfo(GuiGraphics guiGraphics) {
        int x = this.leftPos + 8;
        int y = this.topPos + TREE_AREA_Y;

        // スキルポイント
        int available = this.menu.getAvailablePoints();
        int used = this.menu.getUsedPoints();
        int total = this.menu.getTotalPoints();

        guiGraphics.drawString(this.font, "スキルポイント", x, y, AnvilColors.TECH_CYAN, false);
        guiGraphics.drawString(this.font, String.format("%d / %d", available, total), x, y + 12, AnvilColors.ETHER_WHITE, false);

        // 解放ノード数
        SkillAllocation allocation = this.menu.getEditingAllocation();
        int allocatedCount = allocation.getAllocatedCount();
        guiGraphics.drawString(this.font, "解放ノード", x, y + 30, AnvilColors.TECH_CYAN, false);
        guiGraphics.drawString(this.font, String.valueOf(allocatedCount), x, y + 42, AnvilColors.ETHER_WHITE, false);

        // レベル
        int level = this.menu.getToolLevel();
        guiGraphics.drawString(this.font, "Lv." + level, x, y + 60, AnvilColors.FORGE_ORANGE, false);
    }

    /**
     * ノードのツールチップを描画
     */
    private void renderNodeTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, SkillNode node) {
        List<Component> tooltip = new ArrayList<>();

        // ノード名
        String nodeName = node.id().getPath().replace('/', ' ').replace('_', ' ');
        int nameColor = node.type().getDisplayColor();
        tooltip.add(Component.literal(nodeName).withStyle(s -> s.withColor(nameColor)));

        // タイプ
        tooltip.add(Component.literal("タイプ: " + node.type().name()).withStyle(s -> s.withColor(0xAAAAAA)));

        // コスト
        tooltip.add(Component.literal("コスト: " + node.getActualCost() + " SP").withStyle(s -> s.withColor(0xFFFF00)));

        // 効果
        if (!node.effects().isEmpty()) {
            tooltip.add(Component.literal("効果:").withStyle(s -> s.withColor(0x55FF55)));
            for (SkillEffect effect : node.effects()) {
                String effectText = "  " + effect.getDisplayString();
                int effectColor = effect.isPositive() ? 0x55FF55 : 0xFF5555;
                tooltip.add(Component.literal(effectText).withStyle(s -> s.withColor(effectColor)));
            }
        }

        // 解放条件
        SkillNode.NodeRequirements req = node.requirements();
        if (req.minLevel() > 0 || req.prerequisiteNodes() > 0) {
            tooltip.add(Component.literal("解放条件:").withStyle(s -> s.withColor(0xFFAA00)));
            if (req.minLevel() > 0) {
                tooltip.add(Component.literal("  Lv." + req.minLevel() + " 以上").withStyle(s -> s.withColor(0xAAAAAA)));
            }
            if (req.prerequisiteNodes() > 0) {
                tooltip.add(Component.literal("  " + req.prerequisiteNodes() + " ノード解放").withStyle(s -> s.withColor(0xAAAAAA)));
            }
        }

        // 解放状態
        SkillAllocation allocation = this.menu.getEditingAllocation();
        if (allocation.isAllocated(node.id())) {
            tooltip.add(Component.literal("✓ 解放済み").withStyle(s -> s.withColor(0x55FF55)));
        }

        guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
    }

    // ============================================
    // 座標変換
    // ============================================

    private int worldToScreenX(int worldX) {
        return this.leftPos + TREE_AREA_X + TREE_AREA_WIDTH / 2 + (int) ((worldX + viewX) * zoom);
    }

    private int worldToScreenY(int worldY) {
        return this.topPos + TREE_AREA_Y + TREE_AREA_HEIGHT / 2 + (int) ((worldY + viewY) * zoom);
    }

    private int screenToWorldX(int screenX) {
        return (int) ((screenX - this.leftPos - TREE_AREA_X - TREE_AREA_WIDTH / 2) / zoom - viewX);
    }

    private int screenToWorldY(int screenY) {
        return (int) ((screenY - this.topPos - TREE_AREA_Y - TREE_AREA_HEIGHT / 2) / zoom - viewY);
    }

    // ============================================
    // マウス操作
    // ============================================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX;
        int my = (int) mouseY;

        // ツリーエリア内かチェック
        int treeX = this.leftPos + TREE_AREA_X;
        int treeY = this.topPos + TREE_AREA_Y;

        if (mx >= treeX && mx < treeX + TREE_AREA_WIDTH &&
                my >= treeY && my < treeY + TREE_AREA_HEIGHT) {

            if (button == 0) { // 左クリック
                if (hoveredNode != null) {
                    // ノードを解放
                    if (this.menu.allocateNode(hoveredNode.id())) {
                        ANVIL.LOGGER.info("ノードを解放: {}", hoveredNode.id());
                    }
                    return true;
                } else {
                    // ドラッグ開始
                    isDragging = true;
                    dragStartX = mx;
                    dragStartY = my;
                    dragStartViewX = viewX;
                    dragStartViewY = viewY;
                    return true;
                }
            } else if (button == 1) { // 右クリック
                if (hoveredNode != null) {
                    // ノードを解除
                    if (this.menu.deallocateNode(hoveredNode.id())) {
                        ANVIL.LOGGER.info("ノードを解除: {}", hoveredNode.id());
                    }
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging && button == 0) {
            int mx = (int) mouseX;
            int my = (int) mouseY;
            viewX = dragStartViewX + (mx - dragStartX) / zoom;
            viewY = dragStartViewY + (my - dragStartY) / zoom;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int mx = (int) mouseX;
        int my = (int) mouseY;

        // ツリーエリア内でズーム
        int treeX = this.leftPos + TREE_AREA_X;
        int treeY = this.topPos + TREE_AREA_Y;

        if (mx >= treeX && mx < treeX + TREE_AREA_WIDTH &&
                my >= treeY && my < treeY + TREE_AREA_HEIGHT) {

            float oldZoom = zoom;
            zoom += (float) scrollY * 0.1f;
            zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));

            // ズーム中心をマウス位置に合わせる
            if (zoom != oldZoom) {
                int worldX = screenToWorldX(mx);
                int worldY = screenToWorldY(my);
                viewX = -worldX + (mx - this.leftPos - TREE_AREA_X - TREE_AREA_WIDTH / 2) / zoom;
                viewY = -worldY + (my - this.topPos - TREE_AREA_Y - TREE_AREA_HEIGHT / 2) / zoom;
            }

            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Rキーでリセット
        if (keyCode == 82) { // R
            centerOnRoot();
            zoom = 1.0f;
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
