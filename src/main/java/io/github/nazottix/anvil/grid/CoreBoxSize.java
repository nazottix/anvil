package io.github.nazottix.anvil.grid;

/**
 * コアボックスサイズ定義
 *
 * ツールのグリッドサイズを定義します。
 * レベルに応じて段階的に解放されます。
 *
 * 仕様書参照: docs/04_グリッド配置システム.md
 */
public enum CoreBoxSize {

    // ============================================
    // サイズ定義
    // ============================================

    /** Tiny: 2×2 (4マス) - 初期 */
    TINY("tiny", 2, 0),

    /** Small: 3×3 (9マス) - Lv.50 */
    SMALL("small", 3, 50),

    /** Medium: 4×4 (16マス) - Lv.500 */
    MEDIUM("medium", 4, 500),

    /** Large: 5×5 (25マス) - Lv.2000 */
    LARGE("large", 5, 2000),

    /** Extra Large: 6×6 (36マス) - Lv.5000 */
    EXTRA_LARGE("extra_large", 6, 5000),

    /** Maximum: 7×7 (49マス) - Lv.8000 */
    MAXIMUM("maximum", 7, 8000),

    /** Legendary: 8×8 (64マス) - Lv.10000 + 特殊アイテム */
    LEGENDARY("legendary", 8, 10000);

    // ============================================
    // フィールド
    // ============================================

    private final String id;
    private final int gridSize;
    private final int requiredLevel;

    CoreBoxSize(String id, int gridSize, int requiredLevel) {
        this.id = id;
        this.gridSize = gridSize;
        this.requiredLevel = requiredLevel;
    }

    // ============================================
    // ゲッター
    // ============================================

    public String getId() {
        return id;
    }

    /**
     * グリッドの一辺のサイズ
     */
    public int getGridSize() {
        return gridSize;
    }

    /**
     * 総マス数
     */
    public int getTotalCells() {
        return gridSize * gridSize;
    }

    /**
     * 解放に必要なレベル
     */
    public int getRequiredLevel() {
        return requiredLevel;
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * IDからサイズを取得
     */
    public static CoreBoxSize fromId(String id) {
        for (CoreBoxSize size : values()) {
            if (size.id.equals(id)) {
                return size;
            }
        }
        return TINY;
    }

    /**
     * レベルに応じた最大サイズを取得
     *
     * @param level ツールレベル
     * @return 使用可能な最大サイズ
     */
    public static CoreBoxSize getMaxForLevel(int level) {
        CoreBoxSize result = TINY;
        for (CoreBoxSize size : values()) {
            if (level >= size.requiredLevel) {
                result = size;
            }
        }
        return result;
    }

    /**
     * 次のサイズを取得
     *
     * @return 次のサイズ、最大の場合はnull
     */
    public CoreBoxSize getNextSize() {
        int nextOrdinal = this.ordinal() + 1;
        if (nextOrdinal >= values().length) {
            return null;
        }
        return values()[nextOrdinal];
    }

    /**
     * 座標がグリッド内かどうかを確認
     *
     * @param x X座標
     * @param y Y座標
     * @return グリッド内の場合true
     */
    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < gridSize && y >= 0 && y < gridSize;
    }
}
