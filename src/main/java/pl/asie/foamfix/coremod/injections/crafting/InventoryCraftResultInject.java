package pl.asie.foamfix.coremod.injections.crafting;

import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.item.crafting.IRecipe;

import javax.annotation.Nullable;

public class InventoryCraftResultInject extends InventoryCraftResult implements IFoamFixCraftResult {
	private IRecipe foamfix_lastRecipeUsed;

	@Override
	public void setRecipeUsed(@Nullable IRecipe r) {
		if (r != null) {
			this.foamfix_lastRecipeUsed = r;
		} else if (getRecipeUsed() != null) {
			this.foamfix_lastRecipeUsed = getRecipeUsed();
		}
		setRecipeUsed_foamfix_old(r);
	}

	public void setRecipeUsed_foamfix_old(@Nullable IRecipe r) {

	}

	@Override
	public IRecipe foamfix_getLastRecipeUsed() {
		return foamfix_lastRecipeUsed;
	}
}
