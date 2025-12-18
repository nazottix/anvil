package io.github.nazottix.anvil.compat.jei;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.block.AnvilBlocks;
import io.github.nazottix.anvil.item.AnvilItems;
import io.github.nazottix.anvil.item.PartItem;
import io.github.nazottix.anvil.material.Grade;
import io.github.nazottix.anvil.material.Material;
import io.github.nazottix.anvil.material.MaterialRegistry;
import io.github.nazottix.anvil.tool.PartType;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * ANVIL JEIプラグイン
 *
 * JEIとの連携を行うメインプラグインクラスです。
 * Part Forgeのレシピ表示を担当します。
 */
@JeiPlugin
public class AnvilJEIPlugin implements IModPlugin {

    // プラグインUID
    private static final ResourceLocation PLUGIN_UID = ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    /**
     * レシピカテゴリを登録
     */
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        ANVIL.LOGGER.info("ANVIL: JEIレシピカテゴリを登録");

        // Part Forgeカテゴリを登録
        registration.addRecipeCategories(
                new PartForgeRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    /**
     * レシピを登録
     */
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ANVIL.LOGGER.info("ANVIL: JEIレシピを登録");

        // Part Forgeレシピを生成・登録
        List<PartForgeRecipe> partForgeRecipes = createPartForgeRecipes();
        registration.addRecipes(PartForgeRecipeCategory.RECIPE_TYPE, partForgeRecipes);

        ANVIL.LOGGER.info("ANVIL: {}件のPart Forgeレシピを登録しました", partForgeRecipes.size());
    }

    /**
     * レシピカタリスト（クラフト台）を登録
     */
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        ANVIL.LOGGER.info("ANVIL: JEIレシピカタリストを登録");

        // Part Forgeブロックをカタリストとして登録
        registration.addRecipeCatalyst(
                new ItemStack(AnvilBlocks.PART_FORGE.get()),
                PartForgeRecipeCategory.RECIPE_TYPE
        );
    }

    /**
     * Part Forgeレシピを生成
     *
     * すべての素材×パーツタイプの組み合わせからレシピを生成します。
     *
     * @return Part Forgeレシピのリスト
     */
    private List<PartForgeRecipe> createPartForgeRecipes() {
        List<PartForgeRecipe> recipes = new ArrayList<>();

        // MaterialRegistryから全素材を取得
        MaterialRegistry registry = MaterialRegistry.getInstance();

        // 素材ごとにレシピを生成
        for (Material material : registry.getAll()) {
            // 修理アイテム（素材原料）を取得
            if (material.getRepairItem() == null) {
                continue; // 修理アイテムがない素材はスキップ
            }

            ItemStack input = new ItemStack(material.getRepairItem());

            // 各パーツタイプに対してレシピを生成
            for (PartType partType : PartType.values()) {
                // 出力パーツを生成（グレードCをデフォルトとして表示）
                ItemStack output = PartItem.createPartStack(
                        partType,
                        material.getId().toString(),
                        Grade.C  // JEI表示用のデフォルトグレード
                );

                recipes.add(new PartForgeRecipe(material, partType, input, output));
            }
        }

        return recipes;
    }
}
