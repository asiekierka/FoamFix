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

import com.google.common.collect.Sets;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionParser;
import net.minecraftforge.fml.common.versioning.VersionRange;
import pl.asie.foamfix.FoamFix;

import java.io.File;
import java.util.Set;

public class FoamFixConfig {
	public boolean lwWeakenResourceCache, lwRemovePackageManifestMap;
	public boolean geDeduplicate, clCleanRedundantModelRegistry, clDynamicItemModels;
	public boolean clFasterVertexLighter, clInitOptions, clModelLoaderCleanup;
	public boolean clParallelModelBaking, clDisableTextureAnimations;
	public boolean geBlacklistLibraryTransformers;
	public boolean geBlockPosPatch, geFasterEntityLookup, geFasterPropertyComparisons, geFasterAirLookup, geFasterEntityDataManager;
	public boolean twDisableRedstoneLight;
	public boolean geSmallPropertyStorage;
	public boolean twImmediateLightingUpdates;
	public boolean geFixUnnecessaryGhostload, geFasterHopper, geFixWorldEntityCleanup;
	public boolean expUnpackBakedQuads;
	public boolean txEnable, txFasterAnimation, txRemoveUnnecessaryMiplevels;
	public int txMaxAnimationMipLevel, txCacheAnimationMaxFrames;

	public boolean staging4305;
	public int refreshTimes = 0;

	public int clDeduplicateRecursionLevel;

	private final Set<Property> applicableProperties = Sets.newIdentityHashSet();
	private Configuration config;
	private boolean isCoremod;

	public boolean isDeobfEnvironment;

	public boolean geSmallLightingOptimize = false;

	public boolean resourceDirty;

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

