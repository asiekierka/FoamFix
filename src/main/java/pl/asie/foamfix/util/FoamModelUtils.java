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
/**
 * This file is part of FoamFixAPI.
 *
 * FoamFixAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoamFixAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FoamFixAPI.  If not, see <http://www.gnu.org/licenses/>.
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
package pl.asie.foamfix.util;

import gnu.trove.map.hash.THashMap;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.util.ResourceLocation;

import java.lang.invoke.MethodHandle;
import java.util.Map;

public final class FoamModelUtils {
	public static final MethodHandle PMW_GET_PARENT;
	// public static final MethodHandle MLR_GET_TEXTURES;
	// public static final MethodHandle ML_LOAD_BLOCK;

	static {
		/* MethodHandle MLR_GET_TEXTURES_TMP = null;

		try {
			Class k = Class.forName("net.minecraftforge.client.model.ModelLoaderRegistry");
			MLR_GET_TEXTURES_TMP = MethodHandleHelper.findMethod(k, "getTextures", "getTextures");
		} catch (Exception e) {
			e.printStackTrace();
		}

		MLR_GET_TEXTURES = MLR_GET_TEXTURES_TMP; */

		MethodHandle PMW_GET_PARENT_TMP = null;

		try {
			Class k = Class.forName("net.minecraftforge.client.model.PerspectiveMapWrapper");
			PMW_GET_PARENT_TMP = MethodHandleHelper.findFieldGetter(k, "parent");
		} catch (Exception e) {
			e.printStackTrace();
		}

		PMW_GET_PARENT = PMW_GET_PARENT_TMP;

		/* MethodHandle ML_LOAD_BLOCK_TMP = null;

		try {
			Class k = Class.forName("net.minecraft.client.renderer.block.model.ModelBakery");
			ML_LOAD_BLOCK_TMP = MethodHandleHelper.findMethod(k, "loadBlock", "loadBlock", BlockStateMapper.class, Block.class, ResourceLocation.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		ML_LOAD_BLOCK = ML_LOAD_BLOCK_TMP; */
	}

	private FoamModelUtils() {
	}
}
