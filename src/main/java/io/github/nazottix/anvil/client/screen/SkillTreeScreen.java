package io.github.nazottix.anvil.client.screen;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.client.ui.AnvilColors;
import io.github.nazottix.anvil.item.JewelItem;
import io.github.nazottix.anvil.menu.SkillTreeMenu;
import io.github.nazottix.anvil.skill.*;
import io.github.nazottix.anvil.skill.jewel.JewelData;
import io.github.nazottix.anvil.skill.jewel.JewelRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    // ドラッグ状態（ビューパン用）
    private boolean isDragging = false;
    private int dragStartX, dragStartY;
    private float dragStartViewX, dragStartViewY;

    // ホバー中のノード
    private SkillNode hoveredNode = null;

    // ジュエルドラッグ状態
    private ItemStack draggingJewel = ItemStack.EMPTY;
    private int draggingSourceSlot = -1;

    // ホバー中のジュエルソケット
    private SkillNode hoveredSocket = null;

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

        // ノードツールチップ（ドラッグ中は非表示）
        if (hoveredNode != null && draggingJewel.isEmpty()) {
            renderNodeTooltip(guiGraphics, mouseX, mouseY, hoveredNode);
        }

        // ドラッグ中のジュエルを描画
        if (!draggingJewel.isEmpty()) {
            renderDraggingJewel(guiGraphics, mouseX, mouseY);
        }
    }

    /**
     * ドラッグ中のジュエルを描画
     */
    private void renderDraggingJewel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // マウス位置にジュエルアイテムを描画
        guiGraphics.renderItem(draggingJewel, mouseX - 8, mouseY - 8);

        // ジュエル名を表示
        JewelData data = JewelItem.getJewelData(draggingJewel);
        if (data != null) {
            String name = draggingJewel.getHoverName().getString();
            int nameWidth = this.font.width(name);

            // 背景
            guiGraphics.fill(mouseX - nameWidth / 2 - 2, mouseY + 12,
                    mouseX + nameWidth / 2 + 2, mouseY + 24, 0xCC000000);

            // テキスト
            guiGraphics.drawCenteredString(this.font, name, mouseX, mouseY + 14, data.rarity().getColor());
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
        hoveredSocket = null;

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

        // ジュエルソケットの特別な描画
        if (node.type() == SkillNodeType.JEWEL_SOCKET) {
            renderJewelSocket(guiGraphics, node, x, y, left, top, right, bottom, size, allocation, isAllocated, mouseX, mouseY);
            return;
        }

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
     * ジュエルソケットを描画
     */
    private void renderJewelSocket(GuiGraphics guiGraphics, SkillNode node, int x, int y,
                                   int left, int top, int right, int bottom, int size,
                                   SkillAllocation allocation, boolean isAllocated,
                                   int mouseX, int mouseY) {
        // ジュエルが装着されているか確認
        Optional<JewelData> equippedJewelOpt = this.menu.getEquippedJewelData(node.id());

        // 背景色（ソケットの状態によって変化）
        int bgColor;
        if (isAllocated) {
            if (equippedJewelOpt.isPresent()) {
                // ジュエル装着済み: ジュエルのレアリティ色
                bgColor = equippedJewelOpt.get().rarity().getColor();
            } else {
                // 解放済み・空き: 暗い紫（装着可能を示す）
                bgColor = 0x442255;
            }
        } else {
            // 未解放: 暗いグレー
            bgColor = 0x222222;
        }

        // ソケット形状（六角形風の八角形）
        guiGraphics.fill(left + 2, top, right - 2, bottom, bgColor | 0xFF000000);
        guiGraphics.fill(left, top + 2, right, bottom - 2, bgColor | 0xFF000000);

        // 枠
        int borderColor;
        if (isAllocated) {
            borderColor = equippedJewelOpt.isPresent()
                    ? equippedJewelOpt.get().rarity().getColor() | 0xFF000000
                    : 0xFFAA55FF; // 空きソケット：紫の枠
        } else {
            borderColor = 0xFF444444;
        }

        // 枠を描画（八角形風）
        guiGraphics.fill(left + 2, top, right - 2, top + 1, borderColor);      // 上
        guiGraphics.fill(left + 2, bottom - 1, right - 2, bottom, borderColor); // 下
        guiGraphics.fill(left, top + 2, left + 1, bottom - 2, borderColor);     // 左
        guiGraphics.fill(right - 1, top + 2, right, bottom - 2, borderColor);   // 右

        // ホバーチェック
        if (mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom) {
            hoveredNode = node;
            hoveredSocket = node;
            guiGraphics.fill(left, top, right, bottom, 0x44FFFFFF);

            // ジュエルをドラッグ中で、このソケットにドロップ可能な場合はハイライト
            if (!draggingJewel.isEmpty() && isAllocated && equippedJewelOpt.isEmpty()) {
                guiGraphics.fill(left, top, right, bottom, 0x4455FF55);
            }
        }

        // ソケット内のシンボル/ジュエル名
        if (size >= 16) {
            if (equippedJewelOpt.isPresent()) {
                // ジュエルのシンボル
                String symbol = "◆";
                guiGraphics.drawCenteredString(this.font, symbol, x, y - 4,
                        equippedJewelOpt.get().rarity().getColor());
            } else if (isAllocated) {
                // 空きソケット
                guiGraphics.drawCenteredString(this.font, "◇", x, y - 4, 0xAA55FF);
            } else {
                // 未解放ソケット
                guiGraphics.drawCenteredString(this.font, "◈", x, y - 4, 0x666666);
            }
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

        // ジュエルソケットの場合
        if (node.type() == SkillNodeType.JEWEL_SOCKET) {
            renderJewelSocketTooltip(tooltip, node);
        } else {
            // 通常ノードの効果
            if (!node.effects().isEmpty()) {
                tooltip.add(Component.literal("効果:").withStyle(s -> s.withColor(0x55FF55)));
                for (SkillEffect effect : node.effects()) {
                    String effectText = "  " + effect.getDisplayString();
                    int effectColor = effect.isPositive() ? 0x55FF55 : 0xFF5555;
                    tooltip.add(Component.literal(effectText).withStyle(s -> s.withColor(effectColor)));
                }
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

    /**
     * ジュエルソケットのツールチップ情報を追加
     */
    private void renderJewelSocketTooltip(List<Component> tooltip, SkillNode node) {
        SkillAllocation allocation = this.menu.getEditingAllocation();

        if (!allocation.isAllocated(node.id())) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("◈ ジュエルソケット").withStyle(s -> s.withColor(0xAA55FF)));
            tooltip.add(Component.literal("解放するとジュエルを装着可能").withStyle(s -> s.withColor(0x888888)));
            return;
        }

        tooltip.add(Component.literal(""));

        Optional<JewelData> equippedJewelOpt = this.menu.getEquippedJewelData(node.id());
        if (equippedJewelOpt.isPresent()) {
            JewelData jewel = equippedJewelOpt.get();

            // 装着済みジュエル名
            tooltip.add(Component.literal("◆ 装着済み: ").withStyle(s -> s.withColor(0xAA55FF))
                    .append(Component.translatable(jewel.nameKey()).withStyle(s -> s.withColor(jewel.rarity().getColor()))));

            // レアリティ
            tooltip.add(Component.literal("  レアリティ: " + jewel.rarity().getId().toUpperCase())
                    .withStyle(s -> s.withColor(jewel.rarity().getColor())));

            // 効果一覧
            tooltip.add(Component.literal("  効果:").withStyle(s -> s.withColor(0x888888)));
            for (SkillEffect effect : jewel.getScaledEffects()) {
                String effectText = "    " + effect.getDisplayString();
                int effectColor = effect.isPositive() ? 0x55FF55 : 0xFF5555;
                tooltip.add(Component.literal(effectText).withStyle(s -> s.withColor(effectColor)));
            }

            // 操作説明
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("クリックで取り外し").withStyle(s -> s.withColor(0xFFFF00)));
        } else {
            // 空きソケット
            tooltip.add(Component.literal("◇ 空きソケット").withStyle(s -> s.withColor(0xAA55FF)));
            tooltip.add(Component.literal("ジュエルをドラッグ＆ドロップで装着").withStyle(s -> s.withColor(0x888888)));
        }
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

        // ジュエルをドラッグ中の場合、ドロップ処理
        if (!draggingJewel.isEmpty() && button == 0) {
            return handleJewelDrop(mx, my);
        }

        // プレイヤーインベントリからジュエルをピックアップ
        if (button == 0) {
            int slotIndex = getSlotIndexAt(mx, my);
            if (slotIndex >= 0) {
                ItemStack slotStack = this.menu.getSlot(slotIndex).getItem();
                if (slotStack.getItem() instanceof JewelItem && !slotStack.isEmpty()) {
                    // ジュエルをドラッグ開始
                    draggingJewel = slotStack.copy();
                    draggingJewel.setCount(1);
                    draggingSourceSlot = slotIndex;
                    ANVIL.LOGGER.info("ジュエルをピックアップ: {}", JewelItem.getJewelId(draggingJewel));
                    return true;
                }
            }
        }

        // ツリーエリア内かチェック
        int treeX = this.leftPos + TREE_AREA_X;
        int treeY = this.topPos + TREE_AREA_Y;

        if (mx >= treeX && mx < treeX + TREE_AREA_WIDTH &&
                my >= treeY && my < treeY + TREE_AREA_HEIGHT) {

            if (button == 0) { // 左クリック
                if (hoveredNode != null) {
                    // ジュエルソケットの場合、装着済みジュエルの取り外しをチェック
                    if (hoveredNode.type() == SkillNodeType.JEWEL_SOCKET) {
                        SkillAllocation allocation = this.menu.getEditingAllocation();
                        if (allocation.isAllocated(hoveredNode.id()) && allocation.hasJewel(hoveredNode.id())) {
                            // ジュエルを取り外してドラッグ開始
                            Optional<ResourceLocation> removedJewelId = this.menu.unequipJewel(hoveredNode.id());
                            if (removedJewelId.isPresent()) {
                                draggingJewel = JewelItem.createJewelStack(removedJewelId.get());
                                draggingSourceSlot = -1; // ソケットから取り外し
                                ANVIL.LOGGER.info("ジュエルをソケットから取り外し: {}", removedJewelId.get());
                                return true;
                            }
                        }
                    }

                    // 通常のノード解放
                    if (this.menu.allocateNode(hoveredNode.id())) {
                        ANVIL.LOGGER.info("ノードを解放: {}", hoveredNode.id());
                    }
                    return true;
                } else {
                    // ドラッグ開始（ビューパン）
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

    /**
     * ジュエルドロップ処理
     */
    private boolean handleJewelDrop(int mx, int my) {
        // ツリーエリア内でソケットにドロップ
        int treeX = this.leftPos + TREE_AREA_X;
        int treeY = this.topPos + TREE_AREA_Y;

        if (mx >= treeX && mx < treeX + TREE_AREA_WIDTH &&
                my >= treeY && my < treeY + TREE_AREA_HEIGHT) {

            if (hoveredSocket != null) {
                SkillAllocation allocation = this.menu.getEditingAllocation();
                // ソケットが解放済みで空いている場合
                if (allocation.isAllocated(hoveredSocket.id()) && !allocation.hasJewel(hoveredSocket.id())) {
                    // ジュエルを装着
                    if (this.menu.equipJewel(hoveredSocket.id(), draggingJewel)) {
                        ANVIL.LOGGER.info("ジュエルを装着: {} -> {}", JewelItem.getJewelId(draggingJewel), hoveredSocket.id());

                        // 元のスロットからジュエルを消費
                        if (draggingSourceSlot >= 0) {
                            ItemStack sourceStack = this.menu.getSlot(draggingSourceSlot).getItem();
                            sourceStack.shrink(1);
                        }

                        draggingJewel = ItemStack.EMPTY;
                        draggingSourceSlot = -1;
                        return true;
                    }
                }
            }
        }

        // インベントリスロットにドロップ（キャンセル）
        int slotIndex = getSlotIndexAt(mx, my);
        if (slotIndex >= 0 || draggingSourceSlot < 0) {
            // ドロップ先のスロットにジュエルを戻す（またはソケットから取り外したジュエルをインベントリに追加）
            if (draggingSourceSlot < 0) {
                // ソケットから取り外したジュエル：プレイヤーに与える
                if (this.minecraft != null && this.minecraft.player != null) {
                    if (!this.minecraft.player.getInventory().add(draggingJewel)) {
                        // インベントリがいっぱいの場合はドロップ
                        this.minecraft.player.drop(draggingJewel, false);
                    }
                }
            }
            // draggingSourceSlot >= 0 の場合は元のスロットに残っているのでキャンセル
        }

        draggingJewel = ItemStack.EMPTY;
        draggingSourceSlot = -1;
        return true;
    }

    /**
     * 座標からスロットインデックスを取得
     */
    private int getSlotIndexAt(int mx, int my) {
        for (int i = 0; i < this.menu.slots.size(); i++) {
            var slot = this.menu.getSlot(i);
            int slotX = this.leftPos + slot.x;
            int slotY = this.topPos + slot.y;
            if (mx >= slotX && mx < slotX + 16 && my >= slotY && my < slotY + 16) {
                return i;
            }
        }
        return -1;
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
