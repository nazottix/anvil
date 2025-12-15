package io.github.nazottix.anvil.mod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * MODスロットの定義
 *
 * ツールの各MODスロットの状態（極性、装着MOD）を管理します。
 */
public record ModSlot(
        // スロットのインデックス
        int index,
        // スロットの極性（null = 極性なし）
        @Nullable Polarity polarity,
        // 装着されているMOD（null = 空）
        @Nullable InstalledMod installedMod,
        // スロットタイプ
        SlotType slotType
) {

    // ============================================
    // スロットタイプ
    // ============================================

    /**
     * スロットの種類
     */
    public enum SlotType {
        /** 通常スロット */
        NORMAL("normal"),
        /** オーラスロット（容量を追加） */
        AURA("aura"),
        /** エクシルススロット（ユーティリティ専用） */
        EXILUS("exilus");

        private final String id;

        SlotType(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static SlotType fromId(String id) {
            for (SlotType type : values()) {
                if (type.id.equals(id)) {
                    return type;
                }
            }
            return NORMAL;
        }
    }

    // ============================================
    // Codec
    // ============================================

    /**
     * 保存/読み込み用Codec
     */
    public static final Codec<ModSlot> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("index").forGetter(ModSlot::index),
                    Codec.STRING.optionalFieldOf("polarity", "")
                            .xmap(s -> s.isEmpty() ? null : Polarity.fromId(s),
                                    p -> p == null ? "" : p.getId())
                            .forGetter(ModSlot::polarity),
                    InstalledMod.CODEC.optionalFieldOf("installed_mod")
                            .xmap(opt -> opt.orElse(null), Optional::ofNullable)
                            .forGetter(ModSlot::installedMod),
                    Codec.STRING.optionalFieldOf("slot_type", "normal")
                            .xmap(SlotType::fromId, SlotType::getId)
                            .forGetter(ModSlot::slotType)
            ).apply(instance, ModSlot::new)
    );

    /**
     * ネットワーク通信用StreamCodec
     */
    public static final StreamCodec<ByteBuf, ModSlot> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ModSlot decode(ByteBuf buf) {
            int index = ByteBufCodecs.VAR_INT.decode(buf);
            String polarityId = ByteBufCodecs.STRING_UTF8.decode(buf);
            Polarity polarity = polarityId.isEmpty() ? null : Polarity.fromId(polarityId);
            boolean hasMod = buf.readBoolean();
            InstalledMod mod = hasMod ? InstalledMod.STREAM_CODEC.decode(buf) : null;
            String slotTypeId = ByteBufCodecs.STRING_UTF8.decode(buf);
            SlotType slotType = SlotType.fromId(slotTypeId);
            return new ModSlot(index, polarity, mod, slotType);
        }

        @Override
        public void encode(ByteBuf buf, ModSlot slot) {
            ByteBufCodecs.VAR_INT.encode(buf, slot.index());
            ByteBufCodecs.STRING_UTF8.encode(buf, slot.polarity() == null ? "" : slot.polarity().getId());
            buf.writeBoolean(slot.installedMod() != null);
            if (slot.installedMod() != null) {
                InstalledMod.STREAM_CODEC.encode(buf, slot.installedMod());
            }
            ByteBufCodecs.STRING_UTF8.encode(buf, slot.slotType().getId());
        }
    };

    // ============================================
    // ファクトリメソッド
    // ============================================

    /**
     * 空の通常スロットを作成
     *
     * @param index スロットインデックス
     * @return 新しいModSlot
     */
    public static ModSlot empty(int index) {
        return new ModSlot(index, null, null, SlotType.NORMAL);
    }

    /**
     * 極性付きの空スロットを作成
     *
     * @param index スロットインデックス
     * @param polarity 極性
     * @return 新しいModSlot
     */
    public static ModSlot withPolarity(int index, Polarity polarity) {
        return new ModSlot(index, polarity, null, SlotType.NORMAL);
    }

    /**
     * オーラスロットを作成
     *
     * @param polarity 極性（オーラスロットは通常極性を持つ）
     * @return 新しいModSlot
     */
    public static ModSlot auraSlot(@Nullable Polarity polarity) {
        return new ModSlot(0, polarity, null, SlotType.AURA);
    }

    /**
     * エクシルススロットを作成
     *
     * @return 新しいModSlot
     */
    public static ModSlot exilusSlot() {
        return new ModSlot(0, null, null, SlotType.EXILUS);
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * スロットが空かどうか
     */
    public boolean isEmpty() {
        return installedMod == null;
    }

    /**
     * スロットにMODが装着されているか
     */
    public boolean hasMod() {
        return installedMod != null;
    }

    /**
     * MODを装着した新しいスロットを返す
     *
     * @param mod 装着するMOD
     * @return 新しいModSlot
     */
    public ModSlot withMod(InstalledMod mod) {
        return new ModSlot(this.index, this.polarity, mod, this.slotType);
    }

    /**
     * MODを取り外した新しいスロットを返す
     *
     * @return 新しいModSlot
     */
    public ModSlot clearMod() {
        return new ModSlot(this.index, this.polarity, null, this.slotType);
    }

    /**
     * 極性を設定した新しいスロットを返す
     *
     * @param newPolarity 新しい極性
     * @return 新しいModSlot
     */
    public ModSlot withPolarity(Polarity newPolarity) {
        return new ModSlot(this.index, newPolarity, this.installedMod, this.slotType);
    }

    /**
     * オーラスロットかどうか
     */
    public boolean isAuraSlot() {
        return slotType == SlotType.AURA;
    }

    /**
     * エクシルススロットかどうか
     */
    public boolean isExilusSlot() {
        return slotType == SlotType.EXILUS;
    }

    /**
     * 通常スロットかどうか
     */
    public boolean isNormalSlot() {
        return slotType == SlotType.NORMAL;
    }
}
