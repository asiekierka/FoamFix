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
package pl.asie.foamfix.shared;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class FoamFixConfig {
	public boolean lwWeakenResourceCache, lwDummyPackageManifestMap;
	public boolean clDeduplicate, clCleanRedundantModelRegistry;
	public boolean geBlockPosPatch, clTextureDoubleBuffering;
	public boolean geDynamicRegistrySizeScaling;
	public boolean geSmallPropertyStorage = true; // TODO
	public int clDeduplicateRecursionLevel;

	private Configuration config;

	public void init(File file, boolean isCoremod) {
		if (config == null) {
			config = new Configuration(file);

			lwDummyPackageManifestMap = config.getBoolean("dummyPackageManifestMap", "launchwrapper", true, "Dummy out LaunchWrapper's unused package manifest map. This will only break things if some other mod reflects into the LaunchClassLoader to get the private map, which as far as I know is not the case.");
			lwWeakenResourceCache = config.getBoolean("weakenResourceCache", "launchwrapper", true, "Weaken LaunchWrapper's byte[] resource cache to make it cleanuppable by the GC. Safe.");
			clDeduplicate = config.getBoolean("deduplicateModels", "client", true, "Enable deduplication of redundant objects in memory.");
			clDeduplicateRecursionLevel = config.getInt("deduplicateModelsMaxRecursion", "client", 6, 1, Integer.MAX_VALUE, "The maximum amount of levels of recursion for the deduplication process. Smaller values will deduplicate less data, but make the process run faster.");
			clCleanRedundantModelRegistry = config.getBoolean("clearDuplicateModelRegistry", "client", true, "Clears the baked models generated in the first pass *before* entering the second pass, instead of *after*. While this doesn't reduce memory usage in-game, it does reduce it noticeably during loading.");
			geDynamicRegistrySizeScaling = config.getBoolean("dynamicRegistrySizeScaling", "general", true, "Makes large FML registries scale their availability BitSets dynamically, saving ~48MB of RAM.");

			if (isCoremod) {
				// clTextureDoubleBuffering = config.getBoolean("textureDoubleBuffering", "experimental", true, "Makes texture animations double-buffered, letting the GPU process them independently of scene rendering.");
				geBlockPosPatch = config.getBoolean("optimizedBlockPos", "experimental", true, "Optimizes BlockPos mutable/immutable getters to run on the same variables, letting them be inlined and thus theoretically increasing performance.");
			}

			config.save();
		}
	}
}
