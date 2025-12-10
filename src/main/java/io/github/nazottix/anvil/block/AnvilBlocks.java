package io.github.nazottix.anvil.block;

import io.github.nazottix.anvil.ANVIL;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * ANVILブロック登録クラス
 *
 * すべてのANVILブロックをここで登録します。
 * NeoForgeのDeferredRegisterパターンを使用して遅延登録を行います。
 */
public class AnvilBlocks {

    // ============================================
    // ブロック登録
    // ============================================

    /**
     * ツールステーションブロック
     *
     * ANVILツールの作成・カスタマイズに使用するワークベンチです。
     * プレイヤーはこのブロックでパーツを組み合わせてツールを作成できます。
     */
    public static final DeferredBlock<ToolStationBlock> TOOL_STATION = ANVIL.BLOCKS.register(
            "tool_station",
            () -> new ToolStationBlock(BlockBehaviour.Properties.of()
                    // 木材のような硬さと爆発耐性
                    .strength(2.5f, 2.5f)
                    // ハサミ/斧で素早く破壊
                    .requiresCorrectToolForDrops()
                    // 光を完全に遮断しない
                    .noOcclusion()
            )
    );

    // ============================================
    // アイテム登録（ブロックアイテム）
    // ============================================

    /**
     * ツールステーションのブロックアイテム
     */
    public static final DeferredItem<BlockItem> TOOL_STATION_ITEM = ANVIL.ITEMS.register(
            "tool_station",
            () -> new BlockItem(TOOL_STATION.get(), new Item.Properties())
    );

    // ============================================
    // 初期化
    // ============================================

    /**
     * ブロック登録を初期化
     *
     * このメソッドを呼び出すことで、静的フィールドが確実に初期化されます。
     * ANVILクラスのコンストラクタから呼び出されます。
     */
    public static void init() {
        ANVIL.LOGGER.info("ANVIL: ブロックを初期化");
    }

    /**
     * ブロックをイベントバスに登録
     *
     * @param modEventBus MODイベントバス
     */
    public static void register(IEventBus modEventBus) {
        // ブロックとアイテムはANVIL.javaで一括登録されるため、
        // ここでは追加の登録処理は不要
    }
}
