package io.github.nazottix.anvil.menu;

import io.github.nazottix.anvil.ANVIL;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * ANVILメニュータイプ登録クラス
 *
 * すべてのANVILメニュー（GUI）タイプをここで登録します。
 */
public class AnvilMenuTypes {

    // メニュータイプ登録用のDeferredRegister
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, ANVIL.MODID);

    // ============================================
    // メニュータイプ登録
    // ============================================

    /**
     * ツールステーションメニュータイプ
     *
     * ツール作成画面のメニュータイプです。
     * IMenuTypeExtensionを使用して追加データ（ブロック位置）を送信します。
     */
    public static final DeferredHolder<MenuType<?>, MenuType<ToolStationMenu>> TOOL_STATION =
            MENU_TYPES.register("tool_station",
                    () -> IMenuTypeExtension.create(ToolStationMenu::new)
            );

    /**
     * MODカスタマイズメニュータイプ
     *
     * ツールにMODを装着・変更する画面のメニュータイプです。
     * Warframe風のMODスロットシステムを実装。
     */
    public static final DeferredHolder<MenuType<?>, MenuType<ModCustomizationMenu>> MOD_CUSTOMIZATION =
            MENU_TYPES.register("mod_customization",
                    () -> IMenuTypeExtension.create(ModCustomizationMenu::new)
            );

    /**
     * グリッドカスタマイズメニュータイプ
     *
     * ツールのコアボックスにモジュールを配置する画面のメニュータイプです。
     * Path of Exile風のテトリス配置システムを実装。
     */
    public static final DeferredHolder<MenuType<?>, MenuType<GridCustomizationMenu>> GRID_CUSTOMIZATION =
            MENU_TYPES.register("grid_customization",
                    () -> IMenuTypeExtension.create(GridCustomizationMenu::new)
            );

    /**
     * スキルツリーメニュータイプ
     *
     * ツールのスキルツリー画面のメニュータイプです。
     * Path of Exile風の大規模スキルツリーシステムを実装。
     */
    public static final DeferredHolder<MenuType<?>, MenuType<SkillTreeMenu>> SKILL_TREE =
            MENU_TYPES.register("skill_tree",
                    () -> IMenuTypeExtension.create(SkillTreeMenu::new)
            );

    // ============================================
    // 登録
    // ============================================

    /**
     * メニュータイプをMODイベントバスに登録
     *
     * @param modEventBus MODイベントバス
     */
    public static void register(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
        ANVIL.LOGGER.info("ANVIL: メニュータイプを登録");
    }
}
