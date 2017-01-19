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
package pl.asie.foamfix.shared;

import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionParser;
import net.minecraftforge.fml.common.versioning.VersionRange;

import java.io.File;

public class FoamFixConfig {
	public boolean delayChunkUpdates = true;
	public boolean lwWeakenResourceCache, lwDummyPackageManifestMap;
	public boolean clDeduplicate, clCleanRedundantModelRegistry;
	public boolean geBlockPosPatch, clBlockInfoPatch, clTextureDoubleBuffering;
	public boolean geDynamicRegistrySizeScaling;
	public boolean geSmallPropertyStorage;
	public int clDeduplicateRecursionLevel;

	private Configuration config;
	public boolean geSmallLightingOptimize = false;

	private boolean getBoolean(String name, String category, boolean defaultValue, String description) {
		return config.getBoolean(name, category, defaultValue, description);
	}

	private boolean getBoolean(String name, String category, boolean defaultValue, String description, String forgeVersionRange) {
		VersionRange range = VersionParser.parseRange(forgeVersionRange);
		DefaultArtifactVersion requiredVersion = new DefaultArtifactVersion("Forge", range);

		if (requiredVersion.containsVersion(new DefaultArtifactVersion("Forge", ForgeVersion.getVersion()))) {
			return getBoolean(name, category, defaultValue, description);
		} else {
			return false;
		}
	}

	public void init(File file, boolean isCoremod) {
		if (config == null) {
			config = new Configuration(file);

			lwDummyPackageManifestMap = getBoolean("dummyPackageManifestMap", "launchwrapper", true, "Dummy out LaunchWrapper's unused package manifest map. This will only break things if some other mod reflects into the LaunchClassLoader to get the private map, which as far as I know is not the case.");
			lwWeakenResourceCache = getBoolean("weakenResourceCache", "launchwrapper", true, "Weaken LaunchWrapper's byte[] resource cache to make it cleanuppable by the GC. Safe.");
			clDeduplicate = getBoolean("deduplicateModels", "client", true, "Enable deduplication of redundant objects in memory.");
			clDeduplicateRecursionLevel = config.getInt("deduplicateModelsMaxRecursion", "client", 6, 1, Integer.MAX_VALUE, "The maximum amount of levels of recursion for the deduplication process. Smaller values will deduplicate less data, but make the process run faster.");
			clCleanRedundantModelRegistry = getBoolean("clearDuplicateModelRegistry", "client", true, "Clears the baked models generated in the first pass *before* entering the second pass, instead of *after*. While this doesn't reduce memory usage in-game, it does reduce it noticeably during loading.");
			geDynamicRegistrySizeScaling = getBoolean("dynamicRegistrySizeScaling", "general", true, "Makes large FML registries scale their availability BitSets dynamically, saving ~48MB of RAM.", "(,13.19.1.2190)");

			if (isCoremod) {
				// clTextureDoubleBuffering = getBoolean("textureDoubleBuffering", "experimental", true, "Makes texture animations double-buffered, letting the GPU process them independently of scene rendering.");
				geSmallPropertyStorage = getBoolean("smallPropertyStorage", "experimental", true, "Replaces the default BlockState/ExtendedBlockState implementations with a far more memory-efficient variant.");
				geBlockPosPatch = getBoolean("optimizedBlockPos", "coremod", true, "Optimizes BlockPos mutable/immutable getters to run on the same variables, letting them be inlined and thus theoretically increasing performance.");
				clBlockInfoPatch = getBoolean("optimizedBlockInfo", "coremod", true, "Prevents BlockInfo from generating as many BlockPos objects; also, fixes a lighting bug.");
				geSmallLightingOptimize = getBoolean("smallLightingOptimize", "experimental", true, "Not fully benchmarked, experimental minor lighting calculation code optimization - according to preliminary tests, it doesn't impact performance while reducing GC churn.");
				delayChunkUpdates = getBoolean("delayChunkRenderUpdates", "experimental", true, "Delays chunk render updates to prevent stutter at the cost of minor chunk render update lag.", "(,13.20.0.2220)");
			}

			config.save();
		}
	}
}
