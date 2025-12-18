package io.github.nazottix.anvil.compat.jei;

import io.github.nazottix.anvil.ANVIL;
import io.github.nazottix.anvil.block.AnvilBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Part Forge JEIレシピカテゴリ
 *
 * JEIでPart Forgeのレシピを表示するためのカテゴリです。
 * 素材アイテム → パーツアイテム の変換を表示します。
 */
public class PartForgeRecipeCategory implements IRecipeCategory<PartForgeRecipe> {

    // レシピタイプ定義
    public static final RecipeType<PartForgeRecipe> RECIPE_TYPE = RecipeType.create(
            ANVIL.MODID,
            "part_forge",
            PartForgeRecipe.class
    );

    // カテゴリUID
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ANVIL.MODID, "part_forge");

    // GUIサイズ
    private static final int GUI_WIDTH = 120;
    private static final int GUI_HEIGHT = 40;

    // アイコン
    private final IDrawable icon;

    /**
     * コンストラクタ
     *
     * @param guiHelper JEI GUIヘルパー
     */
    public PartForgeRecipeCategory(IGuiHelper guiHelper) {
        // アイコン（Part Forgeブロック）
        this.icon = guiHelper.createDrawableIngredient(
                VanillaTypes.ITEM_STACK,
                new ItemStack(AnvilBlocks.PART_FORGE.get())
        );
    }

    @Override
    public RecipeType<PartForgeRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        // カテゴリタイトル
        return Component.translatable("gui.jei.anvil.part_forge");
    }

    /**
     * GUI幅を取得（新しいJEI API）
     */
    @Override
    public int getWidth() {
        return GUI_WIDTH;
    }

    /**
     * GUI高さを取得（新しいJEI API）
     */
    @Override
    public int getHeight() {
        return GUI_HEIGHT;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PartForgeRecipe recipe, IFocusGroup focuses) {
        // 入力スロット（素材アイテム）
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 12)
                .addItemStack(recipe.input());

        // 出力スロット（パーツアイテム）
        builder.addSlot(RecipeIngredientRole.OUTPUT, 94, 12)
                .addItemStack(recipe.output());
    }

    /**
     * 矢印を描画するためのdraw override
     * 入力と出力の間に矢印を表示
     */
    @Override
    public void draw(PartForgeRecipe recipe, mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView,
                     net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // パーツタイプ名を中央に表示
        Component partTypeName = Component.translatable(recipe.partType().getTranslationKey());
        int textWidth = net.minecraft.client.Minecraft.getInstance().font.width(partTypeName);
        guiGraphics.drawString(
                net.minecraft.client.Minecraft.getInstance().font,
                partTypeName,
                (GUI_WIDTH - textWidth) / 2,
                3,
                0x404040,
                false
        );

        // 矢印を表示（→）
        guiGraphics.drawString(
                net.minecraft.client.Minecraft.getInstance().font,
                "→",
                52,
                15,
                0x404040,
                false
        );
    }
}
