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

    /**
     * スキルツリーステーションブロックエンティティタイプ
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SkillTreeStationBlockEntity>> SKILL_TREE_STATION =
            BLOCK_ENTITIES.register("skill_tree_station",
                    () -> BlockEntityType.Builder.of(
                            SkillTreeStationBlockEntity::new,
                            AnvilBlocks.SKILL_TREE_STATION.get()
                    ).build(null)
            );

    /**
     * ジュエルステーションブロックエンティティタイプ
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<JewelStationBlockEntity>> JEWEL_STATION =
            BLOCK_ENTITIES.register("jewel_station",
                    () -> BlockEntityType.Builder.of(
                            JewelStationBlockEntity::new,
                            AnvilBlocks.JEWEL_STATION.get()
                    ).build(null)
            );

    /**
     * リスペックステーションブロックエンティティタイプ
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RespecStationBlockEntity>> RESPEC_STATION =
            BLOCK_ENTITIES.register("respec_station",
                    () -> BlockEntityType.Builder.of(
                            RespecStationBlockEntity::new,
                            AnvilBlocks.RESPEC_STATION.get()
                    ).build(null)
            );

    /**
     * コアボックスステーションブロックエンティティタイプ
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CoreBoxStationBlockEntity>> CORE_BOX_STATION =
            BLOCK_ENTITIES.register("core_box_station",
                    () -> BlockEntityType.Builder.of(
                            CoreBoxStationBlockEntity::new,
                            AnvilBlocks.CORE_BOX_STATION.get()
                    ).build(null)
            );

    /**
     * MODステーションブロックエンティティタイプ
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ModStationBlockEntity>> MOD_STATION =
            BLOCK_ENTITIES.register("mod_station",
                    () -> BlockEntityType.Builder.of(
                            ModStationBlockEntity::new,
                            AnvilBlocks.MOD_STATION.get()
                    ).build(null)
            );

    /**
     * リペアステーションブロックエンティティタイプ
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RepairStationBlockEntity>> REPAIR_STATION =
            BLOCK_ENTITIES.register("repair_station",
                    () -> BlockEntityType.Builder.of(
                            RepairStationBlockEntity::new,
                            AnvilBlocks.REPAIR_STATION.get()
                    ).build(null)
            );

    /**
     * 精錬所ブロックエンティティタイプ
     * バニラ素材を加工素材に精錬するステーション
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RefineryBlockEntity>> REFINERY =
            BLOCK_ENTITIES.register("refinery",
                    () -> BlockEntityType.Builder.of(
                            RefineryBlockEntity::new,
                            AnvilBlocks.REFINERY.get()
                    ).build(null)
            );

    /**
     * パーツ鍛造所ブロックエンティティタイプ
     * 加工素材をパーツアイテムに変換するステーション
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PartForgeBlockEntity>> PART_FORGE =
            BLOCK_ENTITIES.register("part_forge",
                    () -> BlockEntityType.Builder.of(
                            PartForgeBlockEntity::new,
                            AnvilBlocks.PART_FORGE.get()
                    ).build(null)
            );

    /**
     * 鍛造ステーションブロックエンティティタイプ
     * 精錬素材を鍛造素材に変換するステーション
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ForgingStationBlockEntity>> FORGING_STATION =
            BLOCK_ENTITIES.register("forging_station",
                    () -> BlockEntityType.Builder.of(
                            ForgingStationBlockEntity::new,
                            AnvilBlocks.FORGING_STATION.get()
                    ).build(null)
            );

    /**
     * 研磨ステーションブロックエンティティタイプ
     * 鍛造素材を研磨素材に変換するステーション
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PolishingStationBlockEntity>> POLISHING_STATION =
            BLOCK_ENTITIES.register("polishing_station",
                    () -> BlockEntityType.Builder.of(
                            PolishingStationBlockEntity::new,
                            AnvilBlocks.POLISHING_STATION.get()
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
