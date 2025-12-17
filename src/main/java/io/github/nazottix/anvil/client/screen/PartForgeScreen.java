package io.github.nazottix.anvil.client.screen;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.client.ui.AnvilButtonRenderer;
import io.github.nazottix.anvil.client.ui.AnvilColors;
import io.github.nazottix.anvil.client.ui.AnvilPanelRenderer;
import io.github.nazottix.anvil.menu.PartForgeMenu;
import io.github.nazottix.anvil.network.PartForgeActionPacket;
import io.github.nazottix.anvil.tool.PartType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * パーツ鍛造所スクリーン
 *
 * パーツ鍛造所のクライアント側UI描画を担当します。
 * パーツタイプの選択ドロップダウンと鋳造ボタンを表示します。
 * Station Blockスタイルに統一されたUIデザイン。
 */
public class PartForgeScreen extends AbstractContainerScreen<PartForgeMenu> {

    // パーツタイプ一覧
    private static final PartType[] PART_TYPES = PartType.values();

    // ドロップダウン設定
    private static final int DROPDOWN_X = 8;
    private static final int DROPDOWN_Y = 18;
    private static final int DROPDOWN_WIDTH = 80;
    private static final int DROPDOWN_CLOSED_HEIGHT = 16;
    private static final int DROPDOWN_ENTRY_HEIGHT = 14;
    private static final int DROPDOWN_MAX_VISIBLE = 6;

    // スロット位置（Menuと同期）
    private static final int INPUT_SLOT_X = 27;
    private static final int INPUT_SLOT_Y = 70;
    private static final int OUTPUT_SLOT_X = 134;
    private static final int OUTPUT_SLOT_Y = 70;

    // 鋳造ボタン
    private static final int FORGE_BUTTON_X = 93;
    private static final int FORGE_BUTTON_Y = 70;
    private static final int FORGE_BUTTON_WIDTH = 30;
    private static final int FORGE_BUTTON_HEIGHT = 16;

    // ドロップダウン状態
    private boolean dropdownOpen = false;
    private int scrollOffset = 0;

