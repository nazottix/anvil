package io.github.nazottix.anvil.data;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.assembly.CalculatedStats;
import io.github.nazottix.anvil.data.component.AnvilToolData;
import io.github.nazottix.anvil.grid.GridConfiguration;
import io.github.nazottix.anvil.mod.ModConfiguration;
import io.github.nazottix.anvil.skill.SkillAllocation;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * ANVILのData Components登録クラス
 *
 * Minecraft 1.21+では、アイテムにカスタムデータを保存するために
 * Data Componentsシステムを使用します。これは従来のNBTタグに代わる
 * 新しい方式で、より型安全で効率的なデータ管理が可能です。
 *
 * 各コンポーネントは以下を定義する必要があります:
 * - Codec: データの保存/読み込み用（NBT、JSON）
 * - StreamCodec: ネットワーク通信用（クライアント-サーバー間）
 *
 * 使用例:
 * // データを取得
 * AnvilToolData data = itemStack.get(AnvilDataComponents.TOOL_DATA.get());
 *
 * // データを設定
 * itemStack.set(AnvilDataComponents.TOOL_DATA.get(), new AnvilToolData(1, 0L));
 */
public class AnvilDataComponents {

    // Data Components登録用のDeferredRegister
    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ANVIL.MODID);

    /**
     * ツールデータコンポーネント
     *
     * ANVILツールの主要データ（レベル、経験値など）を保存します。
     * このコンポーネントはANVILで作成されたツールアイテムに付与されます。
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AnvilToolData>> TOOL_DATA =
            DATA_COMPONENTS.register("tool_data", () ->
                    DataComponentType.<AnvilToolData>builder()
                            // 永続化用Codec（ワールド保存時に使用）
                            .persistent(AnvilToolData.CODEC)
                            // ネットワーク同期用StreamCodec
                            .networkSynchronized(AnvilToolData.STREAM_CODEC)
                            .build()
            );

    /**
     * 計算済みステータスコンポーネント
     *
     * ツールの計算されたステータス（耐久値、採掘速度、攻撃力など）を保存します。
     * これはパーツ構成から計算された最終値をキャッシュするために使用されます。
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CalculatedStats>> CALCULATED_STATS =
            DATA_COMPONENTS.register("calculated_stats", () ->
                    DataComponentType.<CalculatedStats>builder()
                            // 永続化用Codec
                            .persistent(CalculatedStats.CODEC)
                            // ネットワーク同期用StreamCodec
                            .networkSynchronized(CalculatedStats.STREAM_CODEC)
                            .build()
            );

    /**
     * MOD構成コンポーネント
     *
     * ツールに装着されているMODの構成（オーラ、通常MOD、エクシルス）を保存します。
     * 容量システム、極性システムの情報も含みます。
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ModConfiguration>> MOD_CONFIG =
            DATA_COMPONENTS.register("mod_config", () ->
                    DataComponentType.<ModConfiguration>builder()
                            // 永続化用Codec
                            .persistent(ModConfiguration.CODEC)
                            // ネットワーク同期用StreamCodec
                            .networkSynchronized(ModConfiguration.STREAM_CODEC)
                            .build()
            );

    /**
     * グリッド構成コンポーネント
     *
     * ツールのコアボックス（グリッドモジュール配置）構成を保存します。
     * モジュールの配置位置、回転、重量システムの情報を含みます。
     *
     * 仕様書参照: docs/04_グリッド配置システム.md
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GridConfiguration>> GRID_CONFIG =
            DATA_COMPONENTS.register("grid_config", () ->
                    DataComponentType.<GridConfiguration>builder()
                            // 永続化用Codec
                            .persistent(GridConfiguration.CODEC)
                            // ネットワーク同期用StreamCodec
                            .networkSynchronized(GridConfiguration.STREAM_CODEC)
                            .build()
            );

    /**
     * スキル配分コンポーネント
     *
     * ツールのスキルツリー配分（解放済みノード、装着ジュエル）を保存します。
     * Path of Exile風のスキルツリーシステムで使用されます。
     *
     * 仕様書参照: docs/03_スキルツリーシステム.md
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SkillAllocation>> SKILL_ALLOCATION =
            DATA_COMPONENTS.register("skill_allocation", () ->
                    DataComponentType.<SkillAllocation>builder()
                            // 永続化用Codec
                            .persistent(SkillAllocation.CODEC)
                            // ネットワーク同期用StreamCodec
                            .networkSynchronized(SkillAllocation.STREAM_CODEC)
                            .build()
            );

    /**
     * Data ComponentsをMODイベントバスに登録
     *
     * この方法はANVIL.javaのコンストラクタから呼び出されます。
     *
     * @param modEventBus MODイベントバス
     */
    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
        ANVIL.LOGGER.info("ANVIL: Data Componentsを登録");
    }
}
