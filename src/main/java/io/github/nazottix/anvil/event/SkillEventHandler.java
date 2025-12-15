package io.github.nazottix.anvil.event;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.assembly.CalculatedStats;
import io.github.nazottix.anvil.data.AnvilDataComponents;
import io.github.nazottix.anvil.item.AnvilToolItem;
import io.github.nazottix.anvil.skill.SkillEffectApplier;
import io.github.nazottix.anvil.skill.SpecialEffectHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.List;

/**
 * スキルイベントハンドラー
 *
 * ゲームイベントにスキル効果を適用します。
 *
 * 対応イベント:
 * - ブロック破壊（採掘速度、範囲採掘、特殊効果）
 * - 攻撃（ダメージ修正、範囲攻撃、ライフスティール）
 * - 採掘速度計算
 *
 * 仕様書参照: docs/03_スキルツリーシステム.md
 */
@EventBusSubscriber(modid = ANVIL.MODID)
public class SkillEventHandler {

    // ============================================
    // 採掘速度イベント
    // ============================================

    /**
     * 採掘速度計算時にスキル効果を適用
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        ItemStack stack = player.getMainHandItem();

        // ANVILツール以外は無視
        if (!(stack.getItem() instanceof AnvilToolItem)) {
            return;
        }

        // スキル効果を取得
        SkillEffectApplier.AggregatedEffects effects = SkillEffectApplier.aggregateEffects(stack);

        // 採掘速度修正を適用
        float originalSpeed = event.getOriginalSpeed();
        SkillEffectApplier.StatModifiers miningMod = effects.getStatModifiers(SkillEffectApplier.STAT_MINING_SPEED);
        SkillEffectApplier.StatModifiers choppingMod = effects.getStatModifiers(SkillEffectApplier.STAT_CHOPPING_SPEED);
        SkillEffectApplier.StatModifiers diggingMod = effects.getStatModifiers(SkillEffectApplier.STAT_DIGGING_SPEED);

        // 全ての速度修正を合算
        float totalPercent = miningMod.percent() + choppingMod.percent() + diggingMod.percent();
        float totalFlat = miningMod.flat() + choppingMod.flat() + diggingMod.flat();
        float totalMultiply = miningMod.multiply() + choppingMod.multiply() + diggingMod.multiply();

        // 計算: (base + flat) * (1 + percent/100) * (1 + multiply/100)
        float newSpeed = (originalSpeed + totalFlat) * (1 + totalPercent / 100f) * (1 + totalMultiply / 100f);

        event.setNewSpeed(Math.max(0.01f, newSpeed));
    }

    // ============================================
    // ブロック破壊イベント
    // ============================================

    /**
     * ブロック破壊時の特殊効果を処理
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.level().isClientSide) {
            return;
        }

        ItemStack stack = player.getMainHandItem();

        // ANVILツール以外は無視
        if (!(stack.getItem() instanceof AnvilToolItem)) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        Level level = player.level();

        // スキル効果を取得
        SkillEffectApplier.AggregatedEffects effects = SkillEffectApplier.aggregateEffects(stack);

        // 範囲採掘の処理
        float areaDig = effects.getSpecialEffect(SpecialEffectHandler.EFFECT_AREA_DIG);
        if (areaDig > 0) {
            int radius = (int) Math.ceil(areaDig / 2);
            // 再帰的な呼び出しを防ぐためのフラグチェック
            if (!isProcessingAreaMining(player)) {
                setProcessingAreaMining(player, true);
                try {
                    SpecialEffectHandler.processAreaMining(level, player, pos, stack, radius);
                } finally {
                    setProcessingAreaMining(player, false);
                }
            }
        }

        // Tree Feller: 連鎖伐採
        float treeFeller = effects.getSpecialEffect(SpecialEffectHandler.EFFECT_TREE_FELLER);
        if (treeFeller > 0 && isLog(state)) {
            int maxBlocks = (int) treeFeller;
            if (!isProcessingAreaMining(player)) {
                setProcessingAreaMining(player, true);
                try {
                    processTreeFelling(level, player, pos, stack, maxBlocks);
                } finally {
                    setProcessingAreaMining(player, false);
                }
            }
        }

        // Instant Tree Fell: 木全体を一撃で伐採（クールダウン付き）
        if (effects.hasSpecialEffect(SpecialEffectHandler.EFFECT_INSTANT_TREE_FELL)) {
            if (isLog(state) && !SpecialEffectHandler.isOnCooldown(player, SpecialEffectHandler.EFFECT_INSTANT_TREE_FELL)) {
                if (!isProcessingAreaMining(player)) {
                    setProcessingAreaMining(player, true);
                    try {
                        processInstantTreeFell(level, player, pos, stack);
                        // クールダウン設定（100ティック = 5秒）
                        float cooldownTicks = effects.getSpecialEffect(SpecialEffectHandler.EFFECT_COOLDOWN);
                        SpecialEffectHandler.setCooldown(player, SpecialEffectHandler.EFFECT_INSTANT_TREE_FELL,
                                cooldownTicks > 0 ? (int) cooldownTicks : 100);
                    } finally {
                        setProcessingAreaMining(player, false);
                    }
                }
            }
        }
    }

    // ============================================
    // 攻撃イベント
    // ============================================

    /**
     * 攻撃時にスキル効果を適用
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();

        if (player.level().isClientSide || !(target instanceof LivingEntity)) {
            return;
        }

        ItemStack stack = player.getMainHandItem();

        // ANVILツール以外は無視
        if (!(stack.getItem() instanceof AnvilToolItem)) {
            return;
        }

        // 攻撃ダメージはLivingDamageEventで処理するため、ここでは追加効果のみ
        SkillEffectApplier.AggregatedEffects effects = SkillEffectApplier.aggregateEffects(stack);

        // クリティカル判定の追加確率
        float critChanceBonus = effects.getStatModifiers(SkillEffectApplier.STAT_CRIT_CHANCE).percent();
        if (critChanceBonus > 0) {
            // クリティカルフラグを設定（後続処理用）
            // NeoForgeではカスタムデータで管理
            player.getPersistentData().putFloat("anvil:crit_bonus", critChanceBonus);
        }
    }

    /**
     * ダメージ計算時にスキル効果を適用
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        // 攻撃者がプレイヤーでない場合は無視
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        ItemStack stack = player.getMainHandItem();

        // ANVILツール以外は無視
        if (!(stack.getItem() instanceof AnvilToolItem)) {
            return;
        }

        LivingEntity target = event.getEntity();
        float originalDamage = event.getOriginalDamage();

        // スキル効果でダメージを修正
        float modifiedDamage = SpecialEffectHandler.onAttack(player, target, stack, originalDamage);

        // スキル効果からのダメージ修正を適用
        SkillEffectApplier.AggregatedEffects effects = SkillEffectApplier.aggregateEffects(stack);
        SkillEffectApplier.StatModifiers damageMod = effects.getStatModifiers(SkillEffectApplier.STAT_ATTACK_DAMAGE);

        modifiedDamage = damageMod.apply(modifiedDamage);

        // クリティカルダメージボーナス
        float critDamageBonus = effects.getStatModifiers(SkillEffectApplier.STAT_CRIT_DAMAGE).percent();
        if (critDamageBonus > 0 && player.getPersistentData().contains("anvil:crit_bonus")) {
            float critChance = player.getPersistentData().getFloat("anvil:crit_bonus");
            if (player.level().random.nextFloat() * 100 < critChance) {
                modifiedDamage *= (1 + critDamageBonus / 100f);
            }
            player.getPersistentData().remove("anvil:crit_bonus");
        }

        event.setNewDamage(Math.max(0, modifiedDamage));
    }

    // ============================================
    // ユーティリティ
    // ============================================

    // 範囲採掘処理中フラグ（再帰防止用）
    private static final java.util.Set<java.util.UUID> processingAreaMining = new java.util.HashSet<>();

    private static boolean isProcessingAreaMining(Player player) {
        return processingAreaMining.contains(player.getUUID());
    }

    private static void setProcessingAreaMining(Player player, boolean processing) {
        if (processing) {
            processingAreaMining.add(player.getUUID());
        } else {
            processingAreaMining.remove(player.getUUID());
        }
    }

    /**
     * ブロックが原木かどうか
     */
    private static boolean isLog(BlockState state) {
        String name = state.getBlock().getDescriptionId().toLowerCase();
        return name.contains("log") || name.contains("stem") || name.contains("wood");
    }

