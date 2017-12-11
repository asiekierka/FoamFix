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

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pl.asie.foamfix.FoamFix;

import javax.vecmath.Vector3f;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public final class FoamUtils {
	public static final MethodHandle MLR_GET_TEXTURES;
	public static final MethodHandle ML_LOAD_BLOCK;

	static {
		MethodHandle MLR_GET_TEXTURES_TMP = null;

		try {
			Class k = Class.forName("net.minecraftforge.client.model.ModelLoaderRegistry");
			Method m = k.getDeclaredMethod("getTextures");
			m.setAccessible(true);
			MLR_GET_TEXTURES_TMP = MethodHandles.lookup().unreflect(m);
		} catch (Exception e) {
			e.printStackTrace();
		}

		MLR_GET_TEXTURES = MLR_GET_TEXTURES_TMP;

		MethodHandle ML_LOAD_BLOCK_TMP = null;

		try {
			Class k = Class.forName("net.minecraft.client.renderer.block.model.ModelBakery");
			Method m = k.getDeclaredMethod("loadBlock", BlockStateMapper.class, Block.class, ResourceLocation.class);
			m.setAccessible(true);
			ML_LOAD_BLOCK_TMP = MethodHandles.lookup().unreflect(m);
		} catch (Exception e) {
			e.printStackTrace();
		}

		ML_LOAD_BLOCK = ML_LOAD_BLOCK_TMP;
	}

	private FoamUtils() {
	}

	public static void wipeModelLoaderRegistryCache() {
	    Field resourceCacheField = ReflectionHelper.findField(ModelLoaderRegistry.class, "cache");
	    try {
			Map<ResourceLocation, IModel> oldResourceCache = (Map<ResourceLocation, IModel>) resourceCacheField.get(null);
			int itemsCleared = 0;
		    FoamFix.logger.info("Clearing ModelLoaderRegistry cache (" + oldResourceCache.size() + " items)...");
			for (ResourceLocation r : Sets.newHashSet(oldResourceCache.keySet())) {
				// System.out.println(r + " -> " + oldResourceCache.get(r).getClass().getName());
				oldResourceCache.remove(r);
				itemsCleared++;
			}
			FoamFix.logger.info("Cleared " + itemsCleared + " objects.");
	    } catch (IllegalAccessException e) {
	        e.printStackTrace();
	    }
	}
}
