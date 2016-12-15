/**
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
package pl.asie.foamfix;

import com.google.common.cache.CacheBuilder;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import pl.asie.foamfix.shared.FoamFixShared;
import pl.asie.foamfix.common.PretendPackageMap;

import java.lang.reflect.Field;
import java.util.Map;

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

	public void preInit() {
	}

	public void init() {
	}

	public void postInit() {
		optimizeLaunchWrapper();
	}
}
