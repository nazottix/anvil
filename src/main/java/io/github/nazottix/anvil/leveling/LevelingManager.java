package io.github.nazottix.anvil.leveling;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.Config;
import io.github.nazottix.anvil.data.AnvilDataComponents;
import io.github.nazottix.anvil.data.component.AnvilToolData;
import io.github.nazottix.anvil.item.AnvilToolItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * レベリングマネージャー
 *
 * ツールへのXP付与とレベルアップ処理を管理します。
 *
 * 仕様書参照: docs/02_レベリングシステム.md
 */
public final class LevelingManager {

    // ============================================
    // XP付与
    // ============================================

    /**
     * ツールにXPを付与
     *
     * @param stack ツールのItemStack
     * @param baseXp ベースXP量
     * @param multiplier 乗数（難易度、環境ボーナスなど）
     * @return レベルアップした場合true
     */
    public static boolean addXp(ItemStack stack, long baseXp, float multiplier) {
        // ANVILツールか確認
        if (!(stack.getItem() instanceof AnvilToolItem)) {
            return false;
        }

        // ツールデータを取得
        AnvilToolData data = stack.get(AnvilDataComponents.TOOL_DATA.get());
        if (data == null) {
            data = AnvilToolData.DEFAULT;
        }

        // 最大レベルに達している場合はXPを追加しない
        int maxLevel = Config.MAX_LEVEL.get();
        if (data.level() >= maxLevel) {
            return false;
        }

        // XP倍率を適用
        float configMultiplier = Config.XP_MULTIPLIER.get().floatValue();
        long xpToAdd = (long) (baseXp * multiplier * configMultiplier);

        if (xpToAdd <= 0) {
            return false;
        }

        // 現在のXPに加算
        long newCurrentXp = data.currentXp() + xpToAdd;
        long newTotalXp = data.totalXp() + xpToAdd;
        int newLevel = data.level();

        // レベルアップチェック
        boolean leveledUp = false;
        long xpForNextLevel = XpCalculator.getXpForNextLevel(newLevel);

        while (newCurrentXp >= xpForNextLevel && newLevel < maxLevel) {
            newCurrentXp -= xpForNextLevel;
            newLevel++;
            leveledUp = true;
            xpForNextLevel = XpCalculator.getXpForNextLevel(newLevel);
        }

        // 最大レベル到達時は余剰XPを0にする
        if (newLevel >= maxLevel) {
            newLevel = maxLevel;
            newCurrentXp = 0;
        }

        // ツールデータを更新
        AnvilToolData newData = data.withXp(newLevel, newCurrentXp, newTotalXp);
        stack.set(AnvilDataComponents.TOOL_DATA.get(), newData);

        return leveledUp;
    }

    /**
     * ツールにXPを付与（乗数なし）
     *
     * @param stack ツールのItemStack
     * @param xp XP量
     * @return レベルアップした場合true
     */
    public static boolean addXp(ItemStack stack, long xp) {
        return addXp(stack, xp, 1.0f);
    }

    /**
     * XPソースからXPを付与
     *
     * @param stack ツールのItemStack
     * @param source XPソース
     * @param multiplier 乗数
     * @return レベルアップした場合true
     */
    public static boolean addXp(ItemStack stack, XpSource source, float multiplier) {
        int baseXp = source.getRandomXp(new java.util.Random());
        return addXp(stack, baseXp, multiplier);
    }

    // ============================================
    // レベルアップ通知
    // ============================================

    /**
     * レベルアップ通知を送信
     *
     * @param player プレイヤー
     * @param stack ツール
     * @param oldLevel 旧レベル
     * @param newLevel 新レベル
     */
    public static void notifyLevelUp(ServerPlayer player, ItemStack stack, int oldLevel, int newLevel) {
        // TODO: レベルアップメッセージを表示
        // TODO: サウンドを再生
        // TODO: マイルストーンチェック（10レベルごと、100レベルごと等）

        ANVIL.LOGGER.debug("ツールがレベルアップ: {} -> {} (プレイヤー: {})",
                oldLevel, newLevel, player.getName().getString());
    }

    // ============================================
    // マイルストーン
    // ============================================

    /**
     * マイルストーンレベルかどうかをチェック
     *
     * @param level レベル
     * @return マイルストーンの場合true
     */
    public static boolean isMilestone(int level) {
        // 10レベルごと: ステータス選択
        // 50レベルごと: 特性選択
        // 100レベルごと: MODスロット
        // 500レベルごと: コアボックス拡張
        // 1000レベルごと: 特殊報酬
        return level % 10 == 0;
    }

    /**
     * マイルストーンの種類を取得
     *
     * @param level レベル
     * @return マイルストーン種類
     */
    public static MilestoneType getMilestoneType(int level) {
        if (level % 1000 == 0) {
            return MilestoneType.SPECIAL_REWARD;
        } else if (level % 500 == 0) {
            return MilestoneType.CORE_BOX_EXPANSION;
        } else if (level % 100 == 0) {
            return MilestoneType.MOD_SLOT;
        } else if (level % 50 == 0) {
            return MilestoneType.TRAIT_SELECTION;
        } else if (level % 10 == 0) {
            return MilestoneType.STAT_SELECTION;
        }
        return MilestoneType.NONE;
    }

    /**
     * マイルストーン種類
     */
    public enum MilestoneType {
        /** マイルストーンなし */
        NONE,
        /** ステータス選択（10レベルごと） */
        STAT_SELECTION,
        /** 特性選択（50レベルごと） */
        TRAIT_SELECTION,
        /** MODスロット追加（100レベルごと） */
        MOD_SLOT,
        /** コアボックス拡張（500レベルごと） */
        CORE_BOX_EXPANSION,
        /** 特殊報酬（1000レベルごと） */
        SPECIAL_REWARD
    }

    // ============================================
    // ユーティリティ
    // ============================================

    /**
     * ツールの現在レベルを取得
     *
     * @param stack ツール
     * @return レベル、ANVILツールでない場合は0
     */
    public static int getLevel(ItemStack stack) {
        AnvilToolData data = stack.get(AnvilDataComponents.TOOL_DATA.get());
        return data != null ? data.level() : 0;
    }

    /**
     * ツールの現在XPを取得
     *
     * @param stack ツール
     * @return 現在XP
     */
    public static long getCurrentXp(ItemStack stack) {
        AnvilToolData data = stack.get(AnvilDataComponents.TOOL_DATA.get());
        return data != null ? data.currentXp() : 0;
    }

    /**
     * ツールの累計XPを取得
     *
     * @param stack ツール
     * @return 累計XP
     */
    public static long getTotalXp(ItemStack stack) {
        AnvilToolData data = stack.get(AnvilDataComponents.TOOL_DATA.get());
        return data != null ? data.totalXp() : 0;
    }

    /**
     * 次のレベルまでの進捗率を取得
     *
     * @param stack ツール
     * @return 進捗率（0.0 - 1.0）
     */
    public static float getProgress(ItemStack stack) {
        AnvilToolData data = stack.get(AnvilDataComponents.TOOL_DATA.get());
        if (data == null) {
            return 0f;
        }
        return XpCalculator.calculateProgress(data.level(), data.currentXp());
    }

    // コンストラクタを非公開
    private LevelingManager() {}
}
