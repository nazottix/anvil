package io.github.nazottix.anvil.client.screen;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.menu.PartForgeMenu;
import io.github.nazottix.anvil.network.PartForgeActionPacket;
import io.github.nazottix.anvil.tool.PartType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * パーツ鍛造所スクリーン
 *
 * パーツ鍛造所のクライアント側UI描画を担当します。
 * パーツタイプの選択リストと鋳造ボタンを表示します。
 */
public class PartForgeScreen extends AbstractContainerScreen<PartForgeMenu> {

    // パーツタイプ一覧
    private static final PartType[] PART_TYPES = PartType.values();

    // UIオフセット
    private static final int LIST_X = 50;
    private static final int LIST_Y = 17;
    private static final int LIST_ENTRY_HEIGHT = 12;
    private static final int LIST_VISIBLE_ENTRIES = 5;

    // スクロール位置
    private int scrollOffset = 0;

    // 鋳造ボタン
    private Button forgeButton;

    /**
     * コンストラクタ
     */
    public PartForgeScreen(PartForgeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();

        // タイトルラベル位置
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelY = this.imageHeight - 94;

        // 鋳造ボタンを追加
        int buttonX = this.leftPos + 100;
        int buttonY = this.topPos + 45;
        forgeButton = Button.builder(Component.translatable("gui.anvil.part_forge.forge"), this::onForgeButtonPressed)
                .bounds(buttonX, buttonY, 30, 20)
                .build();
        this.addRenderableWidget(forgeButton);
    }

    /**
     * 鋳造ボタン押下時の処理
     */
    private void onForgeButtonPressed(Button button) {
        // サーバーに鋳造リクエストを送信
        PacketDistributor.sendToServer(new PartForgeActionPacket(PartForgeActionPacket.ACTION_FORGE, 0));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // 背景色で塗りつぶし
        int x = this.leftPos;
        int y = this.topPos;

        // メインパネル背景
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFFC6C6C6);

        // パネル枠
        // 上辺（白）
        guiGraphics.fill(x, y, x + imageWidth, y + 1, 0xFFFFFFFF);
        // 左辺（白）
        guiGraphics.fill(x, y, x + 1, y + imageHeight, 0xFFFFFFFF);
        // 右辺（暗い）
        guiGraphics.fill(x + imageWidth - 1, y, x + imageWidth, y + imageHeight, 0xFF555555);
        // 下辺（暗い）
        guiGraphics.fill(x, y + imageHeight - 1, x + imageWidth, y + imageHeight, 0xFF555555);

        // 入力スロット枠（凹み効果）
        int inputX = x + 26;
        int inputY = y + 46;
        drawSlot(guiGraphics, inputX, inputY);

        // 出力スロット枠
        int outputX = x + 133;
        int outputY = y + 46;
        drawSlot(guiGraphics, outputX, outputY);

        // 矢印（入力 → 出力）
        int arrowX = x + 96;
        int arrowY = y + 50;
        guiGraphics.fill(arrowX, arrowY, arrowX + 20, arrowY + 8, 0xFF8B8B8B);
        // 矢印の先（三角形近似）
        guiGraphics.fill(arrowX + 20, arrowY - 2, arrowX + 24, arrowY + 10, 0xFF8B8B8B);

