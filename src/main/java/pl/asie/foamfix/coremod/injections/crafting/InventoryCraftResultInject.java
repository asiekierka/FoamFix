/*
 * Copyright (C) 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of FoamFix.
 *
 * FoamFix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoamFix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FoamFix.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or
 * combining it with the Minecraft game engine, the Mojang Launchwrapper,
 * the Mojang AuthLib and the Minecraft Realms library (and/or modified
 * versions of said software), containing parts covered by the terms of
 * their respective licenses, the licensors of this Program grant you
 * additional permission to convey the resulting work.
 */

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
