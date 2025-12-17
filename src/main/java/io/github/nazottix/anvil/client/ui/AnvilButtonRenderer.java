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

    /**
     * テーマに沿ったボタンを描画
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
        // ボタン色の決定（有効/無効、ホバー状態により変化）
        int bgColor;
        int borderColor;
        int textColor;

        if (isEnabled) {
            // 有効状態: テーマカラーを使用
            bgColor = isHovered ? (AnvilColors.FORGE_ORANGE | 0xFF000000) : (AnvilColors.VOID_BLACK | 0xFF000000);
            borderColor = isHovered ? 0xFFFFFFFF : AnvilColors.FORGE_ORANGE;
            textColor = isHovered ? 0xFFFFFF : AnvilColors.FORGE_ORANGE;
        } else {
            // 無効状態: グレーアウト
            bgColor = AnvilColors.VOID_BLACK | 0xFF000000;
            borderColor = 0xFF555555;
            textColor = 0xFF555555;
        }

        // 枠を描画（1px外側）
        guiGraphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, borderColor);
        // 背景を描画
        guiGraphics.fill(x, y, x + width, y + height, bgColor);

        // テキストを中央に描画
        int textWidth = font.width(text);
        guiGraphics.drawString(font, text,
                x + (width - textWidth) / 2,
                y + (height - 8) / 2,
                textColor, false);
    }

    /**
     * シンボル付きの小型ボタンを描画（ツールタイプ選択等）
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
        // 背景色の決定
        int bgColor = isSelected ? (symbolColor | 0xFF000000) :
                isHovered ? 0xFF444444 : (AnvilColors.VOID_BLACK | 0xFF000000);
        guiGraphics.fill(x, y, x + size, y + size, bgColor);

        // 枠の描画
        int borderColor = isSelected ? 0xFFFFFFFF : 0xFF666666;
        guiGraphics.renderOutline(x, y, size, size, borderColor);

        // シンボルを中央に描画
        int displayColor = isSelected ? 0xFFFFFF : symbolColor;
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