        // パーツタイプリスト描画
        renderPartTypeList(guiGraphics, x, y, mouseX, mouseY);
    }

    /**
     * スロット枠を描画
     */
    private void drawSlot(GuiGraphics guiGraphics, int x, int y) {
        // 凹み効果：上と左が暗く、下と右が明るい
        guiGraphics.fill(x - 1, y - 1, x + 17, y, 0xFF373737);
        guiGraphics.fill(x - 1, y - 1, x, y + 17, 0xFF373737);
        guiGraphics.fill(x, y + 16, x + 17, y + 17, 0xFFFFFFFF);
        guiGraphics.fill(x + 16, y, x + 17, y + 17, 0xFFFFFFFF);
        // スロット内部
        guiGraphics.fill(x, y, x + 16, y + 16, 0xFF8B8B8B);
    }

    /**
     * パーツタイプリストを描画
     */
    private void renderPartTypeList(GuiGraphics guiGraphics, int panelX, int panelY, int mouseX, int mouseY) {
        int listX = panelX + LIST_X;
        int listY = panelY + LIST_Y;
        int listWidth = 40;
        int listHeight = LIST_VISIBLE_ENTRIES * LIST_ENTRY_HEIGHT;

        // リスト背景
        guiGraphics.fill(listX - 1, listY - 1, listX + listWidth + 1, listY + listHeight + 1, 0xFF373737);
        guiGraphics.fill(listX, listY, listX + listWidth, listY + listHeight, 0xFF8B8B8B);

        // 選択中のパーツタイプ
        int selectedIndex = menu.getSelectedPartTypeIndex();

        // 表示するエントリを描画
        for (int i = 0; i < LIST_VISIBLE_ENTRIES; i++) {
            int typeIndex = scrollOffset + i;
            if (typeIndex >= PART_TYPES.length) break;

            PartType type = PART_TYPES[typeIndex];
            int entryY = listY + i * LIST_ENTRY_HEIGHT;

            // 選択中のエントリはハイライト
            if (typeIndex == selectedIndex) {
                guiGraphics.fill(listX, entryY, listX + listWidth, entryY + LIST_ENTRY_HEIGHT, 0xFF4080FF);
            }

            // マウスホバーハイライト
            if (mouseX >= listX && mouseX < listX + listWidth &&
                    mouseY >= entryY && mouseY < entryY + LIST_ENTRY_HEIGHT) {
                if (typeIndex != selectedIndex) {
                    guiGraphics.fill(listX, entryY, listX + listWidth, entryY + LIST_ENTRY_HEIGHT, 0xFF6090C0);
                }
            }

            // パーツ名を描画
            String name = type.getDisplayNameJa();
            if (name.length() > 6) {
                name = name.substring(0, 5) + "..";
            }
            guiGraphics.drawString(this.font, name, listX + 2, entryY + 2, 0xFFFFFFFF, false);
        }

        // スクロールバー
        if (PART_TYPES.length > LIST_VISIBLE_ENTRIES) {
            int scrollBarX = listX + listWidth + 2;
            int scrollBarHeight = listHeight * LIST_VISIBLE_ENTRIES / PART_TYPES.length;
            int scrollBarY = listY + (listHeight - scrollBarHeight) * scrollOffset / (PART_TYPES.length - LIST_VISIBLE_ENTRIES);

            guiGraphics.fill(scrollBarX, listY, scrollBarX + 3, listY + listHeight, 0xFF555555);
            guiGraphics.fill(scrollBarX, scrollBarY, scrollBarX + 3, scrollBarY + scrollBarHeight, 0xFFAAAAAA);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // パーツタイプリストのツールチップ
        int listX = this.leftPos + LIST_X;
        int listY = this.topPos + LIST_Y;
        int listWidth = 40;

        for (int i = 0; i < LIST_VISIBLE_ENTRIES; i++) {
            int typeIndex = scrollOffset + i;
            if (typeIndex >= PART_TYPES.length) break;

            int entryY = listY + i * LIST_ENTRY_HEIGHT;
            if (mouseX >= listX && mouseX < listX + listWidth &&
                    mouseY >= entryY && mouseY < entryY + LIST_ENTRY_HEIGHT) {
                PartType type = PART_TYPES[typeIndex];
                guiGraphics.renderTooltip(this.font,
                        Component.translatable(type.getTranslationKey()), mouseX, mouseY);
            }
        }

        // 鋳造ボタンの状態更新
        forgeButton.active = menu.canForge();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // パーツタイプリストのクリック処理
        int listX = this.leftPos + LIST_X;
        int listY = this.topPos + LIST_Y;
        int listWidth = 40;
        int listHeight = LIST_VISIBLE_ENTRIES * LIST_ENTRY_HEIGHT;

        if (mouseX >= listX && mouseX < listX + listWidth &&
                mouseY >= listY && mouseY < listY + listHeight) {

            int clickedEntry = (int) ((mouseY - listY) / LIST_ENTRY_HEIGHT);
            int typeIndex = scrollOffset + clickedEntry;

            if (typeIndex >= 0 && typeIndex < PART_TYPES.length) {
                // サーバーにパーツタイプ選択を送信
                PacketDistributor.sendToServer(new PartForgeActionPacket(PartForgeActionPacket.ACTION_SELECT, typeIndex));
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // パーツタイプリストのスクロール
        int listX = this.leftPos + LIST_X;
        int listY = this.topPos + LIST_Y;
        int listWidth = 40;
        int listHeight = LIST_VISIBLE_ENTRIES * LIST_ENTRY_HEIGHT;

        if (mouseX >= listX && mouseX < listX + listWidth + 5 &&
                mouseY >= listY && mouseY < listY + listHeight) {

            int maxScroll = Math.max(0, PART_TYPES.length - LIST_VISIBLE_ENTRIES);
            if (scrollY > 0) {
                scrollOffset = Math.max(0, scrollOffset - 1);
            } else if (scrollY < 0) {
                scrollOffset = Math.min(maxScroll, scrollOffset + 1);
            }
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
