/**
 * Copyright (C) 2016, 2017, 2018, 2019, 2020, 2021 Adrian Siekierka
 *
 * This file is part of FoamFix.
 *
 * FoamFix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoamFix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FoamFix.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.asie.foamfix.client.jei;

import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.ingredients.IngredientFilter;
import net.minecraft.client.util.SearchTree;
import net.minecraft.item.ItemStack;
import pl.asie.foamfix.coremod.patches.jei.SearchTreeJEIPatchGlue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SearchTreeJEIItems extends SearchTree<ItemStack> {
	public SearchTreeJEIItems() {
		super(null, null);
	}

	@Override
	public void recalculate() {
		// NOP
	}

	@Override
	public void add(ItemStack element) {
		// NOP
	}

	@Override
	public List<ItemStack> search(String searchText) {
		try {
			return ((List<IIngredientListElement>) SearchTreeJEIPatchGlue.GET_INGREDIENT_LIST_UNCACHED.invokeExact(
					(IngredientFilter) SearchTreeJEIPatchGlue.src, searchText
			)).stream()
			.filter((a) -> a != null && a.getIngredient() instanceof ItemStack)
			.map((a) -> (ItemStack) a.getIngredient())
			.collect(Collectors.toList());
		} catch (Throwable e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}
}
