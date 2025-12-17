package io.github.nazottix.anvil.client.screen;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.client.ui.AnvilButtonRenderer;
import io.github.nazottix.anvil.client.ui.AnvilColors;
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
        // GUI のサイズを設定（縦を拡大してツールボタン用スペース確保 + Inventoryラベルとの間にスペース確保）
        this.imageWidth = 176;
        this.imageHeight = 234;
        // インベントリラベルの位置調整（14ピクセル下にずらした）
        this.inventoryLabelY = 140;
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

        // 背景色で塗りつぶし
        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight,
                AnvilColors.VOID_BLACK | 0xFF000000);

        // パネル背景
        guiGraphics.fill(x + 2, y + 2, x + this.imageWidth - 2, y + this.imageHeight - 2,
                AnvilColors.ANVIL_STEEL | 0xFF000000);

        // ツールタイプ選択ボタンを描画
        renderToolTypeButtons(guiGraphics, x, y, mouseX, mouseY);

        // 区切り線（ツールボタンとスロットの間）
        guiGraphics.fill(x + 8, y + 64, x + this.imageWidth - 8, y + 65, 0xFF444444);

        // スロットエリア背景
        renderSlotAreas(guiGraphics, x, y);

        // Craftボタンを描画
        renderCraftButton(guiGraphics, x, y, mouseX, mouseY);

        // 矢印描画（パーツ → 出力）
        renderArrow(guiGraphics, x + 93, y + 96);

        // 区切り線（スロットとインベントリの間）
        guiGraphics.fill(x + 8, y + 120, x + this.imageWidth - 8, y + 121, 0xFF444444);

        // プレイヤーインベントリ境界線とスロット背景を描画
        renderInventoryBackground(guiGraphics, x, y);
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

        // パーツスロットラベル（選択中のツールタイプに応じて表示）
        ToolType selectedTool = this.menu.getSelectedToolType();
        String[] slotLabels = getPartSlotLabels(selectedTool);
        for (int i = 0; i < PART_SLOT_COUNT; i++) {
            String label = i < slotLabels.length ? slotLabels[i] : "";
            guiGraphics.drawString(this.font, label, PART_SLOT_X + i * PART_SLOT_SPACING + 4, PART_SLOT_Y - 9, 0x888888, false);
        }

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
     * スロットエリアを描画
     */
    private void renderSlotAreas(GuiGraphics guiGraphics, int guiX, int guiY) {
        // パーツスロット背景（4スロット対応）
        for (int i = 0; i < PART_SLOT_COUNT; i++) {
            int slotX = guiX + PART_SLOT_X + i * PART_SLOT_SPACING;
            int slotY = guiY + PART_SLOT_Y;
            guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, 0xFF3A3A3A);
            guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, AnvilColors.VOID_BLACK | 0xFF000000);
        }

        // 出力スロット背景（やや大きめ）
        guiGraphics.fill(guiX + OUTPUT_SLOT_X - 2, guiY + OUTPUT_SLOT_Y - 2,
                guiX + OUTPUT_SLOT_X + 18, guiY + OUTPUT_SLOT_Y + 18, AnvilColors.FORGE_ORANGE | 0xFF000000);
        guiGraphics.fill(guiX + OUTPUT_SLOT_X, guiY + OUTPUT_SLOT_Y,
                guiX + OUTPUT_SLOT_X + 16, guiY + OUTPUT_SLOT_Y + 16, AnvilColors.VOID_BLACK | 0xFF000000);

        // ツール入力スロットはRepairStationに移動
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
     * ツールタイプに応じたパーツスロットラベルを取得
     */
    private String[] getPartSlotLabels(ToolType type) {
        if (type == null) {
            return new String[]{"1", "2", "3", "4"};
        }
        return switch (type) {
            case PICKAXE, AXE -> new String[]{"H", "G", "B", ""};     // ヘッド、ハンドル、バインディング
            case SHOVEL, HOE -> new String[]{"H", "G", "", ""};       // ヘッド、ハンドル
            case SWORD -> new String[]{"B", "G", "D", ""};            // ブレード、ハンドル、ガード
            case BOW -> new String[]{"L", "G", "L", "S"};             // ボウリム、ハンドル、ボウリム、ストリング
            case FISHING_ROD -> new String[]{"R", "H", "L", ""};      // ロッド、フック、ライン
            case SHEARS -> new String[]{"B", "B", "P", ""};           // ブレード×2、ピボット
        };
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
