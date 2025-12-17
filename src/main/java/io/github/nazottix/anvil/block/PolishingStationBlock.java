package io.github.nazottix.anvil.block;

import com.mojang.serialization.MapCodec;
import io.github.nazottix.anvil.block.entity.AnvilBlockEntities;
import io.github.nazottix.anvil.block.entity.PolishingStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * 研磨ステーションブロック
 *
 * 鍛造素材(FORGED)を研磨素材(POLISHED)に変換するステーションです。
 * 研磨剤を消費して品質ボーナスを付与します。
 *
 * 特徴:
 * - 自動処理（時間経過）
 * - 研磨剤消費で品質ボーナス付与
 * - 稼働中は輝きパーティクル
 */
public class PolishingStationBlock extends BaseEntityBlock {

    // コーデック（シリアライズ用）
    public static final MapCodec<PolishingStationBlock> CODEC = simpleCodec(PolishingStationBlock::new);

    // ブロックの向きプロパティ
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    // 稼働中フラグ（パーティクル用）
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    /**
     * コンストラクタ
     */
    public PolishingStationBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // ============================================
    // ブロックエンティティ
    // ============================================

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PolishingStationBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (!level.isClientSide) {
            return createTickerHelper(blockEntityType, AnvilBlockEntities.POLISHING_STATION.get(),
                    PolishingStationBlockEntity::serverTick);
        }
        return null;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // ============================================
    // ブロック状態
    // ============================================

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(LIT, false);
    }

    // ============================================
    // インタラクション
    // ============================================

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof PolishingStationBlockEntity polishingEntity) {
                ((ServerPlayer) player).openMenu(polishingEntity, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    // ============================================
    // パーティクル（クライアント側）
    // ============================================

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            // 稼働中は輝きパーティクル（研磨らしい演出）
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 1.0;
            double z = pos.getZ() + 0.5;

            // ランダムに輝きを発生
            if (random.nextFloat() < 0.4f) {
                level.addParticle(ParticleTypes.END_ROD,
                        x + (random.nextDouble() - 0.5) * 0.5,
                        y,
                        z + (random.nextDouble() - 0.5) * 0.5,
                        0.0, 0.02, 0.0);
            }
        }
    }

    // ============================================
    // ブロック破壊時の処理
    // ============================================

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof PolishingStationBlockEntity polishingEntity) {
                polishingEntity.dropContents(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
