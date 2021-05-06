/*
 * Copyright (C) 2016, 2017, 2018, 2019, 2020, 2021 Adrian Siekierka
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

package pl.asie.foamfix.coremod.patches.jei;

import net.minecraftforge.fml.common.Loader;
import pl.asie.foamfix.util.MethodHandleHelper;

import java.lang.invoke.MethodHandle;

public class SearchTreeJEIPatchGlue {
	public static MethodHandle GET_INGREDIENT_LIST_UNCACHED;
	public static Object src;
	private static boolean initialized;

	public static boolean isValid() {
		if (!initialized) {
			if (Loader.isModLoaded("jei")) {
				try {
					GET_INGREDIENT_LIST_UNCACHED = MethodHandleHelper.findMethod(
							Class.forName("mezz.jei.ingredients.IngredientFilter"),
							"getIngredientListUncached", "getIngredientListUncached",
							String.class
					);
				} catch (Exception e) {
					GET_INGREDIENT_LIST_UNCACHED = null;
				}
			}

			initialized = true;
		}

		return GET_INGREDIENT_LIST_UNCACHED != null;
	}
}
