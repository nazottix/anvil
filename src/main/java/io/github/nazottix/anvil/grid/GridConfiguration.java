package io.github.nazottix.anvil.grid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * グリッド構成データコンポーネント
 *
 * ツールのコアボックス配置状態を保存します。
 *
 * 仕様書参照: docs/04_グリッド配置システム.md
 */
public record GridConfiguration(
        // コアボックスサイズ
        CoreBoxSize size,
        // 配置されたモジュールのリスト
        List<PlacedModule> placedModules
) {

    // ============================================
    // デフォルト
    // ============================================

    public static final GridConfiguration DEFAULT = new GridConfiguration(
            CoreBoxSize.TINY,
            List.of()
    );

    // ============================================
    // Codec
    // ============================================

    public static final Codec<GridConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("size", "tiny")
                            .xmap(CoreBoxSize::fromId, CoreBoxSize::getId)
                            .forGetter(GridConfiguration::size),
                    PlacedModule.CODEC.listOf().optionalFieldOf("placed_modules", List.of())
                            .forGetter(GridConfiguration::placedModules)
            ).apply(instance, GridConfiguration::new)
    );

    public static final StreamCodec<ByteBuf, GridConfiguration> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public GridConfiguration decode(ByteBuf buf) {
            String sizeId = ByteBufCodecs.STRING_UTF8.decode(buf);
            CoreBoxSize size = CoreBoxSize.fromId(sizeId);
            int count = ByteBufCodecs.VAR_INT.decode(buf);
            List<PlacedModule> modules = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                modules.add(PlacedModule.STREAM_CODEC.decode(buf));
            }
            return new GridConfiguration(size, modules);
        }

        @Override
        public void encode(ByteBuf buf, GridConfiguration config) {
            ByteBufCodecs.STRING_UTF8.encode(buf, config.size().getId());
            ByteBufCodecs.VAR_INT.encode(buf, config.placedModules().size());
            for (PlacedModule module : config.placedModules()) {
                PlacedModule.STREAM_CODEC.encode(buf, module);
            }
        }
    };

    // ============================================
    // モジュール管理
    // ============================================

    /**
     * モジュールを追加した新しい構成を返す
     *
     * @param module 追加するモジュール
     * @return 新しいGridConfiguration
     */
    public GridConfiguration withAddedModule(PlacedModule module) {
        List<PlacedModule> newModules = new ArrayList<>(placedModules);
        newModules.add(module);
        return new GridConfiguration(this.size, newModules);
    }

    /**
     * モジュールを削除した新しい構成を返す
     *
     * @param index 削除するインデックス
     * @return 新しいGridConfiguration
     */
    public GridConfiguration withRemovedModule(int index) {
        if (index < 0 || index >= placedModules.size()) {
            return this;
        }
        List<PlacedModule> newModules = new ArrayList<>(placedModules);
        newModules.remove(index);
        return new GridConfiguration(this.size, newModules);
    }

    /**
     * モジュールを更新した新しい構成を返す
     *
     * @param index 更新するインデックス
     * @param module 新しいモジュール
     * @return 新しいGridConfiguration
     */
    public GridConfiguration withUpdatedModule(int index, PlacedModule module) {
        if (index < 0 || index >= placedModules.size()) {
            return this;
        }
        List<PlacedModule> newModules = new ArrayList<>(placedModules);
        newModules.set(index, module);
        return new GridConfiguration(this.size, newModules);
    }

    /**
     * サイズを変更した新しい構成を返す
     *
     * @param newSize 新しいサイズ
     * @return 新しいGridConfiguration
     */
    public GridConfiguration withSize(CoreBoxSize newSize) {
        // サイズが小さくなった場合、範囲外のモジュールを削除
        List<PlacedModule> validModules = new ArrayList<>();
        int gridSize = newSize.getGridSize();

        for (PlacedModule module : placedModules) {
            GridModule def = GridModuleRegistry.get(module.moduleId());
            if (def != null) {
                int[][] cells = def.shape().getRotatedCells(module.rotation());
                boolean valid = true;
                for (int[] cell : cells) {
                    int cellX = module.x() + cell[0];
                    int cellY = module.y() + cell[1];
                    if (cellX >= gridSize || cellY >= gridSize) {
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    validModules.add(module);
                }
            }
        }

        return new GridConfiguration(newSize, validModules);
    }

    /**
     * 全モジュールをクリアした新しい構成を返す
     */
    public GridConfiguration clear() {
        return new GridConfiguration(this.size, List.of());
    }

    // ============================================
    // 情報取得
    // ============================================

    /**
     * 占有されているセルのセットを取得
     *
     * @return 占有セルのセット（キー = (x << 32) | y）
     */
    public Set<Long> getOccupiedCells() {
        Set<Long> occupied = new HashSet<>();

        for (PlacedModule placed : placedModules) {
            GridModule def = GridModuleRegistry.get(placed.moduleId());
            if (def != null) {
                int[][] cells = def.getOccupiedCells(placed.x(), placed.y(), placed.rotation());
                for (int[] cell : cells) {
                    long key = ((long) cell[0] << 32) | (cell[1] & 0xFFFFFFFFL);
                    occupied.add(key);
                }
            }
        }

        return occupied;
    }

    /**
     * 指定座標にモジュールを配置可能かチェック
     *
     * @param module モジュール定義
     * @param x X座標
     * @param y Y座標
     * @param rotation 回転
     * @return 配置可能な場合true
     */
    public boolean canPlace(GridModule module, int x, int y, int rotation) {
        return module.canPlaceAt(size.getGridSize(), x, y, rotation, getOccupiedCells());
    }

    /**
     * 総重量を計算
     */
    public int getTotalWeight() {
        int total = 0;
        for (PlacedModule placed : placedModules) {
            GridModule def = GridModuleRegistry.get(placed.moduleId());
            if (def != null) {
                total += def.weight();
            }
        }
        return total;
    }

    /**
     * 使用セル数を取得
     */
    public int getUsedCellCount() {
        return getOccupiedCells().size();
    }

    /**
     * 空きセル数を取得
     */
    public int getFreeCellCount() {
        return size.getTotalCells() - getUsedCellCount();
    }

    /**
     * 配置モジュール数を取得
     */
    public int getModuleCount() {
        return placedModules.size();
    }
}
