package io.github.nazottix.anvil.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.nazottix.anvil.material.Grade;
import io.github.nazottix.anvil.processing.GradeCalculator;
import io.github.nazottix.anvil.processing.ProcessingLevel;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 加工素材データコンポーネント
 *
 * 加工チェーンにおける素材の状態を表します。
 * 各加工ステップを経るごとに、processingLevelが上昇します。
 *
 * 加工フロー:
 * RAW(0) → REFINED(1) → FORGED(2) → POLISHED(3) → PERFECT(4) → MASTERWORK(5)
 *
 * 例: 鉄の精錬済み素材
 * - materialId: "anvil:iron"
 * - processingLevel: 1 (REFINED)
 * - qualityBonus: 0.0
 *
 * 仕様書参照: docs/素材加工チェーン仕様.md
 *
 * @param materialId 素材ID（例: "anvil:iron", "anvil:diamond"）
 * @param processingLevel 加工レベル（0-5）
 * @param qualityBonus 品質ボーナス（0.0-1.0、研磨剤の品質で変動）
 */
public record ProcessedMaterialData(
        String materialId,
        int processingLevel,
        float qualityBonus
) {

    // ============================================
    // シリアライズ用Codec
    // ============================================

    /**
     * Codec - データの保存/読み込み用（NBT、JSON）
     */
    public static final Codec<ProcessedMaterialData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    // 素材ID（必須）
                    Codec.STRING.fieldOf("material_id").forGetter(ProcessedMaterialData::materialId),
                    // 加工レベル（デフォルト: 0 = RAW）
                    Codec.INT.optionalFieldOf("processing_level", 0).forGetter(ProcessedMaterialData::processingLevel),
                    // 品質ボーナス（デフォルト: 0.0）
                    Codec.FLOAT.optionalFieldOf("quality_bonus", 0.0f).forGetter(ProcessedMaterialData::qualityBonus)
            ).apply(instance, ProcessedMaterialData::new)
    );

    /**
     * StreamCodec - ネットワーク通信用
     */
    public static final StreamCodec<ByteBuf, ProcessedMaterialData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ProcessedMaterialData::materialId,
            ByteBufCodecs.INT, ProcessedMaterialData::processingLevel,
            ByteBufCodecs.FLOAT, ProcessedMaterialData::qualityBonus,
            ProcessedMaterialData::new
    );

    // ============================================
    // ファクトリメソッド
    // ============================================

    /**
     * 未加工（RAW）状態の素材データを作成
     *
     * @param materialId 素材ID
     * @return 新しいProcessedMaterialData
     */
    public static ProcessedMaterialData raw(String materialId) {
        return new ProcessedMaterialData(materialId, 0, 0.0f);
    }

    /**
     * 指定した加工レベルの素材データを作成
     *
     * @param materialId 素材ID
     * @param level 加工レベル
     * @return 新しいProcessedMaterialData
     */
    public static ProcessedMaterialData withLevel(String materialId, ProcessingLevel level) {
        return new ProcessedMaterialData(materialId, level.getLevel(), 0.0f);
    }

    // ============================================
    // ゲッター（派生値）
    // ============================================

    /**
     * 加工レベルEnumを取得
     *
     * @return ProcessingLevel
     */
    public ProcessingLevel getProcessingLevel() {
        return ProcessingLevel.fromLevel(processingLevel);
    }

    /**
     * 最終グレードを計算
     * 加工レベルと品質ボーナスから最終的なグレードを算出
     *
     * @return 計算されたグレード
     */
    public Grade calculateGrade() {
        return GradeCalculator.calculateGrade(processingLevel, qualityBonus);
    }

    /**
     * 基本グレードを取得（品質ボーナスなし）
     *
     * @return 基本グレード
     */
    public Grade getBaseGrade() {
        return getProcessingLevel().getBaseGrade();
    }

    // ============================================
    // 変換メソッド
    // ============================================

    /**
     * 次の加工レベルに進めた新しいインスタンスを返す
     *
     * @return 新しいProcessedMaterialData、または最大レベルの場合は自身
     */
    public ProcessedMaterialData upgrade() {
        ProcessingLevel current = getProcessingLevel();
        if (!current.hasNext()) {
            return this;
        }
        return new ProcessedMaterialData(materialId, current.getNext().getLevel(), qualityBonus);
    }

    /**
     * 品質ボーナスを追加した新しいインスタンスを返す
     *
     * @param additionalBonus 追加する品質ボーナス
     * @return 新しいProcessedMaterialData
     */
    public ProcessedMaterialData addQualityBonus(float additionalBonus) {
        // 品質ボーナスは0.0-1.0の範囲にクランプ
        float newBonus = Math.min(1.0f, Math.max(0.0f, qualityBonus + additionalBonus));
        return new ProcessedMaterialData(materialId, processingLevel, newBonus);
    }

    /**
     * 品質ボーナスを設定した新しいインスタンスを返す
     *
     * @param newBonus 新しい品質ボーナス（0.0-1.0）
     * @return 新しいProcessedMaterialData
     */
    public ProcessedMaterialData withQualityBonus(float newBonus) {
        // 品質ボーナスは0.0-1.0の範囲にクランプ
        float clampedBonus = Math.min(1.0f, Math.max(0.0f, newBonus));
        return new ProcessedMaterialData(materialId, processingLevel, clampedBonus);
    }

    // ============================================
    // 判定メソッド
    // ============================================

    /**
     * 次の加工レベルに進めるかチェック
     *
     * @return アップグレード可能な場合true
     */
    public boolean canUpgrade() {
        return getProcessingLevel().hasNext();
    }

    /**
     * 指定した加工レベル以上かチェック
     *
     * @param required 必要な加工レベル
     * @return 指定レベル以上の場合true
     */
    public boolean isAtLeast(ProcessingLevel required) {
        return getProcessingLevel().isAtLeast(required);
    }

    /**
     * パーツに変換可能かチェック
     * 最低でもREFINED以上の加工レベルが必要
     *
     * @return パーツ変換可能な場合true
     */
    public boolean canConvertToPart() {
        return isAtLeast(ProcessingLevel.REFINED);
    }

    // ============================================
    // 表示用メソッド
    // ============================================

    /**
     * ローカライズキーを取得（加工レベル）
     *
     * @return 翻訳キー
     */
    public String getProcessingLevelKey() {
        return getProcessingLevel().getTranslationKey();
    }

    /**
     * デバッグ用文字列表現
     *
     * @return デバッグ情報
     */
    @Override
    public String toString() {
        return String.format("ProcessedMaterialData{material=%s, level=%s(%d), quality=%.2f, grade=%s}",
                materialId,
                getProcessingLevel().getId(),
                processingLevel,
                qualityBonus,
                calculateGrade().getId());
    }
}
