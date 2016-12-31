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
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.PersistentRegistryManager;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import pl.asie.foamfix.shared.FoamFixConfig;
import pl.asie.foamfix.shared.FoamFixShared;
import pl.asie.foamfix.common.PretendPackageMap;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.BitSet;
import java.util.Map;
import java.util.Set;

public class ProxyCommon {
	private void optimizeLaunchWrapper() {
		LaunchClassLoader loader = (LaunchClassLoader) this.getClass().getClassLoader();
		Field resourceCacheField = ReflectionHelper.findField(LaunchClassLoader.class, "resourceCache");
		Field packageManifestsField = ReflectionHelper.findField(LaunchClassLoader.class, "packageManifests");
		if (FoamFixShared.config.lwWeakenResourceCache) {
			FoamFix.logger.info("Weakening LaunchWrapper resource cache...");
			try {
				Map oldResourceCache = (Map) resourceCacheField.get(loader);
				Map newResourceCache = CacheBuilder.newBuilder().weakValues().build().asMap();
				newResourceCache.putAll(oldResourceCache);
				resourceCacheField.set(loader, newResourceCache);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		if (FoamFixShared.config.lwDummyPackageManifestMap) {
			FoamFix.logger.info("Dummying out LaunchWrapper's unused package manifests...");
			try {
				// Map<Package, Manifest> packageManifests = (Map) packageManifestsField.get(loader);
				packageManifestsField.set(loader, new PretendPackageMap());
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	private void optimizeForgeRegistries() {
		// BitSets scale dynamically, you really don't need to preallocate 8MB for 64 million IDs... *yawn*
		try {
			int optimizedRegs = 0;
			int optimizedSavings = 0;

			Class persistentRegistryClass = Class.forName("net.minecraftforge.fml.common.registry.PersistentRegistryManager$PersistentRegistry");
			Field biMapField = persistentRegistryClass.getDeclaredField("registries");
			Field availMapField = FMLControlledNamespacedRegistry.class.getDeclaredField("availabilityMap");
			Field sizeStickyField = BitSet.class.getDeclaredField("sizeIsSticky");
			Method trimToSizeMethod = BitSet.class.getDeclaredMethod("trimToSize");

			biMapField.setAccessible(true);
			availMapField.setAccessible(true);
			sizeStickyField.setAccessible(true);
			trimToSizeMethod.setAccessible(true);

			for (Object registryHolder : persistentRegistryClass.getEnumConstants()) {
				BiMap biMap = (BiMap) biMapField.get(registryHolder);
				for (FMLControlledNamespacedRegistry registry : (Set<FMLControlledNamespacedRegistry>) biMap.values()) {
					BitSet availMap = (BitSet) availMapField.get(registry);
					int size = availMap.size();
					if (size > 65536) {
						sizeStickyField.set(availMap, false);
						trimToSizeMethod.invoke(availMap);
						optimizedRegs++;
						optimizedSavings += ((size - availMap.size()) >> 3);
					}
				}
			}

			FoamFixShared.ramSaved += optimizedSavings;
			FoamFix.logger.info("Optimized " + optimizedRegs + " FML registries, saving " + optimizedSavings + " bytes.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void preInit() {
	}

	public void init() {
	}

	public void postInit() {
		optimizeLaunchWrapper();

		if (FoamFixShared.config.geDynamicRegistrySizeScaling) {
			optimizeForgeRegistries();
		}

		FoamFix.updateRamSaved();
	}
}
