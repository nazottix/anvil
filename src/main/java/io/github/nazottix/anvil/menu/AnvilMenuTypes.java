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

    /**
     * スキルツリーステーションメニュータイプ
     *
     * スキルツリーステーションブロックのメニュータイプです。
     * ツールのスキルポイント割り振りを行います。
     */
    public static final DeferredHolder<MenuType<?>, MenuType<SkillTreeStationMenu>> SKILL_TREE_STATION =
            MENU_TYPES.register("skill_tree_station",
                    () -> IMenuTypeExtension.create(SkillTreeStationMenu::new)
            );

    /**
     * ジュエルステーションメニュータイプ
     *
     * ジュエルステーションブロックのメニュータイプです。
     * ツールへのジュエル装着・取り外しを行います。
     */
    public static final DeferredHolder<MenuType<?>, MenuType<JewelStationMenu>> JEWEL_STATION =
            MENU_TYPES.register("jewel_station",
                    () -> IMenuTypeExtension.create(JewelStationMenu::new)
            );

    /**
     * リスペックステーションメニュータイプ
     *
     * リスペックステーションブロックのメニュータイプです。
     * ツールのスキルポイントリセットを行います。
     */
    public static final DeferredHolder<MenuType<?>, MenuType<RespecStationMenu>> RESPEC_STATION =
            MENU_TYPES.register("respec_station",
                    () -> IMenuTypeExtension.create(RespecStationMenu::new)
            );

    /**
     * コアボックスステーションメニュータイプ
     *
     * コアボックスステーションブロックのメニュータイプです。
     * テトリス風のモジュール配置を行います。
     */
    public static final DeferredHolder<MenuType<?>, MenuType<CoreBoxStationMenu>> CORE_BOX_STATION =
            MENU_TYPES.register("core_box_station",
                    () -> IMenuTypeExtension.create(CoreBoxStationMenu::new)
            );

    /**
     * MODステーションメニュータイプ
     *
     * MODステーションブロックのメニュータイプです。
     * ツールへのMOD装着・取り外しを行います。
     */
    public static final DeferredHolder<MenuType<?>, MenuType<ModStationMenu>> MOD_STATION =
            MENU_TYPES.register("mod_station",
                    () -> IMenuTypeExtension.create(ModStationMenu::new)
            );

    /**
     * リペアステーションメニュータイプ
     *
     * リペアステーションブロックのメニュータイプです。
     * ツールの修理を行います。
     */
    public static final DeferredHolder<MenuType<?>, MenuType<RepairStationMenu>> REPAIR_STATION =
            MENU_TYPES.register("repair_station",
                    () -> IMenuTypeExtension.create(RepairStationMenu::new)
            );

    /**
     * 精錬所メニュータイプ
     *
     * 精錬所ブロックのメニュータイプです。
     * バニラ素材を加工素材に精錬します。
     */
    public static final DeferredHolder<MenuType<?>, MenuType<RefineryMenu>> REFINERY =
            MENU_TYPES.register("refinery",
                    () -> IMenuTypeExtension.create(RefineryMenu::new)
            );

    /**
     * パーツ鍛造所メニュータイプ
     *
     * パーツ鍛造所ブロックのメニュータイプです。
     * 加工素材をパーツアイテムに変換します。
     */
    public static final DeferredHolder<MenuType<?>, MenuType<PartForgeMenu>> PART_FORGE =
            MENU_TYPES.register("part_forge",
                    () -> IMenuTypeExtension.create(PartForgeMenu::new)
            );

    /**
     * 鍛造ステーションメニュータイプ
     *
     * 鍛造ステーションブロックのメニュータイプです。
     * 精錬素材を鍛造素材に変換します。
     */
    public static final DeferredHolder<MenuType<?>, MenuType<ForgingStationMenu>> FORGING_STATION =
            MENU_TYPES.register("forging_station",
                    () -> IMenuTypeExtension.create(ForgingStationMenu::new)
            );

    /**
     * 研磨ステーションメニュータイプ
     *
     * 研磨ステーションブロックのメニュータイプです。
     * 鍛造素材を研磨素材に変換します。
     */
    public static final DeferredHolder<MenuType<?>, MenuType<PolishingStationMenu>> POLISHING_STATION =
            MENU_TYPES.register("polishing_station",
                    () -> IMenuTypeExtension.create(PolishingStationMenu::new)
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
