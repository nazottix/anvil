package io.github.nazottix.anvil.processing;

import io.github.nazottix.anvil.material.Grade;

/**
 * グレード計算ユーティリティ
 *
 * 加工レベルと品質ボーナスから最終グレードを計算します。
 *
 * 計算ロジック:
 * 1. 加工レベルに対応する基本グレードを取得
 * 2. 品質ボーナスに応じてグレードをアップグレード
 *
 * グレードマッピング:
 * - RAW(0) → E
 * - REFINED(1) → D
 * - FORGED(2) → C
 * - POLISHED(3) → B
 * - PERFECT(4) → A
 * - MASTERWORK(5) → S
 *
 * 品質ボーナス効果:
 * - 0.0-0.33: +0グレード
 * - 0.34-0.66: +1グレード
 * - 0.67-1.0: +2グレード
 */
public final class GradeCalculator {

    // インスタンス化を防止
    private GradeCalculator() {
        throw new UnsupportedOperationException("GradeCalculatorはユーティリティクラスです");
    }

    // ============================================
    // 定数
    // ============================================

    /**
     * 品質ボーナスによる最大グレードアップ数
     */
    public static final int MAX_QUALITY_UPGRADE = 2;

    /**
     * 品質ボーナスの閾値（1グレードアップに必要なボーナス）
     */
    public static final float QUALITY_THRESHOLD_PER_GRADE = 0.34f;

    // ============================================
    // メイン計算メソッド
    // ============================================

    /**
     * 加工レベルと品質ボーナスから最終グレードを計算
     *
     * @param processingLevel 加工レベル（0-5）
     * @param qualityBonus 品質ボーナス（0.0-1.0）
     * @return 計算されたグレード
     */
    public static Grade calculateGrade(int processingLevel, float qualityBonus) {
        // 加工レベルから基本グレードを取得
        Grade baseGrade = getBaseGrade(processingLevel);

        // 品質ボーナスからアップグレード段数を計算
        int upgradeSteps = calculateQualityUpgrade(qualityBonus);

        // グレードをアップグレード
        return upgradeGrade(baseGrade, upgradeSteps);
    }

    /**
     * ProcessingLevelから最終グレードを計算
     *
     * @param level 加工レベルEnum
     * @param qualityBonus 品質ボーナス（0.0-1.0）
     * @return 計算されたグレード
     */
    public static Grade calculateGrade(ProcessingLevel level, float qualityBonus) {
        return calculateGrade(level.getLevel(), qualityBonus);
    }

    // ============================================
    // 基本グレード取得
    // ============================================

    /**
     * 加工レベルから基本グレードを取得
     *
     * @param processingLevel 加工レベル（0-5）
     * @return 基本グレード
     */
    public static Grade getBaseGrade(int processingLevel) {
        ProcessingLevel level = ProcessingLevel.fromLevel(processingLevel);
        return level.getBaseGrade();
    }

    // ============================================
    // 品質ボーナス計算
    // ============================================

    /**
     * 品質ボーナスからグレードアップグレード段数を計算
     *
     * @param qualityBonus 品質ボーナス（0.0-1.0）
     * @return アップグレード段数（0-MAX_QUALITY_UPGRADE）
     */
    public static int calculateQualityUpgrade(float qualityBonus) {
        // 品質ボーナスを0.0-1.0にクランプ
        float clampedBonus = Math.min(1.0f, Math.max(0.0f, qualityBonus));

        // 閾値に基づいてアップグレード段数を計算
        int steps = (int) (clampedBonus / QUALITY_THRESHOLD_PER_GRADE);

        // 最大アップグレード数で制限
        return Math.min(steps, MAX_QUALITY_UPGRADE);
    }

    // ============================================
    // グレードアップグレード
    // ============================================

    /**
     * グレードを指定段数アップグレード
     *
     * @param baseGrade 基本グレード
     * @param steps アップグレード段数
     * @return アップグレード後のグレード（最大MAXで制限）
     */
    public static Grade upgradeGrade(Grade baseGrade, int steps) {
        if (steps <= 0) {
            return baseGrade;
        }

        Grade[] grades = Grade.values();
        int currentIndex = baseGrade.ordinal();
        int newIndex = Math.min(currentIndex + steps, grades.length - 1);

        return grades[newIndex];
    }

    /**
     * グレードを1段階アップグレード
     *
     * @param grade 現在のグレード
     * @return 次のグレード（MAXの場合はMAXのまま）
     */
    public static Grade upgradeGradeByOne(Grade grade) {
        return upgradeGrade(grade, 1);
    }

    // ============================================
    // グレードダウングレード
    // ============================================

    /**
     * グレードを指定段数ダウングレード
     *
     * @param baseGrade 基本グレード
     * @param steps ダウングレード段数
     * @return ダウングレード後のグレード（最低Eで制限）
     */
    public static Grade downgradeGrade(Grade baseGrade, int steps) {
        if (steps <= 0) {
            return baseGrade;
        }

        Grade[] grades = Grade.values();
        int currentIndex = baseGrade.ordinal();
        int newIndex = Math.max(currentIndex - steps, 0);

        return grades[newIndex];
    }

    // ============================================
    // ユーティリティメソッド
    // ============================================

    /**
     * 2つのグレードの差を計算
     *
     * @param higher 高い方のグレード
     * @param lower 低い方のグレード
     * @return グレードの差（higher - lower）
     */
    public static int gradeDifference(Grade higher, Grade lower) {
        return higher.ordinal() - lower.ordinal();
    }

    /**
     * 指定した加工レベルで達成可能な最大グレードを取得
     *
     * @param processingLevel 加工レベル
     * @return 最大グレード（基本グレード + MAX_QUALITY_UPGRADE）
     */
    public static Grade getMaxAchievableGrade(int processingLevel) {
        return calculateGrade(processingLevel, 1.0f);
    }

    /**
     * 指定した加工レベルで達成可能な最大グレードを取得
     *
     * @param level 加工レベルEnum
     * @return 最大グレード
     */
    public static Grade getMaxAchievableGrade(ProcessingLevel level) {
        return getMaxAchievableGrade(level.getLevel());
    }

    /**
     * 特定のグレードを達成するために必要な最低加工レベルを計算
     *
     * @param targetGrade 目標グレード
     * @return 必要な最低加工レベル（達成不可能な場合はMASTERWORK）
     */
    public static ProcessingLevel getMinimumProcessingLevel(Grade targetGrade) {
        // 各加工レベルの最大達成グレードをチェック
        for (ProcessingLevel level : ProcessingLevel.values()) {
            Grade maxGrade = getMaxAchievableGrade(level);
            if (maxGrade.ordinal() >= targetGrade.ordinal()) {
                return level;
            }
        }
        return ProcessingLevel.MASTERWORK;
    }

    /**
     * 特定のグレードを確実に達成するために必要な加工レベルを計算
     * （品質ボーナスなしで達成可能なレベル）
     *
     * @param targetGrade 目標グレード
     * @return 必要な加工レベル（達成不可能な場合はnull）
     */
    public static ProcessingLevel getGuaranteedProcessingLevel(Grade targetGrade) {
        // 各加工レベルの基本グレードをチェック
        for (ProcessingLevel level : ProcessingLevel.values()) {
            if (level.getBaseGrade().ordinal() >= targetGrade.ordinal()) {
                return level;
            }
        }
        return null;
    }
}
