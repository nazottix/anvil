package io.github.nazottix.anvil.mod;

import io.github.nazottix.anvil.Config;

/**
 * 容量計算クラス
 *
 * ツールのMOD容量を計算します。
 *
 * 計算式:
 * 有効容量 = (ベース容量 + レベルボーナス) × 容量倍増器 + オーラボーナス
 *
 * 仕様書参照: docs/05_容量制限システム.md
 */
public final class CapacityCalculator {

    // ============================================
    // 定数
    // ============================================

    /** ベース容量 */
    public static final int BASE_CAPACITY = 30;

    /** 容量倍増器の倍率 */
    public static final int CAPACITY_DOUBLER_MULTIPLIER = 2;

    // ============================================
    // レベルボーナス計算
    // ============================================

    /**
     * レベルに応じた容量ボーナスを計算
     *
     * | レベル範囲 | ボーナス | 累計（範囲終了時） |
     * |-----------|---------|-------------------|
     * | 1-100     | +1/10レベル | +10            |
     * | 101-1,000 | +1/50レベル | +28            |
     * | 1,001-5,000 | +1/100レベル | +68        |
     * | 5,001-10,000 | +1/200レベル | +93      |
     *
     * @param level ツールレベル
     * @return レベルボーナス
     */
    public static int calculateLevelBonus(int level) {
        if (level <= 0) {
            return 0;
        }

        int bonus = 0;

        // 1-100: +1/10レベル
        int early = Math.min(level, 100);
        bonus += early / 10;

        // 101-1000: +1/50レベル
        if (level > 100) {
            int mid = Math.min(level, 1000) - 100;
            bonus += mid / 50;
        }

        // 1001-5000: +1/100レベル
        if (level > 1000) {
            int late = Math.min(level, 5000) - 1000;
            bonus += late / 100;
        }

        // 5001-10000: +1/200レベル
        if (level > 5000) {
            int endGame = Math.min(level, 10000) - 5000;
            bonus += endGame / 200;
        }

        return bonus;
    }

    // ============================================
    // オーラボーナス計算
    // ============================================

    /**
     * オーラMODによる容量ボーナスを計算
     *
     * オーラMODは容量を追加する（消費しない）
     * 極性が一致すると追加容量が2倍になる
     *
     * @param auraSlot オーラスロット
     * @param auraDef オーラMOD定義（null = オーラなし）
     * @return オーラボーナス
     */
    public static int calculateAuraBonus(ModSlot auraSlot, ModDefinition auraDef) {
        if (auraSlot == null || !auraSlot.hasMod() || auraDef == null) {
            return 0;
        }

        InstalledMod auraMod = auraSlot.installedMod();
        int baseBonus = auraDef.baseDrain(); // オーラのdrainは追加容量として機能

        // 極性マッチでボーナス2倍
        if (auraSlot.polarity() != null && auraSlot.polarity() == auraDef.polarity()) {
            return baseBonus * 2;
        }

        return baseBonus;
    }

    // ============================================
    // 総容量計算
    // ============================================

    /**
     * ツールの有効容量を計算
     *
     * @param level ツールレベル
     * @param hasCapacityDoubler 容量倍増器が適用されているか
     * @param auraSlot オーラスロット
     * @param auraDef オーラMOD定義
     * @return 有効容量
     */
    public static int calculateTotalCapacity(
            int level,
            boolean hasCapacityDoubler,
            ModSlot auraSlot,
            ModDefinition auraDef
    ) {
        // ベース容量 + レベルボーナス
        int baseTotal = BASE_CAPACITY + calculateLevelBonus(level);

        // 容量倍増器
        if (hasCapacityDoubler) {
            baseTotal *= CAPACITY_DOUBLER_MULTIPLIER;
        }

        // オーラボーナス
        int auraBonus = calculateAuraBonus(auraSlot, auraDef);

        return baseTotal + auraBonus;
    }

    /**
     * ツールの有効容量を計算（簡易版）
     *
     * @param level ツールレベル
     * @param config MOD構成
     * @return 有効容量
     */
    public static int calculateTotalCapacity(int level, ModConfiguration config) {
        // TODO: オーラMOD定義をレジストリから取得
        return calculateTotalCapacity(level, config.hasCapacityDoubler(), config.auraSlot(), null);
    }

    // ============================================
    // 使用容量計算
    // ============================================

    /**
     * 現在使用中の容量を計算
     *
     * @param config MOD構成
     * @param modLookup MOD定義を取得する関数
     * @return 使用容量
     */
    public static int calculateUsedCapacity(
            ModConfiguration config,
            java.util.function.Function<InstalledMod, ModDefinition> modLookup
    ) {
        int used = 0;

        // 通常スロットのMOD
        for (ModSlot slot : config.normalSlots()) {
            if (slot.hasMod()) {
                InstalledMod mod = slot.installedMod();
                ModDefinition def = modLookup.apply(mod);
                if (def != null) {
                    int drain = def.getDrainAtRank(mod.rank());
                    // 極性計算
                    used += Polarity.calculateDrain(drain, def.polarity(), slot.polarity());
                }
            }
        }

        // エクシルスMOD
        if (config.exilusSlot() != null && config.exilusSlot().hasMod()) {
            InstalledMod mod = config.exilusSlot().installedMod();
            ModDefinition def = modLookup.apply(mod);
            if (def != null) {
                int drain = def.getDrainAtRank(mod.rank());
                ModSlot exSlot = config.exilusSlot();
                used += Polarity.calculateDrain(drain, def.polarity(), exSlot.polarity());
            }
        }

        // オーラは容量を消費しない（追加する）

        return used;
    }

    // ============================================
    // 残り容量計算
    // ============================================

    /**
     * 残り容量を計算
     *
     * @param totalCapacity 総容量
     * @param usedCapacity 使用容量
     * @return 残り容量（負の値 = 容量超過）
     */
    public static int calculateRemainingCapacity(int totalCapacity, int usedCapacity) {
        return totalCapacity - usedCapacity;
    }

    /**
     * 容量超過しているか
     *
     * @param totalCapacity 総容量
     * @param usedCapacity 使用容量
     * @return 超過している場合true
     */
    public static boolean isOverCapacity(int totalCapacity, int usedCapacity) {
        return usedCapacity > totalCapacity;
    }

    // ============================================
    // スロット解放レベル
    // ============================================

    /**
     * 指定レベルで解放されるスロット数を取得
     *
     * | レベル | スロット |
     * |--------|---------|
     * | 1      | 3       |
     * | 100    | 4       |
     * | 500    | 5       |
     * | 1000   | 6       |
     * | 2500   | 7       |
     * | 5000   | 8       |
     *
     * @param level ツールレベル
     * @return 解放スロット数
     */
    public static int getUnlockedSlotsForLevel(int level) {
        if (level >= 5000) return 8;
        if (level >= 2500) return 7;
        if (level >= 1000) return 6;
        if (level >= 500) return 5;
        if (level >= 100) return 4;
        return 3;
    }

    /**
     * エクシルススロットが解放されているか
     *
     * 条件: Lv.3000 + エクシルスアダプター
     *
     * @param level ツールレベル
     * @param hasAdapter エクシルスアダプターを持っているか
     * @return 解放されている場合true
     */
    public static boolean isExilusSlotUnlocked(int level, boolean hasAdapter) {
        return level >= 3000 && hasAdapter;
    }

    // コンストラクタを非公開
    private CapacityCalculator() {}
}
