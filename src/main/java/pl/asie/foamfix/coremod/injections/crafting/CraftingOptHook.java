package pl.asie.foamfix.coremod.injections.crafting;

import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public final class CraftingOptHook {
	private CraftingOptHook() {

	}

	public static IRecipe findMatchingRecipe(InventoryCrafting crafting, World world, InventoryCraftResult result) {
		if (result instanceof IFoamFixCraftResult) {
			IRecipe recipe = ((IFoamFixCraftResult) result).foamfix_getLastRecipeUsed();
			if (recipe != null && recipe.matches(crafting, world)) {
				return recipe;
			}
		}
		return CraftingManager.findMatchingRecipe(crafting, world);
	}
}
