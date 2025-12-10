package io.github.nazottix.anvil.leveling;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.item.AnvilToolItem;
import io.github.nazottix.anvil.tool.ToolType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * XP獲得イベントハンドラー
 *
 * ゲームイベント（ブロック破壊、モブキルなど）を監視し、
 * ANVILツールにXPを付与します。
 *
 * 仕様書参照: docs/02_レベリングシステム.md
 */
@EventBusSubscriber(modid = ANVIL.MODID)
public final class XpEventHandler {

    // ============================================
    // ブロック破壊イベント
    // ============================================

    /**
     * ブロック破壊時のXP付与
     *
     * ピッケル、斧、シャベルなどの採掘ツールに対応
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof AnvilToolItem toolItem)) {
            return;
        }

        BlockState state = event.getState();
        ToolType toolType = toolItem.getToolType();

        // ツールタイプに応じたXPソースを決定
        XpSource source = determineXpSourceForBlock(state, toolType);
        if (source == null) {
            return;
        }

        // XPを付与（乗数1.0）
        int oldLevel = LevelingManager.getLevel(heldItem);
        boolean leveledUp = LevelingManager.addXp(heldItem, source, 1.0f);

        // レベルアップした場合は通知
        if (leveledUp) {
            int newLevel = LevelingManager.getLevel(heldItem);
            LevelingManager.notifyLevelUp(serverPlayer, heldItem, oldLevel, newLevel);
        }

        ANVIL.LOGGER.debug("ブロック破壊XP: {} -> {} (ソース: {})",
                oldLevel, LevelingManager.getLevel(heldItem), source.name());
    }

    /**
     * ブロックとツールタイプからXPソースを決定
     */
    private static XpSource determineXpSourceForBlock(BlockState state, ToolType toolType) {
        Block block = state.getBlock();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);

        return switch (toolType) {
            case PICKAXE -> determinePickaxeXpSource(state, blockId);
            case AXE -> determineAxeXpSource(state, blockId);
            case SHOVEL -> XpSource.SOIL_BREAK;
            case HOE -> determineHoeXpSource(state, blockId);
            case SHEARS -> determineShearXpSource(state, blockId);
            default -> null;
        };
    }

    /**
     * ピッケル用XPソース判定
     */
    private static XpSource determinePickaxeXpSource(BlockState state, ResourceLocation blockId) {
        String path = blockId.getPath();

        // 古代の残骸（最高XP）
        if (path.equals("ancient_debris")) {
            return XpSource.ANCIENT_DEBRIS_BREAK;
        }

        // ネザー鉱石
        if (path.contains("nether") && (path.contains("ore") || path.contains("quartz"))) {
            return XpSource.NETHER_ORE_BREAK;
        }

        // 通常鉱石（タグまたは名前で判定）
        if (state.is(BlockTags.GOLD_ORES) ||
                state.is(BlockTags.IRON_ORES) ||
                state.is(BlockTags.COPPER_ORES) ||
                state.is(BlockTags.COAL_ORES) ||
                state.is(BlockTags.DIAMOND_ORES) ||
                state.is(BlockTags.EMERALD_ORES) ||
                state.is(BlockTags.LAPIS_ORES) ||
                state.is(BlockTags.REDSTONE_ORES) ||
                path.contains("ore")) {
            return XpSource.ORE_BREAK;
        }

        // 通常ブロック
        return XpSource.BLOCK_BREAK;
    }

    /**
     * 斧用XPソース判定
     */
    private static XpSource determineAxeXpSource(BlockState state, ResourceLocation blockId) {
        String path = blockId.getPath();

        // 特殊木材（ネザー木材、歪んだ木など）
        if (path.contains("crimson") || path.contains("warped") ||
                path.contains("mangrove")) {
            return XpSource.SPECIAL_LOG_BREAK;
        }

        // 原木
        if (state.is(BlockTags.LOGS)) {
            return XpSource.LOG_BREAK;
        }

        // その他の木材関連
        return XpSource.BLOCK_BREAK;
    }

    /**
     * クワ用XPソース判定
     */
    private static XpSource determineHoeXpSource(BlockState state, ResourceLocation blockId) {
        String path = blockId.getPath();

        // 成長した作物
        if (path.contains("wheat") || path.contains("carrots") ||
                path.contains("potatoes") || path.contains("beetroots") ||
                path.contains("nether_wart")) {
            return XpSource.MATURE_CROP_HARVEST;
        }

        return XpSource.CROP_HARVEST;
    }

    /**
     * ハサミ用XPソース判定
     */
    private static XpSource determineShearXpSource(BlockState state, ResourceLocation blockId) {
        String path = blockId.getPath();

        if (state.is(BlockTags.LEAVES)) {
            return XpSource.SHEARING_LEAVES;
        }

        if (path.contains("cobweb") || path.contains("vine") || path.contains("glow_lichen")) {
            return XpSource.SHEARING_SPECIAL;
        }

        return XpSource.SHEARING_WOOL;
    }

    // ============================================
    // モブキルイベント
    // ============================================

    /**
     * モブキル時のXP付与
     *
     * 剣やその他の戦闘ツールに対応
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        Entity source = event.getSource().getEntity();
        if (!(source instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ItemStack heldItem = serverPlayer.getMainHandItem();
        if (!(heldItem.getItem() instanceof AnvilToolItem toolItem)) {
            return;
        }

        LivingEntity target = event.getEntity();
        ToolType toolType = toolItem.getToolType();

        // 戦闘系ツールでない場合はスキップ
        if (toolType != ToolType.SWORD && toolType != ToolType.BOW) {
            return;
        }

        // XPソースを決定
        XpSource xpSource = determineXpSourceForEntity(target, serverPlayer, toolType);
        if (xpSource == null) {
            return;
        }

        // XPを付与
        int oldLevel = LevelingManager.getLevel(heldItem);
        boolean leveledUp = LevelingManager.addXp(heldItem, xpSource, 1.0f);

        // レベルアップした場合は通知
        if (leveledUp) {
            int newLevel = LevelingManager.getLevel(heldItem);
            LevelingManager.notifyLevelUp(serverPlayer, heldItem, oldLevel, newLevel);
        }

        ANVIL.LOGGER.debug("モブキルXP: {} -> {} (ソース: {})",
                oldLevel, LevelingManager.getLevel(heldItem), xpSource.name());
    }

    /**
     * エンティティからXPソースを決定
     */
    private static XpSource determineXpSourceForEntity(LivingEntity target, ServerPlayer attacker, ToolType toolType) {
        // ボス判定（EnderDragonPartはLivingEntityではないため除外）
        if (target instanceof WitherBoss ||
                target instanceof EnderDragon) {
            return XpSource.BOSS_KILL;
        }

        // プレイヤーキル（PvP）
        if (target instanceof Player) {
            return XpSource.PLAYER_KILL;
        }

        // 敵対モブ
        if (target instanceof Monster) {
            // 弓の場合は距離ボーナスを考慮
            if (toolType == ToolType.BOW) {
                double distance = attacker.distanceTo(target);
                if (distance >= 50) {
                    return XpSource.BOW_SNIPER_KILL;
                } else if (distance >= 25) {
                    return XpSource.BOW_LONG_KILL;
                }
                return XpSource.BOW_CLOSE_KILL;
            }

            return XpSource.HOSTILE_KILL;
        }

        // その他のエンティティは無視
        return null;
    }

    // コンストラクタを非公開
    private XpEventHandler() {}
}
