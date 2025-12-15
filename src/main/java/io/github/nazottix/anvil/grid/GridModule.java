package io.github.nazottix.anvil.grid;

import io.github.nazottix.anvil.mod.ModEffect;
import io.github.nazottix.anvil.mod.ModRarity;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;

/**
 * グリッドモジュール定義
 *
 * コアボックスに配置できるモジュールを定義します。
 *
 * 仕様書参照: docs/04_グリッド配置システム.md
 */
public record GridModule(
        // モジュールID
        ResourceLocation id,
        // モジュール形状
        ModuleShape shape,
        // モジュールカテゴリ
        ModuleCategory category,
        // レアリティ
        ModRarity rarity,
        // 重量
        int weight,
        // 効果リスト
        List<ModEffect> effects
) {

    // ============================================
    // ビルダー
    // ============================================

    public static class Builder {
        private ResourceLocation id;
        private ModuleShape shape = ModuleShape.SINGLE;
        private ModuleCategory category = ModuleCategory.UTILITY;
        private ModRarity rarity = ModRarity.COMMON;
        private int weight = 1;
        private List<ModEffect> effects = List.of();

        public Builder(ResourceLocation id) {
            this.id = id;
        }

        public Builder(String namespace, String path) {
            this.id = ResourceLocation.fromNamespaceAndPath(namespace, path);
        }

        public Builder shape(ModuleShape shape) {
            this.shape = shape;
            return this;
        }

        public Builder category(ModuleCategory category) {
            this.category = category;
            return this;
        }

        public Builder rarity(ModRarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public Builder weight(int weight) {
            this.weight = weight;
            return this;
        }

        public Builder effects(List<ModEffect> effects) {
            this.effects = effects;
            return this;
        }

        public Builder effects(ModEffect... effects) {
            this.effects = List.of(effects);
            return this;
        }

        public GridModule build() {
            return new GridModule(id, shape, category, rarity, weight, effects);
        }
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * 指定位置に配置可能かチェック
     *
     * @param gridSize グリッドサイズ
     * @param x 配置X座標
     * @param y 配置Y座標
     * @param rotation 回転
     * @param occupiedCells 既に占有されているセル
     * @return 配置可能な場合true
     */
    public boolean canPlaceAt(int gridSize, int x, int y, int rotation, Set<Long> occupiedCells) {
        int[][] cells = shape.getRotatedCells(rotation);

        for (int[] cell : cells) {
            int cellX = x + cell[0];
            int cellY = y + cell[1];

            // グリッド範囲外
            if (cellX < 0 || cellX >= gridSize || cellY < 0 || cellY >= gridSize) {
                return false;
            }

            // 既に占有されている
            long key = ((long) cellX << 32) | (cellY & 0xFFFFFFFFL);
            if (occupiedCells.contains(key)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 配置時に占有するセルの座標を取得
     *
     * @param x 配置X座標
     * @param y 配置Y座標
     * @param rotation 回転
     * @return 占有セルのリスト（[x, y]の配列）
     */
    public int[][] getOccupiedCells(int x, int y, int rotation) {
        int[][] baseCells = shape.getRotatedCells(rotation);
        int[][] result = new int[baseCells.length][2];

        for (int i = 0; i < baseCells.length; i++) {
            result[i][0] = x + baseCells[i][0];
            result[i][1] = y + baseCells[i][1];
        }

        return result;
    }
}
