package io.github.nazottix.anvil.mod;

/**
 * MODタイプの定義
 *
 * MODの効果の種類を定義します。
 *
 * 仕様書参照: docs/05_容量制限システム.md
 */
public enum ModType {

    // ============================================
    // MODタイプ
    // ============================================

    /**
     * 標準MOD
     * - 単一ステータス強化
     * - 例: Damage+60%
     */
    STANDARD("standard", false, false),

    /**
     * 複合MOD
     * - 複数ステータス強化
     * - 例: Damage+30%, Speed+15%
     */
    COMPOSITE("composite", false, false),

    /**
     * 腐敗MOD（Corrupted）
     * - 大ボーナス+デメリット
     * - 例: Efficiency+60%, Duration-60%
     */
    CORRUPTED("corrupted", true, false),

    /**
     * セットMOD
     * - セットボーナス付き
     * - 例: グラディエーター系
     */
    SET("set", false, true),

    /**
     * オーラMOD
     * - 容量を追加する特殊MOD
     * - オーラスロット専用
     */
    AURA("aura", false, false),

    /**
     * エクシルスMOD
     * - エクシルススロット専用
     * - ユーティリティ効果
     */
    EXILUS("exilus", false, false),

    /**
     * ツール専用MOD
     * - 特定のツールタイプでのみ使用可能
     * - 例: Pickaxe専用: Vein Miner
     */
    TOOL_SPECIFIC("tool_specific", false, false);

    // ============================================
    // フィールド
    // ============================================

    private final String id;
    private final boolean hasDrawback;
    private final boolean isSetMod;

    ModType(String id, boolean hasDrawback, boolean isSetMod) {
        this.id = id;
        this.hasDrawback = hasDrawback;
        this.isSetMod = isSetMod;
    }

    // ============================================
    // ゲッター
    // ============================================

    public String getId() {
        return id;
    }

    /**
     * デメリットを持つMODかどうか
     */
    public boolean hasDrawback() {
        return hasDrawback;
    }

    /**
     * セットボーナスを持つMODかどうか
     */
    public boolean isSetMod() {
        return isSetMod;
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * IDからタイプを取得
     */
    public static ModType fromId(String id) {
        if (id == null || id.isEmpty()) {
            return STANDARD;
        }
        for (ModType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return STANDARD;
    }
}
