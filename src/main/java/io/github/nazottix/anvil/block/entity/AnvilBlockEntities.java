package io.github.nazottix.anvil.block.entity;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.block.AnvilBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * ANVILブロックエンティティ登録クラス
 *
 * すべてのANVILブロックエンティティをここで登録します。
 */
public class AnvilBlockEntities {

    // ブロックエンティティ登録用のDeferredRegister
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ANVIL.MODID);

    // ============================================
    // ブロックエンティティ登録
    // ============================================

    /**
     * ツールステーションブロックエンティティタイプ
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ToolStationBlockEntity>> TOOL_STATION =
            BLOCK_ENTITIES.register("tool_station",
                    () -> BlockEntityType.Builder.of(
                            ToolStationBlockEntity::new,
                            AnvilBlocks.TOOL_STATION.get()
                    ).build(null)
            );

    // ============================================
    // 登録
    // ============================================

    /**
     * ブロックエンティティをMODイベントバスに登録
     *
     * @param modEventBus MODイベントバス
     */
    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
        ANVIL.LOGGER.info("ANVIL: ブロックエンティティを登録");
    }
}
