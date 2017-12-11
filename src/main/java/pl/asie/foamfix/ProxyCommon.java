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
package pl.asie.foamfix;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.BiMap;
import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import pl.asie.foamfix.client.Deduplicator;
import pl.asie.foamfix.common.TileEntityFasterHopper;
import pl.asie.foamfix.shared.FoamFixShared;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.jar.Manifest;

public class ProxyCommon {
	public void preInit() {
		if (FoamFixShared.config.geFasterHopper) {
			TileEntity.register("hopper", TileEntityFasterHopper.class);
		}

		if (FoamFixShared.config.lwRemovePackageManifestMap) {
			FoamFix.logger.info("Removing LaunchWrapper package manifest map...");
			try {
				LaunchClassLoader loader = (LaunchClassLoader) this.getClass().getClassLoader();

				Field pmField = ReflectionHelper.findField(LaunchClassLoader.class, "packageManifests");
				pmField.set(loader, new Map<Package, Manifest>() {
					@Override
					public int size() {
						return 0;
					}

					@Override
					public boolean isEmpty() {
						return true;
					}

					@Override
					public boolean containsKey(Object o) {
						return false;
					}

					@Override
					public boolean containsValue(Object o) {
						return false;
					}

					@Override
					public Manifest get(Object o) {
						return null;
					}

					@Override
					public Manifest put(Package o, Manifest o2) {
						return o2;
					}

					@Override
					public Manifest remove(Object o) {
						return null;
					}

					@Override
					public void putAll(Map map) {

					}

					@Override
					public void clear() {

					}

					@Override
					public Set keySet() {
						return Collections.emptySet();
					}

					@Override
					public Collection values() {
						return Collections.emptySet();
					}

					@Override
					public Set<Entry<Package, Manifest>> entrySet() {
						return Collections.emptySet();
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void init() {
		if (FoamFixShared.config.lwWeakenResourceCache) {
			FoamFix.logger.info("Weakening LaunchWrapper resource cache...");
			try {
				LaunchClassLoader loader = (LaunchClassLoader) this.getClass().getClassLoader();

				Field resourceCacheField = ReflectionHelper.findField(LaunchClassLoader.class, "resourceCache");
				Map oldResourceCache = (Map) resourceCacheField.get(loader);
				Map newResourceCache = CacheBuilder.newBuilder().weakValues().build().asMap();
				newResourceCache.putAll(oldResourceCache);
				resourceCacheField.set(loader, newResourceCache);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void postInit() {
		FoamFix.updateRamSaved();
	}

    public void refreshResources() {
    }

	public void updateFasterAnimationFlag() {

	}
}
