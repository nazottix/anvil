package io.github.nazottix.anvil.item.respec;

/**
 * リスペックタイプ
 *
 * リスペックアイテムの種類を定義します。
 *
 * 仕様書参照: docs/08_入手_リスペックシステム.md
 */
public enum RespecType {
    /**
     * 部分リスペック（10ポイント）
     * 記憶の断片で実行
     */
    PARTIAL(10, "partial", 0x55AAFF),

    /**
     * 完全リスペック
     * 記憶の結晶で実行
     */
    FULL(-1, "full", 0xFFAA55),

    /**
     * キーストーンのみリセット
     * 忘却のオーブで実行
     */
    KEYSTONE_ONLY(0, "keystone", 0xFF55FF);

    private final int pointsToRefund;
    private final String id;
    private final int color;

    RespecType(int pointsToRefund, String id, int color) {
        this.pointsToRefund = pointsToRefund;
        this.id = id;
        this.color = color;
    }

    /**
     * リスペックするポイント数を取得
     * -1の場合は全ポイント
     */
    public int getPointsToRefund() {
        return pointsToRefund;
    }

    /**
     * ID文字列を取得
     */
    public String getId() {
        return id;
    }

    /**
     * 表示色を取得
     */
    public int getColor() {
        return color;
    }

    /**
     * 完全リスペックかどうか
     */
    public boolean isFullRespec() {
        return this == FULL;
    }
}
