package io.github.nazottix.anvil.block;

import com.mojang.serialization.MapCodec;
import io.github.nazottix.anvil.block.entity.AnvilBlockEntities;
import io.github.nazottix.anvil.block.entity.ForgingStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
 * 鍛造ステーションブロック
 *
 * 精錬素材(REFINED)を鍛造素材(FORGED)に変換するステーションです。
 * ハンマーを消費して時間経過で自動処理します。
 *
 * 特徴:
 * - 自動処理（時間経過）
 * - ハンマー耐久消費
 * - 稼働中は炎/火花パーティクル
 */
public class ForgingStationBlock extends BaseEntityBlock {

    // コーデック（シリアライズ用）
    public static final MapCodec<ForgingStationBlock> CODEC = simpleCodec(ForgingStationBlock::new);

    // ブロックの向きプロパティ
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    // 稼働中フラグ（パーティクル/光源用）
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    /**
     * コンストラクタ
     */
    public ForgingStationBlock(Properties properties) {
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
        return new ForgingStationBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (!level.isClientSide) {
            return createTickerHelper(blockEntityType, AnvilBlockEntities.FORGING_STATION.get(),
                    ForgingStationBlockEntity::serverTick);
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
            if (blockEntity instanceof ForgingStationBlockEntity forgingEntity) {
                ((ServerPlayer) player).openMenu(forgingEntity, pos);
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
            // 稼働中は火花パーティクル
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 1.0;
            double z = pos.getZ() + 0.5;

            // ランダムに火花を発生
            if (random.nextFloat() < 0.3f) {
                level.addParticle(ParticleTypes.LAVA,
                        x + (random.nextDouble() - 0.5) * 0.5,
                        y,
                        z + (random.nextDouble() - 0.5) * 0.5,
                        0.0, 0.05, 0.0);
            }

            // ランダムに金床音
            if (random.nextFloat() < 0.1f) {
                level.playLocalSound(x, y, z, SoundEvents.ANVIL_USE, SoundSource.BLOCKS,
                        0.3f, 0.8f + random.nextFloat() * 0.4f, false);
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
            if (blockEntity instanceof ForgingStationBlockEntity forgingEntity) {
                forgingEntity.dropContents(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
