package io.github.nazottix.anvil.loot;

import com.mojang.serialization.MapCodec;
import io.github.nazottix.anvil.ANVIL;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * ANVILのGlobal Loot Modifierレジストリ
 *
 * ジュエルドロップなどのLoot修正機能を登録します。
 * NeoForgeのDeferredRegisterパターンを使用してLoot Modifierを登録。
 *
 * 仕様書参照: docs/03_スキルツリーシステム.md
 */
public final class AnvilLootModifiers {

    // ============================================
    // レジストリ
    // ============================================

    /**
     * Global Loot Modifier用のDeferredRegister
     */
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ANVIL.MODID);

    // ============================================
    // Loot Modifier登録
    // ============================================

    /**
     * ジュエルドロップLoot Modifier
     * モブ、釣り、チェストからジュエルをドロップさせる
     */
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<JewelLootModifier>>
            JEWEL_DROP = LOOT_MODIFIERS.register("jewel_drop", () -> JewelLootModifier.CODEC);

    // ============================================
    // 初期化
    // ============================================

    /**
     * レジストリをイベントバスに登録
     *
     * @param eventBus MODイベントバス
     */
    public static void register(IEventBus eventBus) {
        LOOT_MODIFIERS.register(eventBus);
        ANVIL.LOGGER.info("ANVIL: Loot Modifierを登録しました");
    }

    // コンストラクタを非公開
    private AnvilLootModifiers() {}
}
