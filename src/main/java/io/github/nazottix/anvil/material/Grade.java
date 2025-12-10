package io.github.nazottix.anvil.material;

import java.util.Random;

/**
 * グレード列挙型
 *
 * パーツの品質を表す9段階のグレードを定義します。
 * グレードが高いほどステータス乗数が大きくなりますが、出現確率は低くなります。
 *
 * 仕様書参照: docs/01_パーツ_素材システム.md - 3.5 グレードシステム
 */
public enum Grade {
    /**
     * Eグレード - 最低品質
     * ステータス乗数: 0.70x
     * 出現確率: 5%
     * 視覚効果: 灰色、くすんだ外観
     */
    E(
            "e",
            "E",
            0.70,
            0.05,
            0x808080,  // 灰色
            GradeVisualEffect.DULL
    ),

    /**
     * Dグレード
     * ステータス乗数: 0.85x
     * 出現確率: 15%
     * 視覚効果: 薄い色
     */
    D(
            "d",
            "D",
            0.85,
            0.15,
            0xA0A0A0,
            GradeVisualEffect.FADED
    ),

    /**
     * Cグレード - 標準品質
     * ステータス乗数: 1.00x
     * 出現確率: 30%
     * 視覚効果: 標準
     */
    C(
            "c",
            "C",
            1.00,
            0.30,
            0xFFFFFF,  // 白
            GradeVisualEffect.NORMAL
    ),

    /**
     * Bグレード
     * ステータス乗数: 1.10x
     * 出現確率: 25%
     * 視覚効果: やや光沢
     */
    B(
            "b",
            "B",
            1.10,
            0.25,
            0x90EE90,  // ライトグリーン
            GradeVisualEffect.SLIGHT_SHINE
    ),

    /**
     * Aグレード
     * ステータス乗数: 1.20x
     * 出現確率: 15%
     * 視覚効果: 光沢あり
     */
    A(
            "a",
            "A",
            1.20,
            0.15,
            0x00BFFF,  // ディープスカイブルー
            GradeVisualEffect.SHINE
    ),

    /**
     * Sグレード
     * ステータス乗数: 1.35x
     * 出現確率: 7%
     * 視覚効果: 強い光沢
     */
    S(
            "s",
            "S",
            1.35,
            0.07,
            0xFFD700,  // ゴールド
            GradeVisualEffect.STRONG_SHINE
    ),

    /**
     * SSグレード
     * ステータス乗数: 1.50x
     * 出現確率: 2.5%
     * 視覚効果: オーラ付き
     */
    SS(
            "ss",
            "SS",
            1.50,
            0.025,
            0xFF69B4,  // ホットピンク
            GradeVisualEffect.AURA
    ),

    /**
     * SSSグレード
     * ステータス乗数: 1.75x
     * 出現確率: 0.4%
     * 視覚効果: 強いオーラ
     */
    SSS(
            "sss",
            "SSS",
            1.75,
            0.004,
            0x9400D3,  // ダークバイオレット
            GradeVisualEffect.STRONG_AURA
    ),

    /**
     * MAXグレード - 最高品質
     * ステータス乗数: 2.00x
     * 出現確率: 0.1%
     * 視覚効果: 虹色オーラ
     */
    MAX(
            "max",
            "MAX",
            2.00,
            0.001,
            0xFFFFFF,  // 虹色は特殊処理
            GradeVisualEffect.RAINBOW_AURA
    );

    // グレードID
    private final String id;

    // 表示名
    private final String displayName;

    // ステータス乗数
    private final double statMultiplier;

    // 基本出現確率（0.0〜1.0）
    private final double baseProbability;

    // グレードを表す色（16進数RGB）
    private final int color;

    // 視覚効果タイプ
    private final GradeVisualEffect visualEffect;

