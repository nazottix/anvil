package io.github.nazottix.anvil.block;

import com.mojang.serialization.MapCodec;
import io.github.nazottix.anvil.block.entity.AnvilBlockEntities;
import io.github.nazottix.anvil.block.entity.PartForgeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * パーツ鍛造所ブロック
 *
 * 加工済み素材をパーツアイテムに変換するステーションです。
 * プレイヤーがパーツタイプを選択して鋳造を行います。
 *
 * 特徴:
 * - 即時鋳造（時間経過なし）
 * - パーツタイプ選択UI
 * - 加工素材のグレードがパーツに継承
 */
public class PartForgeBlock extends BaseEntityBlock {

    // コーデック（シリアライズ用）
    public static final MapCodec<PartForgeBlock> CODEC = simpleCodec(PartForgeBlock::new);

    // ブロックの向きプロパティ
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    /**
     * コンストラクタ
     */
    public PartForgeBlock(Properties properties) {
        super(properties);
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
        return new PartForgeBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        // モデルとしてレンダリング
        return RenderShape.MODEL;
    }

    // ============================================
    // ブロック状態
    // ============================================

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // プレイヤーの向きと逆方向を向く
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    // ============================================
    // インタラクション
    // ============================================

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof PartForgeBlockEntity partForgeEntity) {
                // サーバー側: メニューを開く
                ((ServerPlayer) player).openMenu(partForgeEntity, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    // ============================================
    // ブロック破壊時の処理
    // ============================================

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof PartForgeBlockEntity partForgeEntity) {
                // インベントリ内容をドロップ
                partForgeEntity.dropContents(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
