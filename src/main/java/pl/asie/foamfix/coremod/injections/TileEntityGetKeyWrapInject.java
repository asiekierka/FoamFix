/*
 * Copyright (C) 2016, 2017 Adrian Siekierka
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

package pl.asie.foamfix.coremod.injections;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import pl.asie.foamfix.FoamFix;

public class TileEntityGetKeyWrapInject {
	public static ResourceLocation getKey(Class<? extends TileEntity> c) {
		ResourceLocation loc = FoamFix.TILE_OVERRIDES.get(c);
		return loc != null ? loc : TileEntityGetKeyWrapInject.getKey_foamfix_old(c);
	}

	public static ResourceLocation getKey_foamfix_old(Class<? extends TileEntity> c) {
		return null;
	}
}
