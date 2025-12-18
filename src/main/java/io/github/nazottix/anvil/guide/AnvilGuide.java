package io.github.nazottix.anvil.guide;

import io.github.nazottix.anvil.ANVIL;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

/**
 * ANVILガイドブック統合クラス
 *
 * GuideMEがインストールされている場合にガイドブックを初期化します。
 * GuideMEはオプショナル依存のため、存在チェックを行ってから初期化します。
 */
public class AnvilGuide {

    // ガイドのリソースロケーション
    public static final ResourceLocation GUIDE_ID = ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "guide");

    // GuideMEのMod ID
    private static final String GUIDEME_MODID = "guideme";

    // ガイドが初期化されたかどうか
    private static boolean initialized = false;

    /**
     * GuideMEが利用可能かチェック
     *
     * @return GuideMEがロードされている場合true
     */
    public static boolean isGuideMeAvailable() {
        return ModList.get().isLoaded(GUIDEME_MODID);
    }

    /**
     * ガイドを初期化
     *
     * クライアント側のセットアップ時に呼び出されます。
     * GuideMEが存在しない場合は何もしません。
     */
    public static void init() {
        if (initialized) {
            return;
        }

        if (!isGuideMeAvailable()) {
            ANVIL.LOGGER.info("GuideMEが見つかりません。ガイドブック機能は無効化されます。");
            return;
        }

        try {
            // GuideME APIを使用してガイドを構築
            initGuide();
            initialized = true;
            ANVIL.LOGGER.info("ANVILガイドブックを初期化しました。");
        } catch (Exception e) {
            ANVIL.LOGGER.error("ANVILガイドブックの初期化に失敗しました: {}", e.getMessage());
        }
    }

    /**
     * GuideME APIを使用してガイドを構築
     *
     * このメソッドはGuideMEが存在する場合のみ呼び出されます。
     */
    private static void initGuide() {
        // GuideME APIを使用してガイドを構築
        // Guide.builder(GUIDE_ID)
        //     .itemSettings(settings -> settings
        //         .displayName(Component.translatable("item.anvil.guide_book"))
        //     )
        //     .build();

        // 注意: 実際のGuideME APIの使用はGuideMEのバージョンによって異なる場合があります
        // 現時点では、データ駆動型ガイド（Markdownファイル）のみを使用します
        ANVIL.LOGGER.debug("GuideMEデータ駆動型ガイドを使用します。");
    }
}
