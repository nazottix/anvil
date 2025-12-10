package io.github.nazottix.anvil.data;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.data.component.AnvilToolData;
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
