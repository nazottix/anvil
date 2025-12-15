package io.github.nazottix.anvil.mod;

import net.minecraft.ChatFormatting;

/**
 * MOD極性（ポラリティ）の定義
 *
 * Warframe風の5極性システムを実装。
 * MODとスロットの極性が一致すると消費容量が半減、
 * 不一致だと25%増加します。
 *
 * 仕様書参照: docs/05_容量制限システム.md
 */
public enum Polarity {

    // ============================================
    // 極性タイプ（5種類）
    // ============================================

    /**
     * 攻撃極性（Madurai）
     * - シンボル: ▽
     * - 色: 赤
     * - 主要用途: ダメージ、クリティカル系MOD
     */
    MADURAI("madurai", "\u25BD", ChatFormatting.RED, "attack"),

    /**
     * 防御極性（Vazarin）
     * - シンボル: D
     * - 色: 青
     * - 主要用途: 耐久、防御系MOD
     */
    VAZARIN("vazarin", "D", ChatFormatting.BLUE, "defense"),

    /**
     * 汎用極性（Naramon）
     * - シンボル: —
     * - 色: 白
     * - 主要用途: ユーティリティMOD
     */
    NARAMON("naramon", "\u2014", ChatFormatting.WHITE, "utility"),

    /**
     * 特殊極性（Zenurik）
     * - シンボル: ~
     * - 色: 紫
     * - 主要用途: 特殊効果MOD
     */
    ZENURIK("zenurik", "~", ChatFormatting.LIGHT_PURPLE, "special"),

    /**
     * エレメント極性（Unairu）
     * - シンボル: ◇
     * - 色: 緑
     * - 主要用途: 属性ダメージMOD
     */
    UNAIRU("unairu", "\u25C7", ChatFormatting.GREEN, "element");

    // ============================================
    // フィールド
    // ============================================

    private final String id;
    private final String symbol;
    private final ChatFormatting color;
    private final String category;

    Polarity(String id, String symbol, ChatFormatting color, String category) {
        this.id = id;
        this.symbol = symbol;
        this.color = color;
        this.category = category;
    }

    // ============================================
    // ゲッター
    // ============================================

    /**
     * 極性IDを取得
     */
    public String getId() {
        return id;
    }

    /**
     * 表示シンボルを取得
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * 表示色を取得
     */
    public ChatFormatting getColor() {
        return color;
    }

    /**
     * カテゴリを取得
     */
    public String getCategory() {
        return category;
    }

    /**
     * 色付きシンボルを取得
     */
    public String getColoredSymbol() {
        return color + symbol + ChatFormatting.RESET;
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * IDから極性を取得
     *
     * @param id 極性ID
     * @return 対応する極性、見つからない場合はnull
     */
    public static Polarity fromId(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        for (Polarity polarity : values()) {
            if (polarity.id.equals(id)) {
                return polarity;
            }
        }
        return null;
    }

    /**
     * ドレイン計算
     *
     * @param baseDrain MODの基本ドレイン
     * @param modPolarity MODの極性
     * @param slotPolarity スロットの極性
     * @return 計算後のドレイン値
     */
    public static int calculateDrain(int baseDrain, Polarity modPolarity, Polarity slotPolarity) {
        // スロットに極性がない場合は基本ドレイン
        if (slotPolarity == null) {
            return baseDrain;
        }

        // 極性一致: 50%（切り上げ）
        if (modPolarity == slotPolarity) {
            return (int) Math.ceil(baseDrain / 2.0);
        }

        // 極性不一致: 125%（四捨五入）
        return (int) Math.round(baseDrain * 1.25);
    }
}
