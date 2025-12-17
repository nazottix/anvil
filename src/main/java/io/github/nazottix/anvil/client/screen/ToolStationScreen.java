package io.github.nazottix.anvil.client.screen;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.client.ui.AnvilButtonRenderer;
import io.github.nazottix.anvil.client.ui.AnvilColors;
import io.github.nazottix.anvil.client.ui.AnvilPanelRenderer;
import io.github.nazottix.anvil.menu.ToolStationMenu;
import io.github.nazottix.anvil.tool.ToolType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

/**
 * ツールステーションスクリーン
 *
 * ツール作成画面のクライアント側描画を行います。
 * ツールタイプ選択、パーツ配置、作成ボタンを提供。
 *
 * レイアウト:
 * - 上部: ツールタイプ選択ボタン（4x2）
 * - 中央: パーツスロット + 作成ボタン + 出力スロット
 * - 下部: 修理スロット + インベントリ
 *
 * 仕様書参照: docs/10_UI_UXデザイン.md
 */
public class ToolStationScreen extends AbstractContainerScreen<ToolStationMenu> {

    // UI定数 - ツールタイプボタン
    private static final int TOOL_BUTTON_SIZE = 20;
    private static final int TOOL_BUTTON_SPACING = 2;
    private static final int TOOL_BUTTONS_X = 8;
    private static final int TOOL_BUTTONS_Y = 18;

    // パーツスロット位置（Menuと同期 - 4スロット対応）
    private static final int PART_SLOT_X = 21;
    private static final int PART_SLOT_Y = 70;
    private static final int PART_SLOT_SPACING = 18;
    private static final int PART_SLOT_COUNT = 4;

    // 出力スロット位置（Menuと同期）
    private static final int OUTPUT_SLOT_X = 124;
    private static final int OUTPUT_SLOT_Y = 78;

    // ツール入力スロットはRepairStationScreenに移動

    // ホバー中のツールタイプ
    private ToolType hoveredToolType = null;

    // Craftボタン
    private static final int CRAFT_BUTTON_X = 93;
    private static final int CRAFT_BUTTON_Y = 78;
    private static final int CRAFT_BUTTON_WIDTH = 26;
    private static final int CRAFT_BUTTON_HEIGHT = 16;

    /**
     * コンストラクタ
     */
    public ToolStationScreen(ToolStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // GUI のサイズを設定（他のStationと同じサイズ）
        this.imageWidth = 176;
        this.imageHeight = 200;
        // インベントリラベルの位置調整
        this.inventoryLabelY = 106;
    }

    @Override
    protected void init() {
        super.init();
        // Craftボタンはカスタム描画で実装（initでウィジェット追加しない）
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

        // ツールタイプ選択ボタンを描画
        renderToolTypeButtons(guiGraphics, x, y, mouseX, mouseY);


        // スロットエリア背景
        renderSlotAreas(guiGraphics, x, y);

        // Craftボタンを描画
        renderCraftButton(guiGraphics, x, y, mouseX, mouseY);

        // 矢印描画（パーツ → 出力）
        renderArrow(guiGraphics, x + 93, y + 96);


        // プレイヤーインベントリ境界線とスロット背景を描画（マイクラ標準風）
        AnvilPanelRenderer.renderInventorySlots(guiGraphics, x, y, 117, 175);
    }

