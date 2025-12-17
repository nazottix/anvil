package io.github.nazottix.anvil.block;

import com.mojang.serialization.MapCodec;
import io.github.nazottix.anvil.block.entity.RepairStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * リペアステーションブロック
 *
 * ANVILツールの修理に使用するワークベンチブロックです。
 * プレイヤーはこのブロックを右クリックしてツール修理UIを開きます。
 *
 * 機能:
 * - ツールの耐久値修理
 * - パーツを消費して修理
 *
 * 仕様書参照: docs/08_入手_リスペックシステム.md
 */
public class RepairStationBlock extends BaseEntityBlock {

    // Codec（データパック用）
    public static final MapCodec<RepairStationBlock> CODEC = simpleCodec(RepairStationBlock::new);

    // 方向プロパティ（設置時のプレイヤーの向きに基づく）
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    // 当たり判定の形状（標準的なブロック）
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    /**
     * コンストラクタ
     *
     * @param properties ブロックプロパティ
     */
    public RepairStationBlock(Properties properties) {
        super(properties);
        // デフォルトの向きを北に設定
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // ============================================
    // ブロックステート
    // ============================================

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // 方向プロパティを追加
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // プレイヤーの向きと反対方向を向く
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    // ============================================
    // ブロックエンティティ
    // ============================================

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RepairStationBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        // モデルレンダリングを使用
        return RenderShape.MODEL;
    }

    // ============================================
    // インタラクション
    // ============================================

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        // サーバー側でのみ処理
        if (!level.isClientSide()) {
            // メニュープロバイダーを取得
            MenuProvider menuProvider = this.getMenuProvider(state, level, pos);
            if (menuProvider != null && player instanceof ServerPlayer serverPlayer) {
                // GUIを開く
                serverPlayer.openMenu(menuProvider, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        // ブロックエンティティを取得
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof RepairStationBlockEntity repairStation) {
            return repairStation;
        }
        return null;
    }

    // ============================================
    // 形状
    // ============================================

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // ============================================
    // ブロック破壊時の処理
    // ============================================

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        // ブロックが変わった場合（破壊された場合）
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof RepairStationBlockEntity repairStation) {
                // インベントリ内のアイテムをドロップ
                repairStation.dropContents(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
