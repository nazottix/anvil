package io.github.nazottix.anvil.grid;

import java.util.Arrays;
import java.util.List;

/**
 * モジュール形状定義
 *
 * テトリス式のモジュール形状を定義します。
 * 各形状は占有するセルの相対座標で表現されます。
 *
 * 仕様書参照: docs/04_グリッド配置システム.md
 */
public enum ModuleShape {

    // ============================================
    // 基本形状
    // ============================================

    /** 1×1 基本形状 */
    SINGLE("single", new int[][]{{0, 0}}),

    /** 2×1 横長 */
    HORIZONTAL_2("horizontal_2", new int[][]{{0, 0}, {1, 0}}),

    /** 1×2 縦長 */
    VERTICAL_2("vertical_2", new int[][]{{0, 0}, {0, 1}}),

    /** 2×2 正方形 */
    SQUARE_2("square_2", new int[][]{{0, 0}, {1, 0}, {0, 1}, {1, 1}}),

    /** 3×1 横長棒 */
    HORIZONTAL_3("horizontal_3", new int[][]{{0, 0}, {1, 0}, {2, 0}}),

    /** 1×3 縦長棒 */
    VERTICAL_3("vertical_3", new int[][]{{0, 0}, {0, 1}, {0, 2}}),

    // ============================================
    // L字型・T字型
    // ============================================

    /** L字型 */
    L_SHAPE("l_shape", new int[][]{{0, 0}, {0, 1}, {1, 1}}),

    /** 逆L字型 */
    L_SHAPE_REVERSE("l_shape_reverse", new int[][]{{1, 0}, {1, 1}, {0, 1}}),

    /** T字型 */
    T_SHAPE("t_shape", new int[][]{{0, 0}, {1, 0}, {2, 0}, {1, 1}}),

    /** 逆T字型 */
    T_SHAPE_REVERSE("t_shape_reverse", new int[][]{{1, 0}, {0, 1}, {1, 1}, {2, 1}}),

    // ============================================
    // Z字型・S字型
    // ============================================

    /** Z字型 */
    Z_SHAPE("z_shape", new int[][]{{0, 0}, {1, 0}, {1, 1}, {2, 1}}),

    /** S字型 */
    S_SHAPE("s_shape", new int[][]{{1, 0}, {2, 0}, {0, 1}, {1, 1}}),

    // ============================================
    // その他の形状
    // ============================================

    /** 十字型 */
    CROSS("cross", new int[][]{{1, 0}, {0, 1}, {1, 1}, {2, 1}, {1, 2}}),

    /** 3×3 正方形 */
    SQUARE_3("square_3", new int[][]{
            {0, 0}, {1, 0}, {2, 0},
            {0, 1}, {1, 1}, {2, 1},
            {0, 2}, {1, 2}, {2, 2}
    }),

    /** コの字型 */
    U_SHAPE("u_shape", new int[][]{{0, 0}, {2, 0}, {0, 1}, {1, 1}, {2, 1}}),

    /** 階段型 */
    STAIRS("stairs", new int[][]{{0, 0}, {0, 1}, {1, 1}, {1, 2}, {2, 2}});

    // ============================================
    // フィールド
    // ============================================

    private final String id;
    private final int[][] cells;
    private final int width;
    private final int height;

    ModuleShape(String id, int[][] cells) {
        this.id = id;
        this.cells = cells;

        // 幅と高さを計算
        int maxX = 0, maxY = 0;
        for (int[] cell : cells) {
            maxX = Math.max(maxX, cell[0]);
            maxY = Math.max(maxY, cell[1]);
        }
        this.width = maxX + 1;
        this.height = maxY + 1;
    }

    // ============================================
    // ゲッター
    // ============================================

    public String getId() {
        return id;
    }

    /**
     * 占有セルの相対座標リスト
     */
    public int[][] getCells() {
        return cells;
    }

    /**
     * セル数
     */
    public int getCellCount() {
        return cells.length;
    }

    /**
     * 形状の幅
     */
    public int getWidth() {
        return width;
    }

    /**
     * 形状の高さ
     */
    public int getHeight() {
        return height;
    }

    // ============================================
    // 回転
    // ============================================

    /**
     * 回転した形状のセル座標を取得
     *
     * @param rotation 回転（0=0°, 1=90°, 2=180°, 3=270°）
     * @return 回転後のセル座標
     */
    public int[][] getRotatedCells(int rotation) {
        rotation = rotation % 4;
        if (rotation == 0) {
            return cells;
        }

        int[][] rotated = new int[cells.length][2];
        for (int i = 0; i < cells.length; i++) {
            int x = cells[i][0];
            int y = cells[i][1];

            switch (rotation) {
                case 1: // 90度
                    rotated[i] = new int[]{height - 1 - y, x};
                    break;
                case 2: // 180度
                    rotated[i] = new int[]{width - 1 - x, height - 1 - y};
                    break;
                case 3: // 270度
                    rotated[i] = new int[]{y, width - 1 - x};
                    break;
                default:
                    rotated[i] = new int[]{x, y};
            }
        }

        // 正規化（左上を0,0に）
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        for (int[] cell : rotated) {
            minX = Math.min(minX, cell[0]);
            minY = Math.min(minY, cell[1]);
        }
        for (int[] cell : rotated) {
            cell[0] -= minX;
            cell[1] -= minY;
        }

        return rotated;
    }

    /**
     * 回転後の幅を取得
     */
    public int getRotatedWidth(int rotation) {
        return (rotation % 2 == 0) ? width : height;
    }

    /**
     * 回転後の高さを取得
     */
    public int getRotatedHeight(int rotation) {
        return (rotation % 2 == 0) ? height : width;
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * IDから形状を取得
     */
    public static ModuleShape fromId(String id) {
        for (ModuleShape shape : values()) {
            if (shape.id.equals(id)) {
                return shape;
            }
        }
        return SINGLE;
    }

    /**
     * 指定座標がこの形状に含まれるか（回転考慮）
     *
     * @param x チェックするX座標
     * @param y チェックするY座標
     * @param rotation 回転
     * @return 含まれる場合true
     */
    public boolean containsCell(int x, int y, int rotation) {
        int[][] rotatedCells = getRotatedCells(rotation);
        for (int[] cell : rotatedCells) {
            if (cell[0] == x && cell[1] == y) {
                return true;
            }
        }
        return false;
    }

    /**
     * 形状の文字列表現を取得（デバッグ用）
     */
    public String toAsciiArt(int rotation) {
        int[][] rotatedCells = getRotatedCells(rotation);
        int w = getRotatedWidth(rotation);
        int h = getRotatedHeight(rotation);

        char[][] grid = new char[h][w];
        for (int y = 0; y < h; y++) {
            Arrays.fill(grid[y], '.');
        }

        for (int[] cell : rotatedCells) {
            grid[cell[1]][cell[0]] = '#';
        }

        StringBuilder sb = new StringBuilder();
        for (char[] row : grid) {
            sb.append(new String(row)).append("\n");
        }
        return sb.toString();
    }
}