    /**
     * プレイヤーインベントリ背景とスロット境界線を描画
     */
    private void renderInventoryBackground(GuiGraphics guiGraphics, int guiX, int guiY) {
        // インベントリスロット（3行9列）- y=151から（14ピクセル下にずらした）
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotX = guiX + 8 + col * 18;
                int slotY = guiY + 151 + row * 18;
                // スロット枠
                guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, 0xFF373737);
                // スロット背景
                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF8B8B8B);
                // スロット内側の影
                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 1, 0xFF555555);
                guiGraphics.fill(slotX, slotY, slotX + 1, slotY + 16, 0xFF555555);
            }
        }

        // ホットバー（1行9列）- y=209（14ピクセル下にずらした）
        for (int col = 0; col < 9; col++) {
            int slotX = guiX + 8 + col * 18;
            int slotY = guiY + 209;
            // スロット枠
            guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, 0xFF373737);
            // スロット背景
            guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF8B8B8B);
            // スロット内側の影
            guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 1, 0xFF555555);
            guiGraphics.fill(slotX, slotY, slotX + 1, slotY + 16, 0xFF555555);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // ツールタイプツールチップ
        if (hoveredToolType != null) {
            renderToolTypeTooltip(guiGraphics, mouseX, mouseY, hoveredToolType);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // タイトル
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY,
                AnvilColors.ETHER_WHITE, false);

        // インベントリラベル
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY,
                AnvilColors.ETHER_WHITE, false);

        // 選択されたツールタイプを表示（タイトル横、ローカライズ対応）
        ToolType selectedType = this.menu.getSelectedToolType();
        if (selectedType != null) {
            Component typeName = Component.translatable(selectedType.getTranslationKey());
            int color = getToolTypeColor(selectedType);
            guiGraphics.drawString(this.font, typeName, 100, 6, color, false);
        }

        // パーツスロットラベルはrenderBgでアイコンとして描画（renderLabelsでは描画しない）

        // 修理ラベルはRepairStationScreenに移動
    }

    /**
     * ツールタイプ選択ボタンを描画
     * AnvilButtonRendererユーティリティを使用してテーマに統一
     */
    private void renderToolTypeButtons(GuiGraphics guiGraphics, int guiX, int guiY, int mouseX, int mouseY) {
        ToolType[] types = ToolType.values();
        ToolType selectedType = this.menu.getSelectedToolType();
        hoveredToolType = null;

        for (int i = 0; i < types.length; i++) {
            int row = i / 4;
            int col = i % 4;
            int btnX = guiX + TOOL_BUTTONS_X + col * (TOOL_BUTTON_SIZE + TOOL_BUTTON_SPACING);
            int btnY = guiY + TOOL_BUTTONS_Y + row * (TOOL_BUTTON_SIZE + TOOL_BUTTON_SPACING);

            ToolType type = types[i];
            boolean isSelected = type == selectedType;
            boolean isHovered = AnvilButtonRenderer.isMouseOverButton(mouseX, mouseY, btnX, btnY, TOOL_BUTTON_SIZE, TOOL_BUTTON_SIZE);

            if (isHovered) {
                hoveredToolType = type;
            }

            // AnvilButtonRendererを使用してアイコンボタンを描画
            String symbol = getToolTypeSymbol(type);
            int symbolColor = getToolTypeColor(type);
            AnvilButtonRenderer.renderIconButton(guiGraphics, this.font, btnX, btnY, TOOL_BUTTON_SIZE, symbol, symbolColor, isSelected, isHovered);
        }
    }

    /**
     * スロットエリアを描画（マイクラ標準風の立体的スロット）
     */
    private void renderSlotAreas(GuiGraphics guiGraphics, int guiX, int guiY) {
        // 選択中のツールタイプを取得
        ToolType selectedTool = this.menu.getSelectedToolType();
        java.util.List<io.github.nazottix.anvil.tool.PartType> requiredParts =
                selectedTool != null ? selectedTool.getRequiredParts() : java.util.Collections.emptyList();

        // パーツスロット背景（4スロット対応）- マイクラ標準風スロット
        for (int i = 0; i < PART_SLOT_COUNT; i++) {
            int slotX = guiX + PART_SLOT_X + i * PART_SLOT_SPACING;
            int slotY = guiY + PART_SLOT_Y;
            AnvilPanelRenderer.renderSlot(guiGraphics, slotX, slotY);

            // スロットが空で、必要なパーツタイプがある場合はアイコンを描画
            if (this.menu.getSlot(i).getItem().isEmpty() && i < requiredParts.size()) {
                renderPartTypeIcon(guiGraphics, slotX, slotY, requiredParts.get(i));
            }
        }

        // 出力スロット背景（やや大きめ、オレンジ枠で強調）
        // オレンジ枠
        guiGraphics.fill(guiX + OUTPUT_SLOT_X - 3, guiY + OUTPUT_SLOT_Y - 3,
                guiX + OUTPUT_SLOT_X + 19, guiY + OUTPUT_SLOT_Y + 19, AnvilColors.FORGE_ORANGE | 0xFF000000);
        // 内側に標準スロットを描画
        AnvilPanelRenderer.renderSlot(guiGraphics, guiX + OUTPUT_SLOT_X, guiY + OUTPUT_SLOT_Y);

        // ツール入力スロットはRepairStationに移動
    }

    /**
     * パーツタイプに応じたアイコンを描画（スロット内に表示）
     * スロットが空の場合に、どのパーツを入れるべきかを示す
     */
    private void renderPartTypeIcon(GuiGraphics guiGraphics, int slotX, int slotY, io.github.nazottix.anvil.tool.PartType partType) {
        // アイコン描画色（半透明グレー）
        int iconColor = 0x55FFFFFF;
        // スロット中央座標
        int centerX = slotX + 8;
        int centerY = slotY + 8;

        switch (partType) {
            case HEAD -> {
                // ヘッド: 上向き三角形（ツールの先端を表現）
                guiGraphics.fill(centerX - 1, centerY - 5, centerX + 2, centerY - 4, iconColor);
                guiGraphics.fill(centerX - 2, centerY - 4, centerX + 3, centerY - 3, iconColor);
                guiGraphics.fill(centerX - 3, centerY - 3, centerX + 4, centerY - 2, iconColor);
                guiGraphics.fill(centerX - 4, centerY - 2, centerX + 5, centerY + 1, iconColor);
                guiGraphics.fill(centerX - 2, centerY + 1, centerX + 3, centerY + 5, iconColor);
            }
            case HANDLE -> {
                // ハンドル: 縦棒（持ち手を表現）
                guiGraphics.fill(centerX - 1, centerY - 5, centerX + 2, centerY + 5, iconColor);
                // 握り部分のアクセント
                guiGraphics.fill(centerX - 2, centerY - 2, centerX + 3, centerY + 2, iconColor);
            }
            case BINDING -> {
                // バインディング: 円形（結合部を表現）
                guiGraphics.fill(centerX - 2, centerY - 4, centerX + 3, centerY - 3, iconColor);
                guiGraphics.fill(centerX - 4, centerY - 3, centerX - 2, centerY + 3, iconColor);
                guiGraphics.fill(centerX + 2, centerY - 3, centerX + 4, centerY + 3, iconColor);
                guiGraphics.fill(centerX - 2, centerY + 3, centerX + 3, centerY + 4, iconColor);
            }
            case BLADE -> {
                // ブレード: 細長い剣の形
                guiGraphics.fill(centerX - 1, centerY - 6, centerX + 2, centerY + 3, iconColor);
                guiGraphics.fill(centerX, centerY - 7, centerX + 1, centerY - 6, iconColor);
                guiGraphics.fill(centerX - 2, centerY + 3, centerX + 3, centerY + 5, iconColor);
            }
            case GUARD -> {
                // ガード: 横棒（鍔を表現）
                guiGraphics.fill(centerX - 5, centerY - 1, centerX + 6, centerY + 2, iconColor);
                guiGraphics.fill(centerX - 1, centerY - 3, centerX + 2, centerY + 4, iconColor);
            }
            case BOW_LIMB -> {
                // ボウリム: 弓の曲線部分
                guiGraphics.fill(centerX - 4, centerY - 5, centerX - 3, centerY + 5, iconColor);
                guiGraphics.fill(centerX - 3, centerY - 6, centerX - 2, centerY - 5, iconColor);
                guiGraphics.fill(centerX - 3, centerY + 5, centerX - 2, centerY + 6, iconColor);
                guiGraphics.fill(centerX - 2, centerY - 7, centerX, centerY - 6, iconColor);
                guiGraphics.fill(centerX - 2, centerY + 6, centerX, centerY + 7, iconColor);
            }
            case BOWSTRING -> {
                // ボウストリング: 縦線（弦を表現）
                guiGraphics.fill(centerX, centerY - 6, centerX + 1, centerY + 6, iconColor);
            }
            case ROD -> {
                // ロッド: 斜めの棒（釣り竿を表現）
                guiGraphics.fill(centerX - 4, centerY + 4, centerX + 5, centerY + 5, iconColor);
                guiGraphics.fill(centerX + 3, centerY - 4, centerX + 5, centerY + 5, iconColor);
            }
            case HOOK -> {
                // フック: Jの字型（釣り針を表現）
                guiGraphics.fill(centerX, centerY - 4, centerX + 1, centerY + 2, iconColor);
                guiGraphics.fill(centerX - 3, centerY + 2, centerX + 1, centerY + 3, iconColor);
                guiGraphics.fill(centerX - 4, centerY, centerX - 3, centerY + 3, iconColor);
                guiGraphics.fill(centerX - 3, centerY - 1, centerX - 2, centerY + 1, iconColor);
            }
            case LINE -> {
                // ライン: 縦の波線（釣り糸を表現）
                guiGraphics.fill(centerX - 1, centerY - 6, centerX, centerY - 4, iconColor);
                guiGraphics.fill(centerX, centerY - 4, centerX + 1, centerY - 2, iconColor);
                guiGraphics.fill(centerX - 1, centerY - 2, centerX, centerY, iconColor);
                guiGraphics.fill(centerX, centerY, centerX + 1, centerY + 2, iconColor);
                guiGraphics.fill(centerX - 1, centerY + 2, centerX, centerY + 4, iconColor);
                guiGraphics.fill(centerX, centerY + 4, centerX + 1, centerY + 6, iconColor);
            }
            case PIVOT -> {
                // ピボット: 中央の丸（ハサミの軸を表現）
                guiGraphics.fill(centerX - 2, centerY - 1, centerX + 3, centerY + 2, iconColor);
                guiGraphics.fill(centerX - 1, centerY - 2, centerX + 2, centerY + 3, iconColor);
            }
            default -> {
                // デフォルト: 小さな四角
                guiGraphics.fill(centerX - 3, centerY - 3, centerX + 4, centerY + 4, iconColor);
            }
        }
    }

    /**
     * Craftボタンを描画
     * AnvilButtonRendererユーティリティを使用してテーマに統一
     */
    private void renderCraftButton(GuiGraphics guiGraphics, int guiX, int guiY, int mouseX, int mouseY) {
        int btnX = guiX + CRAFT_BUTTON_X;
        int btnY = guiY + CRAFT_BUTTON_Y;

        // AnvilButtonRendererを使用してボタンを描画
        boolean isHovered = AnvilButtonRenderer.isMouseOverButton(mouseX, mouseY, btnX, btnY, CRAFT_BUTTON_WIDTH, CRAFT_BUTTON_HEIGHT);
        Component craftText = Component.translatable("gui.anvil.craft");
        AnvilButtonRenderer.renderButton(guiGraphics, this.font, btnX, btnY, CRAFT_BUTTON_WIDTH, CRAFT_BUTTON_HEIGHT, craftText, isHovered, true);
    }

    /**
     * 矢印を描画
     */
    private void renderArrow(GuiGraphics guiGraphics, int x, int y) {
        // 簡易矢印
        guiGraphics.fill(x, y, x + 20, y + 2, 0xFFAAAAAA);
        guiGraphics.fill(x + 14, y - 4, x + 20, y + 8, 0xFFAAAAAA);
    }

    /**
     * ツールタイプのツールチップを描画（ローカライズ対応）
     */
    private void renderToolTypeTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, ToolType type) {
        java.util.List<Component> tooltip = new java.util.ArrayList<>();

        // ツールタイプ名（ローカライズ）
        tooltip.add(Component.translatable(type.getTranslationKey())
                .withStyle(s -> s.withColor(getToolTypeColor(type))));

        // ツールタイプ説明（ローカライズ）
        tooltip.add(Component.translatable("gui.anvil.tool_station.desc." + type.getId())
                .withStyle(s -> s.withColor(0xAAAAAA)));

        // 必要パーツ（ローカライズ）
        tooltip.add(Component.translatable("gui.anvil.tool_station.required_parts")
                .withStyle(s -> s.withColor(0xFFFF00)));
        for (var partType : type.getRequiredParts()) {
            // パーツ名をローカライズキーから取得
            tooltip.add(Component.literal("  - ")
                    .append(Component.translatable("part.anvil." + partType.getId()))
                    .withStyle(s -> s.withColor(0x888888)));
        }

        guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
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
            // Craftボタンのクリック判定
            int craftBtnX = guiX + CRAFT_BUTTON_X;
            int craftBtnY = guiY + CRAFT_BUTTON_Y;
            if (AnvilButtonRenderer.isMouseOverButton((int) mouseX, (int) mouseY, craftBtnX, craftBtnY, CRAFT_BUTTON_WIDTH, CRAFT_BUTTON_HEIGHT)) {
                // サーバーにボタンクリックを送信（ボタンID 0 = クラフト）
                if (mc.gameMode != null) {
                    mc.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
                }
                // AnvilButtonRendererを使用してクラフト音を再生
                AnvilButtonRenderer.playButtonSound(AnvilButtonRenderer.ButtonSound.ACTION);
                ANVIL.LOGGER.info("ツール作成を実行");
                return true;
            }

            // ツールタイプボタンのクリック判定
            ToolType[] types = ToolType.values();
            for (int i = 0; i < types.length; i++) {
                int row = i / 4;
                int col = i % 4;
                int btnX = guiX + TOOL_BUTTONS_X + col * (TOOL_BUTTON_SIZE + TOOL_BUTTON_SPACING);
                int btnY = guiY + TOOL_BUTTONS_Y + row * (TOOL_BUTTON_SIZE + TOOL_BUTTON_SPACING);

                if (AnvilButtonRenderer.isMouseOverButton((int) mouseX, (int) mouseY, btnX, btnY, TOOL_BUTTON_SIZE, TOOL_BUTTON_SIZE)) {
                    this.menu.setSelectedToolType(i);
                    // AnvilButtonRendererを使用して選択音を再生
                    AnvilButtonRenderer.playButtonSound(AnvilButtonRenderer.ButtonSound.SELECT);
                    ANVIL.LOGGER.info("ツールタイプを選択: {}", types[i].getId());
                    return true;
                }
            }
        }

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

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * マウスが指定領域内かチェック
     */
    private boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    /**
     * ツールタイプのシンボルを取得
     */
    private String getToolTypeSymbol(ToolType type) {
        return switch (type) {
            case PICKAXE -> "P";
            case AXE -> "A";
            case SHOVEL -> "S";
            case SWORD -> "W";
            case HOE -> "H";
            case BOW -> "B";
            case FISHING_ROD -> "F";
            case SHEARS -> "C";
        };
    }

    /**
     * ツールタイプの色を取得
     */
    private int getToolTypeColor(ToolType type) {
        return switch (type) {
            case PICKAXE -> AnvilColors.FORGE_ORANGE;
            case AXE -> 0x8B4513;        // 茶色
            case SHOVEL -> 0x808080;      // 灰色
            case SWORD -> 0xFF4444;       // 赤
            case HOE -> 0x228B22;         // 緑
            case BOW -> 0x8B008B;         // 紫
            case FISHING_ROD -> 0x4169E1; // 青
            case SHEARS -> 0xC0C0C0;      // 銀
        };
    }
}
