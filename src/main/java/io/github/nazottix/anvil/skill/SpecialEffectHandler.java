package io.github.nazottix.anvil.skill;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.data.AnvilDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * 特殊効果ハンドラー
 *
 * キーストーンやノータブルの特殊効果を処理します。
 *
 * 仕様書参照: docs/03_スキルツリーシステム.md
 */
public class SpecialEffectHandler {

    // ============================================
    // 特殊効果ID定数
    // ============================================

    // ピッケル
    public static final String EFFECT_VEIN_MINER = "vein_miner";
    public static final String EFFECT_VOID_DROPS = "void_drops";
    public static final String EFFECT_MIDAS_TOUCH = "midas_touch";
    public static final String EFFECT_CHAIN_EXPLOSION = "chain_explosion";
    public static final String EFFECT_INFINITE_VEIN = "infinite_vein";

    // 斧
    public static final String EFFECT_TREE_FELLER = "tree_feller";
    public static final String EFFECT_INSTANT_TREE_FELL = "instant_tree_fell";
    public static final String EFFECT_CLEAVE_RANGE = "cleave_range";
    public static final String EFFECT_AUTO_REPLANT = "auto_replant";
    public static final String EFFECT_SAPLING_CHANCE = "sapling_chance";
    public static final String EFFECT_FOREST_MASTER = "forest_master";

    // シャベル
    public static final String EFFECT_AREA_DIG = "area_dig";
    public static final String EFFECT_INSTANT_PATH = "instant_path";
    public static final String EFFECT_TUNNEL_BORE = "tunnel_bore";
    public static final String EFFECT_BURIED_TREASURE_SENSE = "buried_treasure_sense";
    public static final String EFFECT_EARTH_SHAPER = "earth_shaper";

    // クワ
    public static final String EFFECT_TILLING_AREA = "tilling_area";
    public static final String EFFECT_AUTO_WATER = "auto_water";
    public static final String EFFECT_AUTO_REPLANT_CROP = "auto_replant_crop";
    public static final String EFFECT_HARVEST_AREA = "harvest_area";
    public static final String EFFECT_AURA_GROWTH = "aura_growth";
    public static final String EFFECT_INSTANT_GROWTH_CHANCE = "instant_growth_chance";
    public static final String EFFECT_CONSUME_BONEMEAL = "consume_bonemeal";
    public static final String EFFECT_HARVEST_DEITY = "harvest_deity";

    // 弓
    public static final String EFFECT_MULTISHOT = "multishot";
    public static final String EFFECT_EXPLOSIVE_ARROW = "explosive_arrow";
    public static final String EFFECT_SELF_DAMAGE = "self_damage";
    public static final String EFFECT_INSTANT_KILL_HEADSHOT = "instant_kill_headshot";
    public static final String EFFECT_LEGENDARY_HUNTER = "legendary_hunter";

    // 釣り竿
    public static final String EFFECT_INSTANT_BITE = "instant_bite";
    public static final String EFFECT_DOUBLE_CATCH = "double_catch";
    public static final String EFFECT_INSTANT_CATCH = "instant_catch";
    public static final String EFFECT_FISH_ONLY = "fish_only";
    public static final String EFFECT_ENCHANTED_CATCH = "enchanted_catch";
    public static final String EFFECT_AUTO_REPAIR_CATCH = "auto_repair_catch";
    public static final String EFFECT_MASTER_ANGLER = "master_angler";

    // ハサミ
    public static final String EFFECT_AREA_SHEAR = "area_shear";
    public static final String EFFECT_COLORED_WOOL_CHANCE = "colored_wool_chance";
    public static final String EFFECT_SILK_TOUCH_PLANT = "silk_touch_plant";
    public static final String EFFECT_SHEEP_DAMAGE = "sheep_damage";
    public static final String EFFECT_AUTO_COLLECT = "auto_collect";
    public static final String EFFECT_HARVEST_MASTER = "harvest_master";

    // 共通
    public static final String EFFECT_COOLDOWN = "cooldown";
    public static final String EFFECT_HORIZONTAL_ONLY = "horizontal_only";

    // クールダウン管理
    private static final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    // ============================================
    // ブロック破壊時の処理
    // ============================================

