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
import java.util.BitSet;
import java.util.Map;
import java.util.Set;

public class ProxyCommon {
	private void optimizeLaunchWrapper() {
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

	public void preInit() {
		if (FoamFixShared.config.geFasterHopper) {
			TileEntity.register("hopper", TileEntityFasterHopper.class);
		}
	}

	public void init() {
	}

	public void postInit() {
		optimizeLaunchWrapper();

		FoamFix.updateRamSaved();
	}

    public void refreshResources() {
    }
}
