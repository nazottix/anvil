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
     * グレードステーションメニュータイプ
     *
     * グレードステーションブロックのメニュータイプです。
     * パーツのグレードアップグレードを行います。
     */
    public static final DeferredHolder<MenuType<?>, MenuType<GradeStationMenu>> GRADE_STATION =
            MENU_TYPES.register("grade_station",
                    () -> IMenuTypeExtension.create(GradeStationMenu::new)
            );

    // 削除済み: 精錬所メニュータイプ (素材加工の簡略化のため)

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

    // 削除済み: 鍛造ステーションメニュータイプ (素材加工の簡略化のため)
    // 削除済み: 研磨ステーションメニュータイプ (素材加工の簡略化のため)

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
