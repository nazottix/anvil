package io.github.nazottix.anvil.mod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MOD構成データコンポーネント
 *
 * ツールに装着されている全MODの構成を管理します。
 * オーラスロット、通常スロット、エクシルススロットを含みます。
 *
 * 仕様書参照: docs/05_容量制限システム.md
 */
public record ModConfiguration(
        // オーラスロット（1つ、必須）
        ModSlot auraSlot,
        // 通常MODスロット（最大8つ）
        List<ModSlot> normalSlots,
        // エクシルススロット（0〜1つ）
        @Nullable ModSlot exilusSlot,
        // 容量倍増器が適用されているか
        boolean hasCapacityDoubler,
        // 解放済み通常スロット数
        int unlockedSlots
) {

    // ============================================
    // 定数
    // ============================================

    /** 初期解放スロット数 */
    public static final int INITIAL_SLOTS = 3;

    /** 最大通常スロット数 */
    public static final int MAX_NORMAL_SLOTS = 8;

    /** デフォルト構成 */
    public static final ModConfiguration DEFAULT = new ModConfiguration(
            ModSlot.auraSlot(null),
            createInitialSlots(INITIAL_SLOTS),
            null,
            false,
            INITIAL_SLOTS
    );

    // ============================================
    // Codec
    // ============================================

    /**
     * 保存/読み込み用Codec
     */
    public static final Codec<ModConfiguration> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ModSlot.CODEC.fieldOf("aura_slot").forGetter(ModConfiguration::auraSlot),
                    ModSlot.CODEC.listOf().fieldOf("normal_slots").forGetter(ModConfiguration::normalSlots),
                    ModSlot.CODEC.optionalFieldOf("exilus_slot")
                            .xmap(opt -> opt.orElse(null), Optional::ofNullable)
                            .forGetter(ModConfiguration::exilusSlot),
                    Codec.BOOL.optionalFieldOf("has_capacity_doubler", false)
                            .forGetter(ModConfiguration::hasCapacityDoubler),
                    Codec.INT.optionalFieldOf("unlocked_slots", INITIAL_SLOTS)
                            .forGetter(ModConfiguration::unlockedSlots)
            ).apply(instance, ModConfiguration::new)
    );

    /**
     * ネットワーク通信用StreamCodec
     */
    public static final StreamCodec<ByteBuf, ModConfiguration> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ModConfiguration decode(ByteBuf buf) {
            ModSlot auraSlot = ModSlot.STREAM_CODEC.decode(buf);
            int slotCount = ByteBufCodecs.VAR_INT.decode(buf);
            List<ModSlot> normalSlots = new ArrayList<>(slotCount);
            for (int i = 0; i < slotCount; i++) {
                normalSlots.add(ModSlot.STREAM_CODEC.decode(buf));
            }
            boolean hasExilus = buf.readBoolean();
            ModSlot exilusSlot = hasExilus ? ModSlot.STREAM_CODEC.decode(buf) : null;
            boolean hasCapacityDoubler = buf.readBoolean();
            int unlockedSlots = ByteBufCodecs.VAR_INT.decode(buf);
            return new ModConfiguration(auraSlot, normalSlots, exilusSlot, hasCapacityDoubler, unlockedSlots);
        }

        @Override
        public void encode(ByteBuf buf, ModConfiguration config) {
            ModSlot.STREAM_CODEC.encode(buf, config.auraSlot());
            ByteBufCodecs.VAR_INT.encode(buf, config.normalSlots().size());
            for (ModSlot slot : config.normalSlots()) {
                ModSlot.STREAM_CODEC.encode(buf, slot);
            }
            buf.writeBoolean(config.exilusSlot() != null);
            if (config.exilusSlot() != null) {
                ModSlot.STREAM_CODEC.encode(buf, config.exilusSlot());
            }
            buf.writeBoolean(config.hasCapacityDoubler());
            ByteBufCodecs.VAR_INT.encode(buf, config.unlockedSlots());
        }
    };

    // ============================================
    // ファクトリメソッド
    // ============================================

    /**
     * 初期スロットリストを作成
     */
    private static List<ModSlot> createInitialSlots(int count) {
        List<ModSlot> slots = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            slots.add(ModSlot.empty(i));
        }
        return slots;
    }

    /**
     * 新しい構成を作成
     *
     * @param unlockedSlots 解放済みスロット数
     * @return 新しいModConfiguration
     */
    public static ModConfiguration create(int unlockedSlots) {
        return new ModConfiguration(
                ModSlot.auraSlot(null),
                createInitialSlots(Math.min(unlockedSlots, MAX_NORMAL_SLOTS)),
                null,
                false,
                unlockedSlots
        );
    }

    // ============================================
    // スロット管理
    // ============================================

    /**
     * オーラMODを設定した新しい構成を返す
     *
     * @param mod オーラMOD
     * @return 新しいModConfiguration
     */
    public ModConfiguration withAuraMod(InstalledMod mod) {
        return new ModConfiguration(
                auraSlot.withMod(mod),
                normalSlots,
                exilusSlot,
                hasCapacityDoubler,
                unlockedSlots
        );
    }

    /**
     * 通常スロットにMODを設定した新しい構成を返す
     *
     * @param slotIndex スロットインデックス
     * @param mod MOD（nullで取り外し）
     * @return 新しいModConfiguration
     */
    public ModConfiguration withNormalMod(int slotIndex, @Nullable InstalledMod mod) {
        if (slotIndex < 0 || slotIndex >= normalSlots.size()) {
            return this;
        }

        List<ModSlot> newSlots = new ArrayList<>(normalSlots);
        ModSlot slot = newSlots.get(slotIndex);
        newSlots.set(slotIndex, mod != null ? slot.withMod(mod) : slot.clearMod());

        return new ModConfiguration(auraSlot, newSlots, exilusSlot, hasCapacityDoubler, unlockedSlots);
    }

    /**
     * エクシルスMODを設定した新しい構成を返す
     *
     * @param mod エクシルスMOD
     * @return 新しいModConfiguration
     */
    public ModConfiguration withExilusMod(InstalledMod mod) {
        if (exilusSlot == null) {
            return this;
        }
        return new ModConfiguration(
                auraSlot,
                normalSlots,
                exilusSlot.withMod(mod),
                hasCapacityDoubler,
                unlockedSlots
        );
    }

    /**
     * スロットの極性を設定した新しい構成を返す（フォルマ使用時）
     *
     * @param slotIndex スロットインデックス
     * @param polarity 新しい極性
     * @return 新しいModConfiguration
     */
    public ModConfiguration withSlotPolarity(int slotIndex, Polarity polarity) {
        if (slotIndex < 0 || slotIndex >= normalSlots.size()) {
            return this;
        }

        List<ModSlot> newSlots = new ArrayList<>(normalSlots);
        newSlots.set(slotIndex, newSlots.get(slotIndex).withPolarity(polarity));

        return new ModConfiguration(auraSlot, newSlots, exilusSlot, hasCapacityDoubler, unlockedSlots);
    }

    /**
     * 容量倍増器を適用した新しい構成を返す
     *
     * @return 新しいModConfiguration
     */
    public ModConfiguration withCapacityDoubler() {
        if (hasCapacityDoubler) {
            return this;
        }
        return new ModConfiguration(auraSlot, normalSlots, exilusSlot, true, unlockedSlots);
    }

    /**
     * スロットを解放した新しい構成を返す
     *
     * @param newUnlockedCount 新しい解放数
     * @return 新しいModConfiguration
     */
    public ModConfiguration withUnlockedSlots(int newUnlockedCount) {
        int clampedCount = Math.min(newUnlockedCount, MAX_NORMAL_SLOTS);
        if (clampedCount <= unlockedSlots) {
            return this;
        }

        List<ModSlot> newSlots = new ArrayList<>(normalSlots);
        for (int i = normalSlots.size(); i < clampedCount; i++) {
            newSlots.add(ModSlot.empty(i));
        }

        return new ModConfiguration(auraSlot, newSlots, exilusSlot, hasCapacityDoubler, clampedCount);
    }

    /**
     * エクシルススロットを解放した新しい構成を返す
     *
     * @return 新しいModConfiguration
     */
    public ModConfiguration withExilusSlotUnlocked() {
        if (exilusSlot != null) {
            return this;
        }
        return new ModConfiguration(auraSlot, normalSlots, ModSlot.exilusSlot(), hasCapacityDoubler, unlockedSlots);
    }

    // ============================================
    // 情報取得
    // ============================================

    /**
     * 装着されている全MODを取得
     *
     * @return MODリスト
     */
    public List<InstalledMod> getAllInstalledMods() {
        List<InstalledMod> mods = new ArrayList<>();

        if (auraSlot.hasMod()) {
            mods.add(auraSlot.installedMod());
        }

        for (ModSlot slot : normalSlots) {
            if (slot.hasMod()) {
                mods.add(slot.installedMod());
            }
        }

        if (exilusSlot != null && exilusSlot.hasMod()) {
            mods.add(exilusSlot.installedMod());
        }

        return mods;
    }

    /**
     * 装着MOD数を取得
     *
     * @return MOD数
     */
    public int getInstalledModCount() {
        int count = 0;
        if (auraSlot.hasMod()) count++;
        for (ModSlot slot : normalSlots) {
            if (slot.hasMod()) count++;
        }
        if (exilusSlot != null && exilusSlot.hasMod()) count++;
        return count;
    }

    /**
     * 空きスロット数を取得
     *
     * @return 空きスロット数
     */
    public int getEmptySlotCount() {
        int count = 0;
        if (!auraSlot.hasMod()) count++;
        for (ModSlot slot : normalSlots) {
            if (!slot.hasMod()) count++;
        }
        if (exilusSlot != null && !exilusSlot.hasMod()) count++;
        return count;
    }

    /**
     * エクシルススロットが解放されているか
     */
    public boolean hasExilusSlot() {
        return exilusSlot != null;
    }
}