    /**
     * 連鎖伐採を処理
     */
    private static void processTreeFelling(Level level, Player player, BlockPos startPos,
                                           ItemStack stack, int maxBlocks) {
        java.util.Queue<BlockPos> toCheck = new java.util.LinkedList<>();
        java.util.Set<BlockPos> checked = new java.util.HashSet<>();
        java.util.List<BlockPos> toBreak = new java.util.ArrayList<>();

        toCheck.add(startPos);
        BlockState originalState = level.getBlockState(startPos);

        while (!toCheck.isEmpty() && toBreak.size() < maxBlocks) {
            BlockPos pos = toCheck.poll();
            if (checked.contains(pos)) continue;
            checked.add(pos);

            BlockState state = level.getBlockState(pos);
            if (!state.is(originalState.getBlock())) continue;

            toBreak.add(pos);

            // 隣接ブロックをチェック
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos neighbor = pos.offset(dx, dy, dz);
                        if (!checked.contains(neighbor)) {
                            toCheck.add(neighbor);
                        }
                    }
                }
            }
        }

        // 元のブロックはすでに破壊されるので除外
        toBreak.remove(startPos);

        // ブロックを破壊
        for (BlockPos pos : toBreak) {
            level.destroyBlock(pos, true, player);
            if (!player.getAbilities().instabuild) {
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
            }
        }
    }

    /**
     * 木全体を即時伐採
     */
    private static void processInstantTreeFell(Level level, Player player, BlockPos startPos, ItemStack stack) {
        // 木全体を検索（最大256ブロック）
        processTreeFelling(level, player, startPos, stack, 256);
    }
}
