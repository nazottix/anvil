package io.github.nazottix.anvil.compat.jei;

import io.github.nazottix.anvil.material.Material;
import io.github.nazottix.anvil.tool.PartType;
import net.minecraft.world.item.ItemStack;

/**
 * Part Forge JEIレシピデータクラス
 *
 * Part Forgeでの素材→パーツ変換レシピを表現します。
 * JEIで表示するためのデータを保持します。
 */
public record PartForgeRecipe(
        Material material,
        PartType partType,
        ItemStack input,
        ItemStack output
) {
    /**
     * レシピの一意なIDを取得
     *
     * @return レシピID
     */
    public String getId() {
        return material.getId().toString() + "_" + partType.getId();
    }
}
