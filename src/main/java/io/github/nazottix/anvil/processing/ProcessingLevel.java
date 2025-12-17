package io.github.nazottix.anvil.processing;

import io.github.nazottix.anvil.material.Grade;

/**
 * 素材加工レベル列挙型
 *
 * 素材の加工段階を定義します。
 * 加工レベルが高いほど、最終的なパーツのグレードが向上します。
 *
 * 加工フロー:
 * RAW(0) → REFINED(1) → FORGED(2) → POLISHED(3) → PERFECT(4) → MASTERWORK(5)
 */
public enum ProcessingLevel {

    /**
     * 未加工（原素材）
     * バニラのインゴットや素材アイテムの状態
     */
    RAW(0, "raw", "未加工", Grade.E),

    /**
     * 精錬済み
     * 精錬所で加工された状態
     */
    REFINED(1, "refined", "精錬済み", Grade.D),

    /**
     * 鍛造済み
     * 鍛造ステーションで加工された状態
     */
    FORGED(2, "forged", "鍛造済み", Grade.C),

    /**
     * 研磨済み
     * 研磨ステーションで加工された状態
     */
    POLISHED(3, "polished", "研磨済み", Grade.B),

    /**
     * 完璧
     * 高品質な研磨剤で仕上げた状態
     */
    PERFECT(4, "perfect", "完璧", Grade.A),

    /**
     * 極致
     * 最高品質の研磨剤で仕上げた最高の状態
     */
    MASTERWORK(5, "masterwork", "極致", Grade.S);

    // ============================================
    // フィールド
    // ============================================

    private final int level;
    private final String id;
    private final String displayNameJa;
    private final Grade baseGrade;

    // ============================================
    // コンストラクタ
    // ============================================

    ProcessingLevel(int level, String id, String displayNameJa, Grade baseGrade) {
        this.level = level;
        this.id = id;
        this.displayNameJa = displayNameJa;
        this.baseGrade = baseGrade;
    }

    // ============================================
    // ゲッター
    // ============================================

    /**
     * 加工レベル値を取得
     *
     * @return 加工レベル（0-5）
     */
    public int getLevel() {
        return level;
    }

    /**
     * IDを取得
     *
     * @return 加工レベルID
     */
    public String getId() {
        return id;
    }

    /**
     * 日本語表示名を取得
     *
     * @return 日本語名
     */
    public String getDisplayNameJa() {
        return displayNameJa;
    }

    /**
     * 基本グレードを取得
     * この加工レベルで得られる最低グレード
     *
     * @return 基本グレード
     */
    public Grade getBaseGrade() {
        return baseGrade;
    }

    /**
     * ローカライズキーを取得
     *
     * @return 翻訳キー
     */
    public String getTranslationKey() {
        return "processing.anvil." + id;
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * 次の加工レベルを取得
     *
     * @return 次のレベル、MASTERWORKの場合はnull
     */
    public ProcessingLevel getNext() {
        return switch (this) {
            case RAW -> REFINED;
            case REFINED -> FORGED;
            case FORGED -> POLISHED;
            case POLISHED -> PERFECT;
            case PERFECT -> MASTERWORK;
            case MASTERWORK -> null;
        };
    }

    /**
     * 次の加工レベルが存在するかチェック
     *
     * @return 次のレベルが存在する場合true
     */
    public boolean hasNext() {
        return this != MASTERWORK;
    }

    /**
     * 指定レベル以上かチェック
     *
     * @param other 比較対象
     * @return 指定レベル以上の場合true
     */
    public boolean isAtLeast(ProcessingLevel other) {
        return this.level >= other.level;
    }

    /**
     * レベル値からProcessingLevelを取得
     *
     * @param level レベル値（0-5）
     * @return 対応するProcessingLevel、見つからない場合はRAW
     */
    public static ProcessingLevel fromLevel(int level) {
        for (ProcessingLevel pl : values()) {
            if (pl.level == level) {
                return pl;
            }
        }
        return RAW;
    }

    /**
     * IDからProcessingLevelを取得
     *
     * @param id 加工レベルID
     * @return 対応するProcessingLevel、見つからない場合はRAW
     */
    public static ProcessingLevel fromId(String id) {
        for (ProcessingLevel pl : values()) {
            if (pl.id.equals(id)) {
                return pl;
            }
        }
        return RAW;
    }

    /**
     * 最大加工レベルを取得
     *
     * @return 最大レベル（MASTERWORK）
     */
    public static ProcessingLevel getMax() {
        return MASTERWORK;
    }

    /**
     * 最小加工レベルを取得
     *
     * @return 最小レベル（RAW）
     */
    public static ProcessingLevel getMin() {
        return RAW;
    }
}
