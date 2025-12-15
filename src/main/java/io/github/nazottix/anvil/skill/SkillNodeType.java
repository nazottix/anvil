package io.github.nazottix.anvil.skill;

/**
 * スキルノードタイプ定義
 *
 * スキルツリーにおけるノードの種類を定義します。
 *
 * 仕様書参照: docs/03_スキルツリーシステム.md
 */
public enum SkillNodeType {

    // ============================================
    // ノードタイプ定義
    // ============================================

    /**
     * マイナーノード（小）
     * - 効果: +2-5%ステータス
     * - 出現比率: 70%
     * - ポイントコスト: 1
     */
    MINOR("minor", 1, 0.7f, 0x888888),

    /**
     * ノータブル（中）
     * - 効果: +10-20%、特殊効果
     * - 出現比率: 15%
     * - ポイントコスト: 3
     */
    NOTABLE("notable", 3, 0.15f, 0x55AAFF),

    /**
     * キーストーン（大）
     * - 効果: ビルド定義級（トレードオフ）
     * - 出現比率: 3%
     * - ポイントコスト: 5
     */
    KEYSTONE("keystone", 5, 0.03f, 0xFFAA00),

    /**
     * ジュエルソケット（特殊）
     * - 効果: カスタムジュエル装着
     * - 出現比率: 7%
     * - ポイントコスト: 0（装着のみ）
     */
    JEWEL_SOCKET("jewel_socket", 0, 0.07f, 0x44FFFF),

    /**
     * マスタリーノード（超大）
     * - 効果: ツリー最終目標
     * - 出現比率: 5%
     * - ポイントコスト: 10
     */
    MASTERY("mastery", 10, 0.05f, 0xFF44FF),

    /**
     * ルートノード（起点）
     * - 効果: 初期ボーナス
     * - 出現比率: 1つのみ
     * - ポイントコスト: 0
     */
    ROOT("root", 0, 0.0f, 0x44FF44);

    // ============================================
    // フィールド
    // ============================================

    private final String id;
    private final int baseCost;
    private final float spawnRatio;
    private final int displayColor;

    SkillNodeType(String id, int baseCost, float spawnRatio, int displayColor) {
        this.id = id;
        this.baseCost = baseCost;
        this.spawnRatio = spawnRatio;
        this.displayColor = displayColor;
    }

    // ============================================
    // ゲッター
    // ============================================

    public String getId() {
        return id;
    }

    /**
     * 基本ポイントコストを取得
     */
    public int getBaseCost() {
        return baseCost;
    }

    /**
     * 出現比率を取得
     */
    public float getSpawnRatio() {
        return spawnRatio;
    }

    /**
     * 表示色を取得
     */
    public int getDisplayColor() {
        return displayColor;
    }

    /**
     * ノードサイズ（ピクセル）を取得
     */
    public int getDisplaySize() {
        return switch (this) {
            case MINOR -> 12;
            case NOTABLE -> 18;
            case KEYSTONE -> 28;
            case JEWEL_SOCKET -> 20;
            case MASTERY -> 32;
            case ROOT -> 24;
        };
    }

    /**
     * IDからタイプを取得
     */
    public static SkillNodeType fromId(String id) {
        for (SkillNodeType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return MINOR;
    }
}
