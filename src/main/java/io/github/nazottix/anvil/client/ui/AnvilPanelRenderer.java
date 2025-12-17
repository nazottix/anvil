package io.github.nazottix.anvil.client.ui;

import net.minecraft.client.gui.GuiGraphics;

/**
 * ANVILパネルレンダラー
 *
 * マインクラフト従来の奥行きがあるUIデザインを提供します。
 * ANVIL_STEELをベースにした立体的なパネル・スロット描画ユーティリティ。
 */
public final class AnvilPanelRenderer {

    // ============================================
    // パネル描画用カラー（ANVIL_STEELベース）
    // ============================================

    /** パネル背景色（中間色）- ANVIL_STEELベース */
    private static final int PANEL_BASE = 0xFF374151;

    /** パネル外枠 - 明るい色（上・左辺用） */
    private static final int PANEL_HIGHLIGHT = 0xFF5B6B7F;

    /** パネル外枠 - 暗い色（下・右辺用） */
    private static final int PANEL_SHADOW = 0xFF1F2937;

    /** パネル内側背景 - 少し明るい色 */
    private static final int PANEL_INNER = 0xFF4B5563;

    // ============================================
    // スロット描画用カラー（マイクラ標準風）
    // ============================================

    /** スロット外枠 - 暗い色（凹み感を出すため上・左を暗く） */
    private static final int SLOT_SHADOW = 0xFF373737;

    /** スロット外枠 - 明るい色（下・右辺用） */
    private static final int SLOT_HIGHLIGHT = 0xFFFFFFFF;

    /** スロット背景色 */
    private static final int SLOT_BACKGROUND = 0xFF8B8B8B;

    /** スロット内側の影（上・左） */
    private static final int SLOT_INNER_SHADOW = 0xFF555555;

    // ============================================
    // メインパネル描画メソッド
    // ============================================

    /**
     * 立体的なメインパネルを描画（外側に立体感）
     *
     * @param guiGraphics 描画コンテキスト
     * @param x パネルX座標
     * @param y パネルY座標
     * @param width パネル幅
     * @param height パネル高さ
     */
    public static void renderMainPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // 外枠 - 暗い色（影側：下・右）を先に描画
        guiGraphics.fill(x, y, x + width, y + height, PANEL_SHADOW);

        // 外枠 - 明るい色（ハイライト側：上・左）
        // 上辺
        guiGraphics.fill(x, y, x + width - 1, y + 1, PANEL_HIGHLIGHT);
        // 左辺
        guiGraphics.fill(x, y, x + 1, y + height - 1, PANEL_HIGHLIGHT);

        // 中間層（2px内側）
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_BASE);

        // 内側ハイライト（上・左）
        guiGraphics.fill(x + 1, y + 1, x + width - 2, y + 2, PANEL_INNER);
        guiGraphics.fill(x + 1, y + 1, x + 2, y + height - 2, PANEL_INNER);

        // 内側シャドウ（下・右）
        guiGraphics.fill(x + 2, y + height - 2, x + width - 1, y + height - 1, PANEL_SHADOW);
        guiGraphics.fill(x + width - 2, y + 2, x + width - 1, y + height - 1, PANEL_SHADOW);

        // パネル内側背景（3px内側）
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, PANEL_BASE);
    }

    /**
     * 凹んだ内側パネルを描画（スロットエリア等用）
     *
     * @param guiGraphics 描画コンテキスト
     * @param x パネルX座標
     * @param y パネルY座標
     * @param width パネル幅
     * @param height パネル高さ
     */
    public static void renderInnerPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // 外枠 - 明るい色（ハイライト側：下・右）を先に描画（凹み感のため逆転）
        guiGraphics.fill(x, y, x + width, y + height, PANEL_HIGHLIGHT);

        // 外枠 - 暗い色（影側：上・左）
        // 上辺
        guiGraphics.fill(x, y, x + width - 1, y + 1, PANEL_SHADOW);
        // 左辺
        guiGraphics.fill(x, y, x + 1, y + height - 1, PANEL_SHADOW);

        // 内側背景
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_BASE);

        // 内側シャドウ（上・左、より暗く）
        guiGraphics.fill(x + 1, y + 1, x + width - 2, y + 2, 0xFF2A3444);
        guiGraphics.fill(x + 1, y + 1, x + 2, y + height - 2, 0xFF2A3444);
    }

    // ============================================
    // スロット描画メソッド
    // ============================================

    /**
     * マイクラ標準風のスロットを描画
     *
     * @param guiGraphics 描画コンテキスト
     * @param x スロットX座標
     * @param y スロットY座標
     */
    public static void renderSlot(GuiGraphics guiGraphics, int x, int y) {
        renderSlot(guiGraphics, x, y, 16, 16);
    }

    /**
     * マイクラ標準風のスロットを描画（サイズ指定）
     *
     * @param guiGraphics 描画コンテキスト
     * @param x スロットX座標
     * @param y スロットY座標
     * @param width スロット幅
     * @param height スロット高さ
     */
    public static void renderSlot(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // スロット外枠 - 暗い色（上・左）で凹み感
        guiGraphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, SLOT_HIGHLIGHT);

        // 上辺・左辺を暗く
        guiGraphics.fill(x - 1, y - 1, x + width, y, SLOT_SHADOW);
        guiGraphics.fill(x - 1, y - 1, x, y + height, SLOT_SHADOW);

        // スロット背景
        guiGraphics.fill(x, y, x + width, y + height, SLOT_BACKGROUND);

        // 内側の影（上・左）
        guiGraphics.fill(x, y, x + width, y + 1, SLOT_INNER_SHADOW);
        guiGraphics.fill(x, y, x + 1, y + height, SLOT_INNER_SHADOW);
    }

    /**
     * プレイヤーインベントリのスロット群を描画
     *
     * @param guiGraphics 描画コンテキスト
     * @param guiX GUI左上X座標
     * @param guiY GUI左上Y座標
     * @param inventoryY インベントリスロット開始Y座標（GUI相対）
     * @param hotbarY ホットバースロット開始Y座標（GUI相対）
     */
    public static void renderInventorySlots(GuiGraphics guiGraphics, int guiX, int guiY,
                                            int inventoryY, int hotbarY) {
        // インベントリスロット（3行9列）
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotX = guiX + 8 + col * 18;
                int slotY = guiY + inventoryY + row * 18;
                renderSlot(guiGraphics, slotX, slotY);
            }
        }

        // ホットバー（1行9列）
        for (int col = 0; col < 9; col++) {
            int slotX = guiX + 8 + col * 18;
            int slotY = guiY + hotbarY;
            renderSlot(guiGraphics, slotX, slotY);
        }
    }

    // ============================================
    // 区切り線描画メソッド
    // ============================================

    /**
     * 立体的な水平区切り線を描画
     *
     * @param guiGraphics 描画コンテキスト
     * @param x 開始X座標
     * @param y Y座標
     * @param width 線の幅
     */
    public static void renderHorizontalSeparator(GuiGraphics guiGraphics, int x, int y, int width) {
        // 上の線（暗い色 = 凹み）
        guiGraphics.fill(x, y, x + width, y + 1, PANEL_SHADOW);
        // 下の線（明るい色 = ハイライト）
        guiGraphics.fill(x, y + 1, x + width, y + 2, PANEL_HIGHLIGHT);
    }

    // ============================================
    // ユーティリティ
    // ============================================

    // コンストラクタを非公開
    private AnvilPanelRenderer() {}
}
