package io.github.nazottix.anvil.grid;

import net.minecraft.ChatFormatting;

/**
 * モジュールカテゴリ定義
 *
 * モジュールの種類を分類します。
 * シナジーボーナス計算に使用されます。
 *
 * 仕様書参照: docs/04_グリッド配置システム.md
 */
public enum ModuleCategory {

    // ============================================
    // カテゴリ
    // ============================================

    /** 攻撃系: ダメージ、クリティカル */
    ATTACK("attack", ChatFormatting.RED),

    /** 防御系: 耐久、ダメージ軽減 */
    DEFENSE("defense", ChatFormatting.BLUE),

    /** 速度系: 速度、効率 */
    SPEED("speed", ChatFormatting.YELLOW),

    /** ユーティリティ: 特殊効果 */
    UTILITY("utility", ChatFormatting.GREEN),

    /** 容量系: MOD容量追加 */
    CAPACITY("capacity", ChatFormatting.LIGHT_PURPLE),

    /** コネクタ: シナジーボーナス強化 */
    CONNECTOR("connector", ChatFormatting.WHITE);

    // ============================================
    // フィールド
    // ============================================

    private final String id;
    private final ChatFormatting color;

    ModuleCategory(String id, ChatFormatting color) {
        this.id = id;
        this.color = color;
    }

    // ============================================
    // ゲッター
    // ============================================

    public String getId() {
        return id;
    }

    public ChatFormatting getColor() {
        return color;
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * IDからカテゴリを取得
     */
    public static ModuleCategory fromId(String id) {
        for (ModuleCategory category : values()) {
            if (category.id.equals(id)) {
                return category;
            }
        }
        return UTILITY;
    }
}
