package io.github.nazottix.anvil.block;

import com.mojang.serialization.MapCodec;
import io.github.nazottix.anvil.block.entity.AnvilBlockEntities;
import io.github.nazottix.anvil.block.entity.RefineryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * 精錬所ブロック
 *
 * バニラの素材アイテムを加工素材（ProcessedMaterial）に変換するステーションです。
 * 燃料を消費して時間経過で素材を精錬します。
 *
 * 機能:
 * - 入力スロットにバニラ素材を配置
 * - 燃料スロットに燃料を配置
 * - 時間経過で精錬済み素材（REFINED）を出力
 *
 * 加工チェーンの最初のステップ:
 * バニラ素材 → [精錬所] → 精錬素材(REFINED, Grade D)
 */
public class RefineryBlock extends BaseEntityBlock {

    // Codec（データパック用）
    public static final MapCodec<RefineryBlock> CODEC = simpleCodec(RefineryBlock::new);

    // 方向プロパティ（設置時のプレイヤーの向きに基づく）
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    // 稼働中かどうか（パーティクル・サウンド制御用）
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    // 当たり判定の形状（標準的なブロック）
    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    /**
     * コンストラクタ
     *
     * @param properties ブロックプロパティ
     */
    public RefineryBlock(Properties properties) {
        super(properties);
        // デフォルトの向きを北に、消灯状態で設定
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false));
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
        // 方向とLIT状態プロパティを追加
        builder.add(FACING, LIT);
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
        return new RefineryBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        // モデルレンダリングを使用
        return RenderShape.MODEL;
    }

    /**
     * サーバー側でのtick処理用Tickerを返す
     */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        // サーバー側でのみtick処理を実行
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(blockEntityType, AnvilBlockEntities.REFINERY.get(), RefineryBlockEntity::serverTick);
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
        if (blockEntity instanceof RefineryBlockEntity refinery) {
            return refinery;
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
    // アニメーション効果
    // ============================================

    /**
     * 稼働中のパーティクルとサウンド効果
     * クライアント側で呼ばれる
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // 稼働中のみエフェクトを表示
        if (!state.getValue(LIT)) {
            return;
        }

        // サウンド効果（10%の確率）
        if (random.nextDouble() < 0.1) {
            level.playLocalSound(
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    SoundEvents.FURNACE_FIRE_CRACKLE,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F,
                    false
            );
        }

        // パーティクル効果
        Direction direction = state.getValue(FACING);
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;

        // 向きに応じたオフセット
        Direction.Axis axis = direction.getAxis();
        double offset = random.nextDouble() * 0.6 - 0.3;
        double xOffset = axis == Direction.Axis.X ? direction.getStepX() * 0.52 : offset;
        double zOffset = axis == Direction.Axis.Z ? direction.getStepZ() * 0.52 : offset;

        // 煙と火花のパーティクル
        level.addParticle(ParticleTypes.SMOKE, x + xOffset, y, z + zOffset, 0.0, 0.0, 0.0);
        level.addParticle(ParticleTypes.FLAME, x + xOffset, y, z + zOffset, 0.0, 0.0, 0.0);
    }

    // ============================================
    // ブロック破壊時の処理
    // ============================================

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        // ブロックが変わった場合（破壊された場合）
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof RefineryBlockEntity refinery) {
                // インベントリ内のアイテムをドロップ
                refinery.dropContents(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
