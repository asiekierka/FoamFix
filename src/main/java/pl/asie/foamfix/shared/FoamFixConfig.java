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

import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionParser;
import net.minecraftforge.fml.common.versioning.VersionRange;

import java.io.File;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class FoamFixConfig {
	public boolean lwWeakenResourceCache;
	public boolean clDeduplicate, clCleanRedundantModelRegistry, clDynamicItemModels;
	public boolean clFasterVertexLighter, clInitOptions;
	public boolean clParallelModelBaking, clDisableTextureAnimations;
	public boolean geBlacklistLibraryTransformers;
	public boolean geBlockPosPatch, geFasterEntityLookup, geFasterPropertyComparisons, geFasterAirLookup, geFasterEntityDataManager;
	public boolean geDynamicRegistrySizeScaling, twDisableRedstoneLight;
	public boolean geSmallPropertyStorage;
	public boolean twImmediateLightingUpdates;
	public boolean geReplaceSimpleName;

	public int clDeduplicateRecursionLevel;

	private final Set<Property> applicableProperties = Sets.newIdentityHashSet();
	private Configuration config;
	private boolean isCoremod;

	public boolean geSmallLightingOptimize = false;

	private boolean getBoolean(String name, String category, boolean defaultValue, String description, boolean requiresRestart, boolean showInGui) {
		Property prop = config.get(category, name, defaultValue);
		prop.setDefaultValue(defaultValue);
		prop.setComment(description + " [default: " + defaultValue + "]");
		prop.setRequiresMcRestart(requiresRestart);
		prop.setShowInGui(showInGui);
		prop.setLanguageKey("foamfix.config." + name);
		applicableProperties.add(prop);
		return prop.getBoolean(defaultValue);
	}

	private int getInt(String name, String category, int defaultValue, int minValue, int maxValue, String comment, boolean requiresRestart, boolean showInGui) {
		Property prop = config.get(category, name, defaultValue);
		prop.setMinValue(minValue);
		prop.setMaxValue(maxValue);
		prop.setComment(comment + " [default: " + defaultValue + ", range: " + minValue + "-" + maxValue + "]");
		prop.setRequiresMcRestart(requiresRestart);
		prop.setShowInGui(showInGui);
		prop.setLanguageKey("foamfix.config." + name);
		applicableProperties.add(prop);
		return prop.getInt(defaultValue);
	}

	private boolean getBoolean(String name, String category, boolean defaultValue, String description, boolean requiresRestart, boolean showInGui, String forgeVersionRange) {
		VersionRange range = VersionParser.parseRange(forgeVersionRange);
		DefaultArtifactVersion requiredVersion = new DefaultArtifactVersion("Forge", range);

		if (requiredVersion.containsVersion(new DefaultArtifactVersion("Forge", ForgeVersion.getVersion()))) {
			return getBoolean(name, category, defaultValue, description, requiresRestart, showInGui);
		} else {
			return false;
		}
	}

	public void reload() {
		lwWeakenResourceCache = getBoolean("weakenResourceCache", "launchwrapper", true, "Weaken LaunchWrapper's byte[] resource cache to make it cleanuppable by the GC. Safe.", true, true);
		clDeduplicate = getBoolean("deduplicateModels", "client", true, "Enable deduplication of redundant objects in memory.", true, true);
		clDeduplicateRecursionLevel = getInt("deduplicateModelsMaxRecursion", "client", 6, 1, Integer.MAX_VALUE, "The maximum amount of levels of recursion for the deduplication process. Smaller values will deduplicate less data, but make the process run faster.", true, true);
		clCleanRedundantModelRegistry = getBoolean("clearDuplicateModelRegistry", "client", true, "Clears the baked models generated in the first pass *before* entering the second pass, instead of *after*. While this doesn't reduce memory usage in-game, it does reduce it noticeably during loading.", true, true);
		geDynamicRegistrySizeScaling = getBoolean("dynamicRegistrySizeScaling", "general", true, "Makes large FML registries scale their availability BitSets dynamically, saving ~48MB of RAM.", true, true, "(,13.19.1.2190)");

		if (isCoremod) {
			clDisableTextureAnimations = getBoolean("disableTextureAnimations", "client", false, "Disables texture animations.", false, true);
			clInitOptions = getBoolean("initOptions", "client", true, "Initialize the options.txt and forge.cfg files with rendering performance-friendly defaults if not present.", true, false);
			clFasterVertexLighter = getBoolean("fasterVertexLighter", "experimental", true, "Implements optimizations to VertexLighter(Flat) inspired by thecodewarrior and bs2609's work.", true, true);
			// clTextureDoubleBuffering = getBoolean("textureDoubleBuffering", "experimental", true, "Makes texture animations double-buffered, letting the GPU process them independently of scene rendering.");
			twImmediateLightingUpdates = getBoolean("immediateLightingUpdates", "tweaks", false, "Do not delay lighting updates over other types of updates.", true, true);
			geBlacklistLibraryTransformers = getBoolean("blacklistLibraryTransformers", "coremod", true, "Stops certain non-Minecraft-related libraries from being ASM transformed. You shouldn't be transforming those anyway.", true, true);
			geSmallPropertyStorage = getBoolean("smallPropertyStorage", "coremod", true, "Replaces the default BlockState/ExtendedBlockState implementations with a far more memory-efficient variant.", true, true);
			geBlockPosPatch = getBoolean("optimizedBlockPos", "coremod", true, "Optimizes BlockPos mutable/immutable getters to run on the same variables, letting them be inlined and thus theoretically increasing performance.", true, true);
			clDynamicItemModels = getBoolean("dynamicItemModels", "coremod", true, "Make 3D forms of items be rendered dynamically and cached when necessary.", true, true);
			// geSmallLightingOptimize = getBoolean("smallLightingOptimize", "experimental", true, "Not fully benchmarked, experimental minor lighting calculation code optimization - according to preliminary tests, it doesn't impact performance while reducing GC churn.");
			// geFasterSideTransformer = getBoolean("fasterSideTransformer", "coremod", true, "Faster @SideOnly ASM transformer - makes the game load faster");
			clParallelModelBaking = getBoolean("parallelModelBaking", "experimental", true, "Threaded, parallel model baking.", true, true);
			geReplaceSimpleName = getBoolean("replaceWorldSimpleName", "coremod", true, "Replaces Class.getSimpleName in World.updateEntities with getName. As Class.getName's output is cached, unlike getSimpleName, this should provide a small performance boost.", true, true);
			geFasterEntityLookup = getBoolean("fasterEntityLookup", "coremod", true, "Speeds up entity lookup by optimizing ClassInheritanceMultiMap.getByClass.", true, true);
			geFasterAirLookup = getBoolean("fasterAirItemLookup", "coremod", true, "Optimizes ItemStack.isEmpty by removing a map lookup.", true, true);
			geFasterPropertyComparisons = getBoolean("fasterPropertyComparisons", "coremod", true, "Optimizes blockstate property equals and hashCode methods.", true, true);
			geFasterEntityDataManager = getBoolean("fasterEntityDataManager", "coremod", true, "Optimizes the backing map for EntityDataManager, saving memory *and* CPU time!", true, true);
		}

		twDisableRedstoneLight = getBoolean("disableRedstoneLight", "tweaks", false, "Prevent redstone from causing light updates by removing its light level.", true, true);

		config.save();
	}


	public void init(File file, boolean isCoremod) {
		if (config == null) {
			config = new Configuration(file);
			this.isCoremod = isCoremod;
			reload();
		}
	}

	public Configuration getConfig() {
		return config;
	}

	public boolean isApplicable(Property property) {
		return applicableProperties.contains(property);
	}

	public Property getProperty(String name, String category) {
		ConfigCategory cat = config.getCategory(category);
		if (cat != null && cat.containsKey(name)) {
			return cat.get(name);
		} else {
			return null;
		}
	}
}
