package io.github.nazottix.anvil.guide;

import io.github.nazottix.anvil.ANVIL;
import net.minecraft.client.Minecraft;
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
        // GuideMEのデータ駆動型ガイド（Markdownファイル）を使用
        // ガイドはassets/anvil/guides/anvil/guide/に配置されたMarkdownファイルから自動的に読み込まれます
        ANVIL.LOGGER.debug("GuideMEデータ駆動型ガイドを使用します。");
    }

    /**
     * ガイドを開く（クライアント側のみ）
     *
     * GuideMEのAPIを使用してガイドスクリーンを表示します。
     * このメソッドはクライアント側でのみ呼び出してください。
     */
    public static void openGuide() {
        if (!isGuideMeAvailable()) {
            ANVIL.LOGGER.warn("GuideMEが利用できないため、ガイドを開けません。");
            return;
        }

        try {
            // GuideMEのAPIを使用してガイドを開く
            // リフレクションを使用してGuideMEのinternalクラスにアクセス
            openGuideInternal();
        } catch (Exception e) {
            ANVIL.LOGGER.error("ガイドを開く際にエラーが発生しました: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * GuideME内部APIを使用してガイドを開く
     *
     * GuideMEのinternalパッケージにアクセスするためリフレクションを使用します。
     */
    private static void openGuideInternal() {
        try {
            // GuideMEProxy.instance().openGuide(player, guideId) を呼び出す
            Class<?> proxyClass = Class.forName("guideme.internal.GuideMEProxy");
            Object proxyInstance = proxyClass.getMethod("instance").invoke(null);

            // openGuide(Player player, ResourceLocation guideId) を呼び出す
            var player = Minecraft.getInstance().player;
            if (player != null) {
                proxyClass.getMethod("openGuide", net.minecraft.world.entity.player.Player.class, ResourceLocation.class)
                        .invoke(proxyInstance, player, GUIDE_ID);
                ANVIL.LOGGER.debug("ガイドを開きました: {}", GUIDE_ID);
            }
        } catch (ClassNotFoundException e) {
            ANVIL.LOGGER.error("GuideMEProxyクラスが見つかりません。GuideMEのバージョンを確認してください。");
        } catch (NoSuchMethodException e) {
            ANVIL.LOGGER.error("GuideMEProxyのメソッドが見つかりません: {}", e.getMessage());
        } catch (Exception e) {
            ANVIL.LOGGER.error("ガイドを開く際にエラーが発生しました: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
