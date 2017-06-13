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

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionParser;
import net.minecraftforge.fml.common.versioning.VersionRange;

import java.io.File;

public class FoamFixConfig {
	public boolean lwWeakenResourceCache;
	public boolean clDeduplicate, clCleanRedundantModelRegistry, clDynamicItemModels;
	public boolean clFasterVertexLighter, clInitOptions;
	public boolean clParallelModelBaking;
	public boolean geBlacklistLibraryTransformers;
	public boolean geBlockPosPatch, geFasterEntityLookup, geFasterPropertyComparisons, geFasterAirLookup, geFasterEntityDataManager;
	public boolean geDynamicRegistrySizeScaling, geDisableRedstoneLight;
	public boolean geSmallPropertyStorage;
	public boolean geImmediateLightingUpdates;
	public boolean geReplaceSimpleName;

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

			lwWeakenResourceCache = getBoolean("weakenResourceCache", "launchwrapper", true, "Weaken LaunchWrapper's byte[] resource cache to make it cleanuppable by the GC. Safe.");
			clDeduplicate = getBoolean("deduplicateModels", "client", true, "Enable deduplication of redundant objects in memory.");
			clDeduplicateRecursionLevel = config.getInt("deduplicateModelsMaxRecursion", "client", 6, 1, Integer.MAX_VALUE, "The maximum amount of levels of recursion for the deduplication process. Smaller values will deduplicate less data, but make the process run faster.");
			clCleanRedundantModelRegistry = getBoolean("clearDuplicateModelRegistry", "client", true, "Clears the baked models generated in the first pass *before* entering the second pass, instead of *after*. While this doesn't reduce memory usage in-game, it does reduce it noticeably during loading.");
			geDynamicRegistrySizeScaling = getBoolean("dynamicRegistrySizeScaling", "general", true, "Makes large FML registries scale their availability BitSets dynamically, saving ~48MB of RAM.", "(,13.19.1.2190)");

			if (isCoremod) {
				clInitOptions = getBoolean("initOptions", "client", true, "Initialize the options.txt and forge.cfg files with rendering performance-friendly defaults if not present.");
				clFasterVertexLighter = getBoolean("fasterVertexLighter", "experimental", true, "Implements optimizations to VertexLighter(Flat) inspired by thecodewarrior and bs2609's work.");
				// clTextureDoubleBuffering = getBoolean("textureDoubleBuffering", "experimental", true, "Makes texture animations double-buffered, letting the GPU process them independently of scene rendering.");
				geImmediateLightingUpdates = getBoolean("immediateLightingUpdates", "tweaks", false, "Do not delay lighting updates over other types of updates.");
				geBlacklistLibraryTransformers = getBoolean("blacklistLibraryTransformers", "coremod", true, "Stops certain non-Minecraft-related libraries from being ASM transformed. You shouldn't be transforming those anyway.");
				geSmallPropertyStorage = getBoolean("smallPropertyStorage", "coremod", true, "Replaces the default BlockState/ExtendedBlockState implementations with a far more memory-efficient variant.");
				geBlockPosPatch = getBoolean("optimizedBlockPos", "coremod", true, "Optimizes BlockPos mutable/immutable getters to run on the same variables, letting them be inlined and thus theoretically increasing performance.");
				clDynamicItemModels = getBoolean("dynamicItemModels", "coremod", true, "Make 3D forms of items be rendered dynamically and cached when necessary.");
				// geSmallLightingOptimize = getBoolean("smallLightingOptimize", "experimental", true, "Not fully benchmarked, experimental minor lighting calculation code optimization - according to preliminary tests, it doesn't impact performance while reducing GC churn.");
				// geFasterSideTransformer = getBoolean("fasterSideTransformer", "coremod", true, "Faster @SideOnly ASM transformer - makes the game load faster");
				clParallelModelBaking = getBoolean("parallelModelBaking", "experimental", true, "Threaded, parallel model baking.");
				geReplaceSimpleName = getBoolean("replaceWorldSimpleName", "coremod", true, "Replaces Class.getSimpleName in World.updateEntities with getName. As Class.getName's output is cached, unlike getSimpleName, this should provide a small performance boost.");
				geFasterEntityLookup = getBoolean("fasterEntityLookup", "coremod", true, "Speeds up entity lookup by optimizing ClassInheritanceMultiMap.getByClass.");
				geFasterAirLookup = getBoolean("fasterAirItemLookup", "coremod", true, "Optimizes ItemStack.isEmpty by removing a map lookup.");
				geFasterPropertyComparisons = getBoolean("fasterPropertyComparisons", "coremod", true, "Optimizes blockstate property equals and hashCode methods.");
				geFasterEntityDataManager = getBoolean("fasterEntityDataManager", "coremod", true, "Optimizes the backing map for EntityDataManager, saving memory *and* CPU time!");
			}

			geDisableRedstoneLight = getBoolean("disableRedstoneLight", "tweaks", false, "Prevent redstone from causing light updates by removing its light level.");

			config.save();
		}
	}
}
