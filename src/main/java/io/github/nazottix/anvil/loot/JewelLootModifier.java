package io.github.nazottix.anvil.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.Config;
import io.github.nazottix.anvil.item.JewelItem;
import io.github.nazottix.anvil.skill.jewel.JewelData;
import io.github.nazottix.anvil.skill.jewel.JewelRegistry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * ジュエルドロップLoot Modifier
 *
 * モブ、釣り、チェストからジュエルをドロップさせるGlobal Loot Modifier。
 * ドロップ確率はConfig設定に基づき、レアリティはソース種別に応じてボーナスが付与されます。
 *
 * 仕様書参照: docs/03_スキルツリーシステム.md
 */
public class JewelLootModifier extends LootModifier {

    // ============================================
    // ドロップソース種別
    // ============================================

    /**
     * ドロップソースの種別を定義
     */
    public enum DropSource {
        /** 通常モブ（ゾンビ、スケルトン等） */
        NORMAL_MOB("normal_mob"),
        /** 強敵モブ（エンダーマン、ウィッチ等） */
        STRONG_MOB("strong_mob"),
        /** ボスモブ（ドラゴン、ウィザー） */
        BOSS_MOB("boss_mob"),
        /** 釣り報酬 */
        FISHING("fishing"),
        /** チェスト報酬 */
        CHEST("chest");

        private final String id;

        DropSource(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static DropSource fromId(String id) {
            for (DropSource source : values()) {
                if (source.id.equals(id)) {
                    return source;
                }
            }
            return NORMAL_MOB;
        }
    }

    // ============================================
    // フィールド
    // ============================================

    /** ドロップソース種別 */
    private final DropSource source;

    // ============================================
    // Codec定義
    // ============================================

    /**
     * JewelLootModifier用のMapCodec
     * NeoForge 1.21ではMapCodecを使用してLoot Modifierをシリアライズ
     */
    public static final MapCodec<JewelLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            // 親クラス(LootModifier)のconditionsフィールドを継承
            codecStart(instance).and(
                    // ドロップソース種別をエンコード/デコード
                    com.mojang.serialization.Codec.STRING
                            .xmap(DropSource::fromId, DropSource::getId)
                            .fieldOf("source")
                            .forGetter(m -> m.source)
            ).apply(instance, JewelLootModifier::new)
    );

    // ============================================
    // コンストラクタ
    // ============================================

    /**
     * JewelLootModifierコンストラクタ
     *
     * @param conditionsIn Loot条件（Loot Tableで定義）
     * @param source ドロップソース種別
     */
    public JewelLootModifier(LootItemCondition[] conditionsIn, DropSource source) {
        super(conditionsIn);
        this.source = source;
    }

    // ============================================
    // Loot Modifier実装
    // ============================================

    /**
     * Lootを修正してジュエルを追加
     *
     * @param generatedLoot 元々生成されたLoot
     * @param context Lootコンテキスト
     * @return 修正後のLootリスト
     */
    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(
            ObjectArrayList<ItemStack> generatedLoot,
            LootContext context) {

        // ジュエルドロップが無効の場合はスキップ
        if (!Config.ENABLE_JEWEL_DROP.get()) {
            return generatedLoot;
        }

        // ドロップ判定
        float dropChance = getDropChance();
        float rarityBonus = getRarityBonus();

        // 乱数でドロップ判定
        if (context.getRandom().nextFloat() * 100 < dropChance) {
            // ジュエルを生成
            Optional<JewelData> jewelOpt = JewelRegistry.getRandomJewel(
                    context.getRandom(),
                    rarityBonus
            );

            // ジュエルが取得できた場合はLootに追加
            if (jewelOpt.isPresent()) {
                JewelData jewel = jewelOpt.get();
                ItemStack jewelStack = JewelItem.createJewelStack(jewel.id());
                generatedLoot.add(jewelStack);

                // ログ出力（デバッグ用）
                ANVIL.LOGGER.debug("ANVIL: ジュエルをドロップ: {} ({})",
                        jewel.id(), jewel.rarity().getId());
            }
        }

        return generatedLoot;
    }

    /**
     * ドロップソースに応じたドロップ確率を取得
     *
     * @return ドロップ確率（%）
     */
    private float getDropChance() {
        return switch (source) {
            case NORMAL_MOB -> Config.JEWEL_DROP_CHANCE_NORMAL.get().floatValue();
            case STRONG_MOB -> Config.JEWEL_DROP_CHANCE_STRONG.get().floatValue();
            case BOSS_MOB -> Config.JEWEL_DROP_CHANCE_BOSS.get().floatValue();
            case FISHING -> Config.JEWEL_DROP_CHANCE_FISHING.get().floatValue();
            case CHEST -> Config.JEWEL_DROP_CHANCE_CHEST.get().floatValue();
        };
    }

    /**
     * ドロップソースに応じたレアリティボーナスを取得
     *
     * @return レアリティボーナス（%）
     */
    private float getRarityBonus() {
        return switch (source) {
            case NORMAL_MOB -> 0f;
            case STRONG_MOB -> Config.RARITY_BONUS_STRONG.get().floatValue();
            case BOSS_MOB -> Config.RARITY_BONUS_BOSS.get().floatValue();
            case FISHING -> Config.RARITY_BONUS_FISHING.get().floatValue();
            case CHEST -> Config.RARITY_BONUS_CHEST.get().floatValue();
        };
    }

    // ============================================
    // Codec取得
    // ============================================

    /**
     * このModifierのCodecを返す
     *
     * @return MapCodec<JewelLootModifier>
     */
    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