    /**
     * ブロック破壊時の特殊効果を処理
     *
     * @param level ワールド
     * @param player プレイヤー
     * @param pos 破壊位置
     * @param state 破壊したブロック
     * @param stack 使用したツール
     * @param drops 元のドロップリスト
     * @return 処理後のドロップリスト
     */
    public static List<ItemStack> onBlockBreak(Level level, Player player, BlockPos pos,
                                                BlockState state, ItemStack stack, List<ItemStack> drops) {
        SkillEffectApplier.AggregatedEffects effects = SkillEffectApplier.aggregateEffects(stack);
        List<ItemStack> modifiedDrops = new ArrayList<>(drops);

        // Void Mining: ドロップの50%が消失
        if (effects.hasSpecialEffect(EFFECT_VOID_DROPS)) {
            float voidChance = effects.getSpecialEffect(EFFECT_VOID_DROPS) / 100f;
            modifiedDrops.removeIf(drop -> level.random.nextFloat() < voidChance);
        }

        // Midas Touch: 鉱石が金塊に変換
        if (effects.hasSpecialEffect(EFFECT_MIDAS_TOUCH)) {
            float midasChance = effects.getSpecialEffect(EFFECT_MIDAS_TOUCH) / 100f;
            if (isOre(state) && level.random.nextFloat() < midasChance) {
                modifiedDrops.clear();
                modifiedDrops.add(new ItemStack(Items.GOLD_NUGGET, 1 + level.random.nextInt(3)));
            }
        }

        // Chain Reaction: 連鎖爆発
        if (effects.hasSpecialEffect(EFFECT_CHAIN_EXPLOSION)) {
            if (isOre(state) && !level.isClientSide) {
                triggerChainExplosion(level, player, pos, effects.getSpecialEffect(EFFECT_CHAIN_EXPLOSION));
            }
        }

        // Auto Replant (木): 苗木を自動植え
        if (effects.hasSpecialEffect(EFFECT_AUTO_REPLANT)) {
            if (isLog(state) && !level.isClientSide) {
                scheduleReplant(level, pos, state);
            }
        }

        // Sapling Chance: 追加苗木ドロップ
        if (effects.hasSpecialEffect(EFFECT_SAPLING_CHANCE)) {
            if (isLog(state)) {
                float saplingChance = effects.getSpecialEffect(EFFECT_SAPLING_CHANCE) / 100f;
                if (level.random.nextFloat() < saplingChance) {
                    ItemStack sapling = getSaplingForLog(state);
                    if (!sapling.isEmpty()) {
                        modifiedDrops.add(sapling);
                    }
                }
            }
        }

        return modifiedDrops;
    }

    /**
     * 範囲採掘/掘削を処理
     *
     * @param level ワールド
     * @param player プレイヤー
     * @param centerPos 中心位置
     * @param stack 使用したツール
     * @param radius 範囲半径
     * @return 追加で破壊したブロック数
     */
    public static int processAreaMining(Level level, Player player, BlockPos centerPos,
                                        ItemStack stack, int radius) {
        if (level.isClientSide || radius <= 0) {
            return 0;
        }

        SkillEffectApplier.AggregatedEffects effects = SkillEffectApplier.aggregateEffects(stack);
        int destroyed = 0;

        // 範囲内のブロックを取得
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x == 0 && y == 0 && z == 0) continue; // 中心はスキップ

                    BlockPos targetPos = centerPos.offset(x, y, z);
                    BlockState targetState = level.getBlockState(targetPos);