    /**
     * コンストラクタ
     *
     * GUIサイズをStation Blockスタイル（200）に設定
     */
    public PartForgeScreen(PartForgeMenu menu, Inventory playerInventory, Component title) {
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
        // ドロップダウンは閉じた状態から開始
        this.dropdownOpen = false;
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

        // 出力スロット背景（オレンジ枠で強調、Station Blockスタイル）
        guiGraphics.fill(x + OUTPUT_SLOT_X - 3, y + OUTPUT_SLOT_Y - 3,
                x + OUTPUT_SLOT_X + 19, y + OUTPUT_SLOT_Y + 19, AnvilColors.FORGE_ORANGE | 0xFF000000);
        AnvilPanelRenderer.renderSlot(guiGraphics, x + OUTPUT_SLOT_X, y + OUTPUT_SLOT_Y);

        // 鋳造ボタンを描画
        renderForgeButton(guiGraphics, x, y, mouseX, mouseY);

        // 矢印描画（入力 → 出力）
        renderArrow(guiGraphics, x + 50, y + 74);

        // プレイヤーインベントリ境界線とスロット背景を描画（Station Blockスタイル）
        AnvilPanelRenderer.renderInventorySlots(guiGraphics, x, y, 117, 175);

        // パーツタイプドロップダウンを描画（最後に描画して最前面に）
        renderPartTypeDropdown(guiGraphics, x, y, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // タイトル（Station Blockスタイルの白色）
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY,
                AnvilColors.ETHER_WHITE, false);

        // インベントリラベル（Station Blockスタイルの白色）
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY,
                AnvilColors.ETHER_WHITE, false);
    }

    /**
     * パーツタイプドロップダウンを描画（クリックで開閉するスタイル）
     */
    private void renderPartTypeDropdown(GuiGraphics guiGraphics, int panelX, int panelY, int mouseX, int mouseY) {
        int dropdownX = panelX + DROPDOWN_X;
        int dropdownY = panelY + DROPDOWN_Y;

        // 選択中のパーツタイプ
        PartType selectedType = menu.getSelectedPartType();
        String selectedName = selectedType != null ?
                Component.translatable(selectedType.getTranslationKey()).getString() : "選択...";

        // ドロップダウンヘッダー（常に表示）
        renderDropdownHeader(guiGraphics, dropdownX, dropdownY, mouseX, mouseY, selectedName);

        // ドロップダウンが開いている場合、リストを描画
        if (dropdownOpen) {
            renderDropdownList(guiGraphics, dropdownX, dropdownY + DROPDOWN_CLOSED_HEIGHT, mouseX, mouseY);
        }
    }

    /**
     * ドロップダウンヘッダーを描画
     */
    private void renderDropdownHeader(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, String text) {
        boolean isHovered = mouseX >= x && mouseX < x + DROPDOWN_WIDTH &&
                mouseY >= y && mouseY < y + DROPDOWN_CLOSED_HEIGHT;

        // 背景
        int bgColor = dropdownOpen ? 0xFF2D3748 : (isHovered ? 0xFF3D4758 : 0xFF374151);
        guiGraphics.fill(x, y, x + DROPDOWN_WIDTH, y + DROPDOWN_CLOSED_HEIGHT, bgColor);

        // 枠（Station Blockスタイル）
        guiGraphics.fill(x, y, x + DROPDOWN_WIDTH, y + 1, 0xFF5B6B7F);
        guiGraphics.fill(x, y, x + 1, y + DROPDOWN_CLOSED_HEIGHT, 0xFF5B6B7F);
        guiGraphics.fill(x, y + DROPDOWN_CLOSED_HEIGHT - 1, x + DROPDOWN_WIDTH, y + DROPDOWN_CLOSED_HEIGHT, 0xFF1F2937);
        guiGraphics.fill(x + DROPDOWN_WIDTH - 1, y, x + DROPDOWN_WIDTH, y + DROPDOWN_CLOSED_HEIGHT, 0xFF1F2937);

        // テキスト
        guiGraphics.drawString(this.font, text, x + 4, y + 4, AnvilColors.ETHER_WHITE, false);

        // 矢印アイコン（開閉状態を示す）
        String arrow = dropdownOpen ? "▲" : "▼";
        guiGraphics.drawString(this.font, arrow, x + DROPDOWN_WIDTH - 12, y + 4, AnvilColors.ETHER_WHITE, false);
    }

    /**
     * ドロップダウンリストを描画
     */
    private void renderDropdownList(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        int visibleEntries = Math.min(DROPDOWN_MAX_VISIBLE, PART_TYPES.length);
        int listHeight = visibleEntries * DROPDOWN_ENTRY_HEIGHT;

        // リスト背景（半透明で他の要素の上に表示）
        guiGraphics.fill(x, y, x + DROPDOWN_WIDTH, y + listHeight, 0xFF1F2937);

        // 枠
        guiGraphics.fill(x, y, x + 1, y + listHeight, 0xFF5B6B7F);
        guiGraphics.fill(x + DROPDOWN_WIDTH - 1, y, x + DROPDOWN_WIDTH, y + listHeight, 0xFF1F2937);
        guiGraphics.fill(x, y + listHeight - 1, x + DROPDOWN_WIDTH, y + listHeight, 0xFF1F2937);

        // 選択中のパーツタイプ
        int selectedIndex = menu.getSelectedPartTypeIndex();

        // エントリを描画
        for (int i = 0; i < visibleEntries; i++) {
            int typeIndex = scrollOffset + i;
            if (typeIndex >= PART_TYPES.length) break;

            PartType type = PART_TYPES[typeIndex];
            int entryY = y + i * DROPDOWN_ENTRY_HEIGHT;

            // 選択中のエントリはハイライト（オレンジ系）
            if (typeIndex == selectedIndex) {
                guiGraphics.fill(x + 1, entryY, x + DROPDOWN_WIDTH - 1, entryY + DROPDOWN_ENTRY_HEIGHT,
                        AnvilColors.FORGE_ORANGE | 0xAA000000);
            }
            // マウスホバーハイライト
            else if (mouseX >= x && mouseX < x + DROPDOWN_WIDTH &&
                    mouseY >= entryY && mouseY < entryY + DROPDOWN_ENTRY_HEIGHT) {
                guiGraphics.fill(x + 1, entryY, x + DROPDOWN_WIDTH - 1, entryY + DROPDOWN_ENTRY_HEIGHT,
                        0x44FFFFFF);
            }

            // パーツ名を描画（ローカライズ対応）
            Component partName = Component.translatable(type.getTranslationKey());
            int textColor = (typeIndex == selectedIndex) ? 0xFFFFFFFF : AnvilColors.ETHER_WHITE;
            guiGraphics.drawString(this.font, partName, x + 4, entryY + 3, textColor, false);
        }

        // スクロールバー（必要な場合）
        if (PART_TYPES.length > DROPDOWN_MAX_VISIBLE) {
            int scrollBarX = x + DROPDOWN_WIDTH - 5;
            int scrollBarHeight = Math.max(10, listHeight * DROPDOWN_MAX_VISIBLE / PART_TYPES.length);
            int maxScroll = PART_TYPES.length - DROPDOWN_MAX_VISIBLE;
            int scrollBarY = y + (listHeight - scrollBarHeight) * scrollOffset / maxScroll;

            // スクロールバー背景
            guiGraphics.fill(scrollBarX, y, scrollBarX + 4, y + listHeight, 0xFF374151);
            // スクロールバーつまみ
            guiGraphics.fill(scrollBarX, scrollBarY, scrollBarX + 4, scrollBarY + scrollBarHeight, 0xFF5B6B7F);
        }
    }

    /**
     * 鋳造ボタンを描画（AnvilButtonRendererを使用したStation Blockスタイル）
     */
    private void renderForgeButton(GuiGraphics guiGraphics, int guiX, int guiY, int mouseX, int mouseY) {
        int btnX = guiX + FORGE_BUTTON_X;
        int btnY = guiY + FORGE_BUTTON_Y;

        // AnvilButtonRendererを使用してボタンを描画
        boolean isHovered = AnvilButtonRenderer.isMouseOverButton(mouseX, mouseY, btnX, btnY, FORGE_BUTTON_WIDTH, FORGE_BUTTON_HEIGHT);
        boolean isEnabled = menu.canForge();
        Component forgeText = Component.translatable("gui.anvil.part_forge.forge");
        AnvilButtonRenderer.renderButton(guiGraphics, this.font, btnX, btnY, FORGE_BUTTON_WIDTH, FORGE_BUTTON_HEIGHT, forgeText, isHovered, isEnabled);
    }

    /**
     * 矢印を描画（Station Blockスタイル）
     */
    private void renderArrow(GuiGraphics guiGraphics, int x, int y) {
        // 簡易矢印
        guiGraphics.fill(x, y, x + 30, y + 2, 0xFFAAAAAA);
        guiGraphics.fill(x + 24, y - 4, x + 30, y + 8, 0xFFAAAAAA);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // ドロップダウンが開いている場合、ホバー中のパーツタイプのツールチップ
        if (dropdownOpen) {
            int dropdownX = this.leftPos + DROPDOWN_X;
            int dropdownY = this.topPos + DROPDOWN_Y + DROPDOWN_CLOSED_HEIGHT;
            int visibleEntries = Math.min(DROPDOWN_MAX_VISIBLE, PART_TYPES.length);

            for (int i = 0; i < visibleEntries; i++) {
                int typeIndex = scrollOffset + i;
                if (typeIndex >= PART_TYPES.length) break;

                int entryY = dropdownY + i * DROPDOWN_ENTRY_HEIGHT;
                if (mouseX >= dropdownX && mouseX < dropdownX + DROPDOWN_WIDTH &&
                        mouseY >= entryY && mouseY < entryY + DROPDOWN_ENTRY_HEIGHT) {
                    PartType type = PART_TYPES[typeIndex];
                    guiGraphics.renderTooltip(this.font,
                            Component.translatable(type.getTranslationKey()), mouseX, mouseY);
                }
            }
        }
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
            // ドロップダウンヘッダーのクリック（開閉トグル）
            int headerX = guiX + DROPDOWN_X;
            int headerY = guiY + DROPDOWN_Y;
            if (mouseX >= headerX && mouseX < headerX + DROPDOWN_WIDTH &&
                    mouseY >= headerY && mouseY < headerY + DROPDOWN_CLOSED_HEIGHT) {
                dropdownOpen = !dropdownOpen;
                AnvilButtonRenderer.playButtonSound(AnvilButtonRenderer.ButtonSound.SELECT);
                return true;
            }

            // ドロップダウンリストのクリック（アイテム選択）
            if (dropdownOpen) {
                int listX = guiX + DROPDOWN_X;
                int listY = guiY + DROPDOWN_Y + DROPDOWN_CLOSED_HEIGHT;
                int visibleEntries = Math.min(DROPDOWN_MAX_VISIBLE, PART_TYPES.length);
                int listHeight = visibleEntries * DROPDOWN_ENTRY_HEIGHT;

                if (mouseX >= listX && mouseX < listX + DROPDOWN_WIDTH &&
                        mouseY >= listY && mouseY < listY + listHeight) {

                    int clickedEntry = (int) ((mouseY - listY) / DROPDOWN_ENTRY_HEIGHT);
                    int typeIndex = scrollOffset + clickedEntry;

                    if (typeIndex >= 0 && typeIndex < PART_TYPES.length) {
                        // サーバーにパーツタイプ選択を送信
                        PacketDistributor.sendToServer(new PartForgeActionPacket(PartForgeActionPacket.ACTION_SELECT, typeIndex));
                        // AnvilButtonRendererを使用して選択音を再生
                        AnvilButtonRenderer.playButtonSound(AnvilButtonRenderer.ButtonSound.SELECT);
                        // ドロップダウンを閉じる
                        dropdownOpen = false;
                        ANVIL.LOGGER.info("パーツタイプを選択: {}", PART_TYPES[typeIndex].getId());
                        return true;
                    }
                } else {
                    // リスト外をクリックしたらドロップダウンを閉じる
                    dropdownOpen = false;
                }
            }

            // 鋳造ボタンのクリック判定
            int forgeBtnX = guiX + FORGE_BUTTON_X;
            int forgeBtnY = guiY + FORGE_BUTTON_Y;
            if (AnvilButtonRenderer.isMouseOverButton((int) mouseX, (int) mouseY, forgeBtnX, forgeBtnY, FORGE_BUTTON_WIDTH, FORGE_BUTTON_HEIGHT)) {
                if (menu.canForge()) {
                    // サーバーに鋳造リクエストを送信
                    PacketDistributor.sendToServer(new PartForgeActionPacket(PartForgeActionPacket.ACTION_FORGE, 0));
                    // AnvilButtonRendererを使用して鋳造音を再生
                    AnvilButtonRenderer.playButtonSound(AnvilButtonRenderer.ButtonSound.ACTION);
                    ANVIL.LOGGER.info("パーツ鋳造を実行");
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // ドロップダウンリストのスクロール
        if (dropdownOpen) {
            int listX = this.leftPos + DROPDOWN_X;
            int listY = this.topPos + DROPDOWN_Y + DROPDOWN_CLOSED_HEIGHT;
            int visibleEntries = Math.min(DROPDOWN_MAX_VISIBLE, PART_TYPES.length);
            int listHeight = visibleEntries * DROPDOWN_ENTRY_HEIGHT;

            if (mouseX >= listX && mouseX < listX + DROPDOWN_WIDTH + 10 &&
                    mouseY >= listY && mouseY < listY + listHeight) {

                int maxScroll = Math.max(0, PART_TYPES.length - DROPDOWN_MAX_VISIBLE);
                if (scrollY > 0) {
                    scrollOffset = Math.max(0, scrollOffset - 1);
                } else if (scrollY < 0) {
                    scrollOffset = Math.min(maxScroll, scrollOffset + 1);
                }
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