    /**
     * Gradeコンストラクタ
     *
     * @param id グレードID
     * @param displayName 表示名
     * @param statMultiplier ステータス乗数
     * @param baseProbability 基本出現確率
     * @param color 表示色
     * @param visualEffect 視覚効果
     */
    Grade(String id, String displayName, double statMultiplier,
          double baseProbability, int color, GradeVisualEffect visualEffect) {
        this.id = id;
        this.displayName = displayName;
        this.statMultiplier = statMultiplier;
        this.baseProbability = baseProbability;
        this.color = color;
        this.visualEffect = visualEffect;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getStatMultiplier() {
        return statMultiplier;
    }

    public double getBaseProbability() {
        return baseProbability;
    }

    public int getColor() {
        return color;
    }

    public GradeVisualEffect getVisualEffect() {
        return visualEffect;
    }

    /**
     * ローカライズキーを取得
     * 例: "grade.anvil.s"
     */
    public String getTranslationKey() {
        return "grade.anvil." + id;
    }

    /**
     * このグレードが指定グレードより高いかチェック
     *
     * @param other 比較対象のグレード
     * @return このグレードが高い場合true
     */
    public boolean isHigherThan(Grade other) {
        return this.ordinal() > other.ordinal();
    }

    /**
     * 指定された値にグレード乗数を適用
     *
     * @param baseValue 基本値
     * @return グレード乗数を適用した値
     */
    public double applyMultiplier(double baseValue) {
        return baseValue * statMultiplier;
    }

    /**
     * 基本確率に基づいてランダムにグレードを決定
     *
     * @param random 乱数ジェネレータ
     * @return 決定されたグレード
     */
    public static Grade rollGrade(Random random) {
        double roll = random.nextDouble();
        double cumulative = 0.0;

        // 確率の低い順（MAX→E）でチェック
        Grade[] grades = values();
        for (int i = grades.length - 1; i >= 0; i--) {
            cumulative += grades[i].baseProbability;
            if (roll < cumulative) {
                return grades[i];
            }
        }

        // フォールバック（通常ここには到達しない）
        return C;
    }

    /**
     * 触媒を考慮してグレードを決定
     * 触媒によって高グレードの出現確率が上昇
     *
     * @param random 乱数ジェネレータ
     * @param catalystBonus 触媒による確率ボーナス（0.0〜1.0）
     * @return 決定されたグレード
     */
    public static Grade rollGradeWithCatalyst(Random random, double catalystBonus) {
        // 触媒ボーナスが高いほど、高グレードの確率が上がる
        // 実装: ロール値を下方修正することで高グレードが出やすくなる
        double roll = random.nextDouble() * (1.0 - catalystBonus * 0.5);
        double cumulative = 0.0;

        Grade[] grades = values();
        for (int i = grades.length - 1; i >= 0; i--) {
            cumulative += grades[i].baseProbability;
            if (roll < cumulative) {
                return grades[i];
            }
        }

        return C;
    }

    /**
     * IDからGradeを取得
     *
     * @param id グレードID
     * @return 対応するGrade、見つからない場合はC
     */
    public static Grade fromId(String id) {
        for (Grade grade : values()) {
            if (grade.id.equals(id)) {
                return grade;
            }
        }
        return C;
    }

    /**
     * グレードの視覚効果タイプ
     *
     * グレードに応じたアイテムの見た目の変化を定義
     */
    public enum GradeVisualEffect {
        DULL("dull", "くすみ"),              // 灰色がかった外観
        FADED("faded", "薄い"),              // 色が薄い
        NORMAL("normal", "標準"),            // 変化なし
        SLIGHT_SHINE("slight_shine", "微光沢"),  // わずかな光沢
        SHINE("shine", "光沢"),              // 光沢あり
        STRONG_SHINE("strong_shine", "強光沢"),  // 強い光沢
        AURA("aura", "オーラ"),              // オーラエフェクト
        STRONG_AURA("strong_aura", "強オーラ"), // 強いオーラ
        RAINBOW_AURA("rainbow_aura", "虹オーラ"); // 虹色オーラ

        private final String id;
        private final String displayNameJa;

        GradeVisualEffect(String id, String displayNameJa) {
            this.id = id;
            this.displayNameJa = displayNameJa;
        }

        public String getId() {
            return id;
        }

        public String getDisplayNameJa() {
            return displayNameJa;
        }
    }
}
