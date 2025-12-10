package io.github.nazottix.anvil.assembly;

import io.github.nazottix.anvil.data.component.ToolPart;
import io.github.nazottix.anvil.material.Material;
import io.github.nazottix.anvil.tool.ToolType;
import io.github.nazottix.anvil.trait.Trait;

import java.util.List;
import java.util.Map;

/**
 * ツール組み立て結果
 *
 * パーツからツールを組み立てた結果を保持します。
 * 成功/失敗の状態、計算されたステータス、特性リストを含みます。
 *
 * 仕様書参照: docs/01_パーツ_素材システム.md
 */
public record AssemblyResult(
        // 組み立て成功したかどうか
        boolean success,
        // エラーメッセージ（失敗時）
        String errorMessage,
        // ツールタイプ
        ToolType toolType,
        // パーツリスト
        List<ToolPart> parts,
        // 計算されたステータス
        CalculatedStats stats,
        // 集約された特性（特性ID → レベル）
        Map<String, Integer> traits,
        // 主要素材（色や名前に使用）
        Material primaryMaterial
) {
    /**
     * 成功結果を作成
     */
    public static AssemblyResult success(
            ToolType toolType,
            List<ToolPart> parts,
            CalculatedStats stats,
            Map<String, Integer> traits,
            Material primaryMaterial
    ) {
        return new AssemblyResult(true, null, toolType, parts, stats, traits, primaryMaterial);
    }

    /**
     * 失敗結果を作成
     */
    public static AssemblyResult failure(String errorMessage) {
        return new AssemblyResult(false, errorMessage, null, List.of(), CalculatedStats.DEFAULT, Map.of(), null);
    }

    /**
     * 失敗結果を作成（ツールタイプ情報付き）
     */
    public static AssemblyResult failure(ToolType toolType, String errorMessage) {
        return new AssemblyResult(false, errorMessage, toolType, List.of(), CalculatedStats.DEFAULT, Map.of(), null);
    }

    /**
     * 結果が成功かどうか
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 結果が失敗かどうか
     */
    public boolean isFailure() {
        return !success;
    }

    /**
     * 特定の特性を持っているかチェック
     *
     * @param traitId 特性ID
     * @return 持っている場合true
     */
    public boolean hasTrait(String traitId) {
        return traits.containsKey(traitId);
    }

    /**
     * 特性のレベルを取得
     *
     * @param traitId 特性ID
     * @return 特性レベル（持っていない場合は0）
     */
    public int getTraitLevel(String traitId) {
        return traits.getOrDefault(traitId, 0);
    }

    /**
     * 総特性数を取得
     */
    public int getTotalTraitCount() {
        return traits.size();
    }

    /**
     * エラーメッセージのローカライズキーを取得
     */
    public String getErrorTranslationKey() {
        if (errorMessage == null) {
            return null;
        }
        return "assembly.anvil.error." + errorMessage;
    }
}