                    // 同じ種類のブロックのみ破壊
                    if (canBreakWithTool(targetState, stack)) {
                        // ブロックを破壊
                        level.destroyBlock(targetPos, true, player);
                        destroyed++;

                        // 耐久値消費
                        if (!player.getAbilities().instabuild) {
                            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
                        }
                    }
                }
            }
        }

        return destroyed;
    }

    // ============================================
    // 攻撃時の処理
    // ============================================

    /**
     * 攻撃時の特殊効果を処理
     *
     * @param attacker 攻撃者
     * @param target ターゲット
     * @param stack 使用したツール
     * @param baseDamage 基本ダメージ
     * @return 修正後のダメージ
     */
    public static float onAttack(LivingEntity attacker, Entity target, ItemStack stack, float baseDamage) {
        SkillEffectApplier.AggregatedEffects effects = SkillEffectApplier.aggregateEffects(stack);
        float damage = baseDamage;

        // Berserker: 低HP時ダメージ増加
        if (effects.hasSpecialEffect("berserker_damage")) {
            float maxBonus = effects.getSpecialEffect("berserker_damage");
            float healthRatio = attacker.getHealth() / attacker.getMaxHealth();
            // HP50%以下から効果発動、HP0%で最大ボーナス
            if (healthRatio < 0.5f) {
                float bonusRatio = (0.5f - healthRatio) / 0.5f;
                damage *= (1 + (maxBonus / 100f) * bonusRatio);
            }
        }

        // Cleave: 範囲攻撃
        if (effects.hasSpecialEffect(EFFECT_CLEAVE_RANGE)) {
            float range = effects.getSpecialEffect(EFFECT_CLEAVE_RANGE);
            if (attacker instanceof Player player && !attacker.level().isClientSide) {
                performCleaveAttack(player, target, range, damage * 0.5f);
            }
        }

        // Lifesteal
        SkillEffectApplier.StatModifiers lifestealMod = effects.getStatModifiers(SkillEffectApplier.STAT_LIFESTEAL);
        if (lifestealMod.percent() > 0) {
            float healAmount = damage * (lifestealMod.percent() / 100f);
            attacker.heal(healAmount);
        }

        return damage;
    }

    /**
     * 範囲攻撃を実行
     */
    private static void performCleaveAttack(Player player, Entity primaryTarget, float range, float damage) {
        Level level = player.level();
        AABB area = primaryTarget.getBoundingBox().inflate(range);

        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e != primaryTarget && e.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(level.damageSources().playerAttack(player), damage);
        }
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * クールダウンをチェック
     */
    public static boolean isOnCooldown(Player player, String effectId) {
        UUID playerId = player.getUUID();
        if (!cooldowns.containsKey(playerId)) {
            return false;
        }

        Long cooldownEnd = cooldowns.get(playerId).get(effectId);
        if (cooldownEnd == null) {
            return false;
        }

        return System.currentTimeMillis() < cooldownEnd;
    }

    /**
     * クールダウンを設定
     */
    public static void setCooldown(Player player, String effectId, int ticks) {
        UUID playerId = player.getUUID();
        cooldowns.computeIfAbsent(playerId, k -> new HashMap<>());
        // ティックをミリ秒に変換（1ティック = 50ms）
        long cooldownEnd = System.currentTimeMillis() + (ticks * 50L);
        cooldowns.get(playerId).put(effectId, cooldownEnd);
    }

    /**
     * 残りクールダウン時間を取得（ティック）
     */
    public static int getRemainingCooldown(Player player, String effectId) {
        UUID playerId = player.getUUID();
        if (!cooldowns.containsKey(playerId)) {
            return 0;
        }

        Long cooldownEnd = cooldowns.get(playerId).get(effectId);
        if (cooldownEnd == null) {
            return 0;
        }

        long remaining = cooldownEnd - System.currentTimeMillis();
        return remaining > 0 ? (int)(remaining / 50) : 0;
    }

    /**
     * ブロックが鉱石かどうか
     */
    private static boolean isOre(BlockState state) {
        Block block = state.getBlock();
        String name = block.getDescriptionId().toLowerCase();
        return name.contains("ore") || name.contains("raw_");
    }

    /**
     * ブロックが原木かどうか
     */
    private static boolean isLog(BlockState state) {
        Block block = state.getBlock();
        String name = block.getDescriptionId().toLowerCase();
        return name.contains("log") || name.contains("stem") || name.contains("wood");
    }

    /**
     * ツールでブロックを破壊できるか
     */
    private static boolean canBreakWithTool(BlockState state, ItemStack tool) {
        return !state.isAir() && state.getDestroySpeed(null, BlockPos.ZERO) >= 0
                && tool.isCorrectToolForDrops(state);
    }

    /**
     * 連鎖爆発を発生させる
     */
    private static void triggerChainExplosion(Level level, Player player, BlockPos pos, float power) {
        // 小規模な爆発（ブロック破壊なし、エンティティダメージあり）
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                power * 0.5f, Level.ExplosionInteraction.NONE);

        // プレイヤーにも少量のダメージ
        float selfDamage = power * 0.5f;
        player.hurt(level.damageSources().explosion(null), selfDamage);
    }

    /**
     * 自動植林をスケジュール
     */
    private static void scheduleReplant(Level level, BlockPos pos, BlockState originalState) {
        // 簡易実装: 地面を探して苗木を設置
        BlockPos groundPos = pos.below();
        while (groundPos.getY() > level.getMinBuildHeight() && level.getBlockState(groundPos).isAir()) {
            groundPos = groundPos.below();
        }

        BlockPos saplingPos = groundPos.above();
        if (level.getBlockState(saplingPos).isAir()) {
            ItemStack sapling = getSaplingForLog(originalState);
            if (!sapling.isEmpty() && sapling.getItem() instanceof net.minecraft.world.item.BlockItem blockItem) {
                level.setBlock(saplingPos, blockItem.getBlock().defaultBlockState(), 3);
            }
        }
    }

    /**
     * 原木に対応する苗木を取得
     */
    private static ItemStack getSaplingForLog(BlockState logState) {
        String name = logState.getBlock().getDescriptionId().toLowerCase();

        if (name.contains("oak")) return new ItemStack(Items.OAK_SAPLING);
        if (name.contains("birch")) return new ItemStack(Items.BIRCH_SAPLING);
        if (name.contains("spruce")) return new ItemStack(Items.SPRUCE_SAPLING);
        if (name.contains("jungle")) return new ItemStack(Items.JUNGLE_SAPLING);
        if (name.contains("acacia")) return new ItemStack(Items.ACACIA_SAPLING);
        if (name.contains("dark_oak")) return new ItemStack(Items.DARK_OAK_SAPLING);
        if (name.contains("cherry")) return new ItemStack(Items.CHERRY_SAPLING);
        if (name.contains("mangrove")) return new ItemStack(Items.MANGROVE_PROPAGULE);

        return ItemStack.EMPTY;
    }

    // コンストラクタを非公開
    private SpecialEffectHandler() {}
}
