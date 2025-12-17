package io.github.nazottix.anvil.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/**
 * ANVILボタンレンダラー
 *
 * Station画面で使用するボタンの統一描画・音再生ユーティリティ。
 * テーマカラー（FORGE_ORANGE/VOID_BLACK）を使用した一貫したUIを提供します。
 */
public final class AnvilButtonRenderer {

    // ============================================
    // ボタン音の種類
    // ============================================

    /**
     * ボタン音の種類
     */
    public enum ButtonSound {
        /** アクション実行音（クラフト、修理等）- 金床の音 */
        ACTION,
        /** 選択音（タブ切替等）- UIクリック音 */
        SELECT,
        /** 装備音（ジュエル装着等）- 経験値音 */
        EQUIP,
        /** 無音 */
        NONE
    }

    // ============================================
    // ボタン描画メソッド
    // ============================================

    // ============================================
    // ボタン描画用カラー
    // ============================================

    /** ボタン背景色（通常） */
    private static final int BUTTON_BG_NORMAL = 0xFF2D2D2D;
    /** ボタン背景色（ホバー） */
    private static final int BUTTON_BG_HOVER = 0xFF3D3D3D;
    /** ボタン背景色（押下/選択） */
    private static final int BUTTON_BG_PRESSED = 0xFF1D1D1D;
    /** ボタンハイライト色（上・左辺） */
    private static final int BUTTON_HIGHLIGHT = 0xFF5A5A5A;
    /** ボタンシャドウ色（下・右辺） */
    private static final int BUTTON_SHADOW = 0xFF1A1A1A;
    /** ボタン無効時の背景色 */
    private static final int BUTTON_BG_DISABLED = 0xFF1F1F1F;

    /**
     * テーマに沿ったボタンを描画（マイクラ風立体デザイン）
     *
     * @param guiGraphics 描画コンテキスト
     * @param font フォント
     * @param x ボタンX座標
     * @param y ボタンY座標
     * @param width ボタン幅
     * @param height ボタン高さ
     * @param text ボタンテキスト
     * @param isHovered ホバー中かどうか
     * @param isEnabled 有効かどうか
     */
    public static void renderButton(GuiGraphics guiGraphics, Font font,
                                    int x, int y, int width, int height,
                                    Component text, boolean isHovered, boolean isEnabled) {
        int bgColor;
        int highlightColor;
        int shadowColor;
        int textColor;

        if (isEnabled) {
            if (isHovered) {
                // ホバー状態: 明るめの背景
                bgColor = BUTTON_BG_HOVER;
                highlightColor = 0xFF7A7A7A;
                shadowColor = BUTTON_SHADOW;
                textColor = 0xFFFFFFFF;
            } else {
                // 通常状態
                bgColor = BUTTON_BG_NORMAL;
                highlightColor = BUTTON_HIGHLIGHT;
                shadowColor = BUTTON_SHADOW;
                textColor = AnvilColors.FORGE_ORANGE;
            }
        } else {
            // 無効状態: グレーアウト
            bgColor = BUTTON_BG_DISABLED;
            highlightColor = 0xFF3A3A3A;
            shadowColor = 0xFF0A0A0A;
            textColor = 0xFF555555;
        }

        // 外枠（シャドウ側：下・右）を先に描画
        guiGraphics.fill(x, y, x + width, y + height, shadowColor);

        // 外枠（ハイライト側：上・左）
        guiGraphics.fill(x, y, x + width - 1, y + 1, highlightColor);
        guiGraphics.fill(x, y, x + 1, y + height - 1, highlightColor);

        // ボタン背景（1px内側）
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, bgColor);

        // テキストを中央に描画
        int textWidth = font.width(text);
        guiGraphics.drawString(font, text,
                x + (width - textWidth) / 2,
                y + (height - 8) / 2,
                textColor, false);
    }

    /**
     * シンボル付きの小型ボタンを描画（ツールタイプ選択等、マイクラ風立体デザイン）
     *
     * @param guiGraphics 描画コンテキスト
     * @param font フォント
     * @param x ボタンX座標
     * @param y ボタンY座標
     * @param size ボタンサイズ（正方形）
     * @param symbol ボタンに表示するシンボル
     * @param symbolColor シンボルの色
     * @param isSelected 選択中かどうか
     * @param isHovered ホバー中かどうか
     */
    public static void renderIconButton(GuiGraphics guiGraphics, Font font,
                                        int x, int y, int size,
                                        String symbol, int symbolColor,
                                        boolean isSelected, boolean isHovered) {
        int bgColor;
        int highlightColor;
        int shadowColor;
        int displayColor;

        if (isSelected) {
            // 選択状態: 凹んだデザイン（ハイライトとシャドウを逆転）
            bgColor = BUTTON_BG_PRESSED;
            highlightColor = BUTTON_SHADOW;  // 逆転
            shadowColor = BUTTON_HIGHLIGHT;  // 逆転
            displayColor = 0xFFFFFFFF;
        } else if (isHovered) {
            // ホバー状態: 明るめ
            bgColor = BUTTON_BG_HOVER;
            highlightColor = 0xFF7A7A7A;
            shadowColor = BUTTON_SHADOW;
            displayColor = symbolColor;
        } else {
            // 通常状態
            bgColor = BUTTON_BG_NORMAL;
            highlightColor = BUTTON_HIGHLIGHT;
            shadowColor = BUTTON_SHADOW;
            displayColor = symbolColor;
        }

        // 外枠（シャドウ側：下・右）を先に描画
        guiGraphics.fill(x, y, x + size, y + size, shadowColor);

        // 外枠（ハイライト側：上・左）
        guiGraphics.fill(x, y, x + size - 1, y + 1, highlightColor);
        guiGraphics.fill(x, y, x + 1, y + size - 1, highlightColor);

        // ボタン背景（1px内側）
        guiGraphics.fill(x + 1, y + 1, x + size - 1, y + size - 1, bgColor);

        // シンボルを中央に描画
        guiGraphics.drawCenteredString(font, symbol, x + size / 2, y + (size - 8) / 2, displayColor);
    }

    // ============================================
    // 音再生メソッド
    // ============================================

    /**
     * ボタンクリック音を再生
     *
     * @param sound 再生する音の種類
     */
    public static void playButtonSound(ButtonSound sound) {
        Minecraft mc = Minecraft.getInstance();
        switch (sound) {
            case ACTION -> {
                // 金床の音（クラフト・修理等のアクション）
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ANVIL_USE, 1.0F, 0.5F));
            }
            case SELECT -> {
                // UIボタンクリック音（選択・タブ切替等）
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F));
            }
            case EQUIP -> {
                // 装備音（ジュエル装着等）- 金属音を使用
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ARMOR_EQUIP_IRON.value(), 1.0F, 1.2F));
            }
            case NONE -> {
                // 無音
            }
        }
    }

    // ============================================
    // ユーティリティメソッド
    // ============================================

    /**
     * マウスがボタン上にあるかを判定
     *
     * @param mouseX マウスX座標
     * @param mouseY マウスY座標
     * @param x ボタンX座標
     * @param y ボタンY座標
     * @param width ボタン幅
     * @param height ボタン高さ
     * @return マウスがボタン上にある場合true
     */
    public static boolean isMouseOverButton(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    // コンストラクタを非公開
    private AnvilButtonRenderer() {}
}
