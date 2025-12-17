package io.github.nazottix.anvil.affix;

/**
 * アフィックスタイプ列挙型
 *
 * アフィックス（接辞）の種類を定義します。
 * - PREFIX: 接頭辞（例：「鋭利な」ピッケル）
 * - SUFFIX: 接尾辞（例：ピッケル「の炎」）
 *
 * 仕様書参照: docs/06_レアリティシステム.md - 8.3 アフィックスシステム
 */
public enum AffixType {
    /**
     * プレフィックス（接頭辞）
     * ツール名の前に付く修飾語
     * 例：「鋭利な」「堅固な」「迅速な」
     */
    PREFIX("prefix", "接頭辞"),

    /**
     * サフィックス（接尾辞）
     * ツール名の後に付く修飾語
     * 例：「〜の炎」「〜の氷」「〜の雷」
     */
    SUFFIX("suffix", "接尾辞");

    private final String id;
    private final String displayNameJa;

    AffixType(String id, String displayNameJa) {
        this.id = id;
        this.displayNameJa = displayNameJa;
    }

    public String getId() {
        return id;
    }

    public String getDisplayNameJa() {
        return displayNameJa;
    }

    /**
     * ローカライズキーを取得
     */
    public String getTranslationKey() {
        return "affix.anvil.type." + id;
    }
}