	private double getDouble(String name, String category, double defaultValue, double minValue, double maxValue, String comment, boolean requiresRestart, boolean showInGui) {
		Property prop = config.get(category, name, defaultValue);
		prop.setMinValue(minValue);
		prop.setMaxValue(maxValue);
		prop.setComment(comment + " [default: " + defaultValue + ", range: " + minValue + "-" + maxValue + "]");
		prop.setRequiresMcRestart(requiresRestart);
		prop.setShowInGui(showInGui);
		prop.setLanguageKey("foamfix.config." + name);
		applicableProperties.add(prop);
		return prop.getDouble(defaultValue);
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
		refreshTimes++;

		boolean oldGeDeduplicate = geDeduplicate;
		int oldClDeduplicateRecursionLevel = clDeduplicateRecursionLevel;
		boolean oldExpUnpackBakedQuads = expUnpackBakedQuads;

		lwWeakenResourceCache = getBoolean("weakenResourceCache", "launchwrapper", true, "Weaken LaunchWrapper's byte[] resource cache to make it cleanuppable by the GC. Safe.", true, true);
		lwRemovePackageManifestMap = getBoolean("removePackageManifestMap", "launchwrapper", true, "Remove Launchwrapper package manifest map (which is not used anyway).", true, true);
		geDeduplicate = getBoolean("deduplicate", "general", true, "Enable deduplication of redundant objects in memory.", false, true);
		clDeduplicateRecursionLevel = getInt("deduplicateModelsMaxRecursion", "client", 6, 1, Integer.MAX_VALUE, "The maximum amount of levels of recursion for the deduplication process. Smaller values will deduplicate less data, but make the process run faster.", false, true);
		clCleanRedundantModelRegistry = getBoolean("clearDuplicateModelRegistry", "client", true, "Clears the baked models generated in the first pass *before* entering the second pass, instead of *after*. While this doesn't reduce memory usage in-game, it does reduce it noticeably during loading.", true, true);
		clModelLoaderCleanup = getBoolean("modelLoaderCleanup", "client", true, "Remove unnecessary data from a pointlessly cached ModelLoader instance.", true, true);
		expUnpackBakedQuads = getBoolean("unpackBakedQuads", "experimental", false, "Unpacks all baked quads. Increases RAM usage, but might speed some things up.", false, true);

		if (refreshTimes > 1) {
			if (oldGeDeduplicate != geDeduplicate || oldClDeduplicateRecursionLevel != clDeduplicateRecursionLevel || oldExpUnpackBakedQuads != expUnpackBakedQuads) {
				resourceDirty = true;
			}
		}

		if (isCoremod && getBoolean("forceDisable", "coremod", false, "Disables all coremod functionality.", true, true)) {
			isCoremod = false;
		}

		if (isCoremod) {
			boolean oldTxFasterAnimation = txFasterAnimation;
			boolean oldTxRemoveUnnecessaryMiplevels = txRemoveUnnecessaryMiplevels;
			int oldTxCacheAnimationMaxFrames = txCacheAnimationMaxFrames;
			int oldTxMaxAnimationMipLevel = txMaxAnimationMipLevel;

			staging4305 = getBoolean("pr4305", "staging", true, "Adjust diffuse light calculation to match vanilla facing values", true, true);

			txEnable = getBoolean("enable", "textures", true, "If false, disables any patches from this category.", true, true);
			txFasterAnimation = getBoolean("fasterAnimation", "textures", true, "Enables the faster animation path. Set to false only if true causes issues.", false, true);
			txCacheAnimationMaxFrames = getInt("maxAnimationFramesForCache", "textures", 256, 0, Integer.MAX_VALUE, "The maximum amount of frames an animation can have for it to be cached. If you have a lot of VRAM, set higher.", false, true);
			txMaxAnimationMipLevel = getInt("maxAnimationMipLevel", "textures", -1, -1, 4, "Set to a number to disable animation updates past that mip level. -1 means update all. Higher numbers update more levels.", false, true);
			txRemoveUnnecessaryMiplevels = getBoolean("removeUnnecessaryMipLevels", "textures", true, "Removes unnecessary mip level data from the CPU. Can same some RAM. Inspired by Speiger's TextureFix, although a different (less memory-saving, but more compatible) take on it.", false, true);

			if (refreshTimes > 1) {
				if (oldTxFasterAnimation != txFasterAnimation || oldTxCacheAnimationMaxFrames != txCacheAnimationMaxFrames || oldTxMaxAnimationMipLevel != txMaxAnimationMipLevel || oldTxRemoveUnnecessaryMiplevels != txRemoveUnnecessaryMiplevels) {
					resourceDirty = true;
				}

				if (oldTxFasterAnimation != txFasterAnimation) {
					FoamFix.proxy.updateFasterAnimationFlag();
				}
			}

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
			clParallelModelBaking = getBoolean("parallelModelBaking", "experimental", false, "Threaded, parallel model baking.", true, true);
			if (!isDeobfEnvironment)
				geFasterEntityLookup = getBoolean("fasterEntityLookup", "coremod", true, "Speeds up entity lookup by optimizing ClassInheritanceMultiMap.getByClass.", true, true);
			geFasterAirLookup = getBoolean("fasterAirItemLookup", "coremod", true, "Optimizes ItemStack.isEmpty by removing a map lookup.", true, true);
			geFasterPropertyComparisons = getBoolean("fasterPropertyComparisons", "coremod", true, "Optimizes blockstate property equals and hashCode methods.", true, true);
			geFasterEntityDataManager = getBoolean("fasterEntityDataManager", "experimental", false, "Optimizes the backing map for EntityDataManager, saving memory *and* CPU time! May cause issues, however - please test and report back!", true, true);
			geFixUnnecessaryGhostload = getBoolean("fixUnnecessaryGhostload", "coremod", true, "Fixes unnecessary ghost chunkloading in vanilla.", true, true);
			geFasterHopper = getBoolean("fasterHopper", "coremod", true, "Speeds up the hopper's calculations.", true, true);
			geFixWorldEntityCleanup = getBoolean("fixWorldEntityCleanup", "coremod", true, "Fixes the server not removing unloaded entities/tile entities if no chunkloaders are active. Thanks to CreativeMD for finding this!", true, true);
		}

		twDisableRedstoneLight = getBoolean("disableRedstoneLight", "tweaks", false, "Prevent redstone from causing light updates by removing its light level.", true, true);

		FoamFixShared.isCoremod = isCoremod;

		config.save();
	}


	public void init(File file, boolean isCoremod) {
		if (config == null) {
			config = new Configuration(file);
			this.isCoremod = isCoremod;
			this.isDeobfEnvironment = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
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
