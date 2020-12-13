/*
 * Copyright (C) 2016, 2017, 2018, 2019 Adrian Siekierka
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
	public boolean geDeduplicate, clWipeModelCache, clCleanRedundantModelRegistry, clDynamicItemModels;
	public boolean clCheapMinimumLighter, clInitOptions, clModelLoaderCleanup;
	public boolean clDisableTextureAnimations;
	public boolean geBlacklistLibraryTransformers;
	public boolean geBlockPosPatch, geFasterEntityLookup, geFasterPropertyComparisons, geFasterAirLookup, geFasterEntityDataManager;
	public boolean twDisableRedstoneLight;
	public boolean geSmallPropertyStorage, gePatchChunkSerialization;
	public boolean twImmediateLightingUpdates;
	public boolean gbPatchBeds, geFasterHopper, geFixWorldEntityCleanup, clDeduplicateModels, clDeduplicateIModels;
	public boolean gbNotifyNonUnloadedWorlds, gbForgeGCNonUnloaded;
	public int gbWorldUnloadTime;
	public boolean dbgCountModels;
	public boolean expUnpackBakedQuads;
	public boolean txEnable, gbEnableWrapper, gbWrapperCountNotifyBlock;
	public boolean clClearCachesOnUnload;
	public int txFasterAnimation;
	public int txMaxAnimationMipLevel, txCacheAnimationMaxFrames;
	public int geMobSpawnerCheckSpeed;

	public boolean gbPatchFluids;
	public boolean gbPatchFarmland;
	public boolean gbPatchBopGrass;
	public String[] gbCustomRules;

	public boolean clJeiCreativeSearch;

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

	private String[] getStringList(String name, String category, String description, boolean requiresRestart, boolean showInGui) {
		Property prop = config.get(category, name, new String[0]);
		prop.setDefaultValues(new String[0]);
		prop.setComment(description);
		prop.setRequiresMcRestart(requiresRestart);
		prop.setShowInGui(showInGui);
		prop.setLanguageKey("foamfix.config." + name);
		applicableProperties.add(prop);
		return prop.getStringList();
	}

	public void reload() {
		refreshTimes++;

		boolean oldGeDeduplicate = geDeduplicate;
		int oldClDeduplicateRecursionLevel = clDeduplicateRecursionLevel;
		boolean oldExpUnpackBakedQuads = expUnpackBakedQuads;
		boolean oldClDeduplicateIModels = clDeduplicateIModels;
		boolean oldClJeiCreativeSearch = clJeiCreativeSearch;

		dbgCountModels = getBoolean("countListBakedModels", "debug", false, "Should FoamFix count and list baked models during deduplication, per mod?", false, false);

		lwWeakenResourceCache = getBoolean("weakenResourceCache", "launchwrapper", true, "Weaken LaunchWrapper's byte[] resource cache to make it cleanuppable by the GC. Safe.", true, true);
		lwRemovePackageManifestMap = getBoolean("removePackageManifestMap", "launchwrapper", true, "Remove Launchwrapper package manifest map (which is not used anyway).", true, true);
		geDeduplicate = getBoolean("deduplicate", "general", true, "Enable deduplication of redundant objects in memory.", false, true);
		clDeduplicateRecursionLevel = getInt("deduplicateModelsMaxRecursion", "client", 9, 1, Integer.MAX_VALUE, "The maximum amount of levels of recursion for the deduplication process. Smaller values will deduplicate less data, but make the process run faster.", false, true);
		clDeduplicateModels = getBoolean("deduplicateModels", "client", true, "Deduplicates baked models. The original feature.", false, true);
		clDeduplicateIModels = getBoolean("deduplicateModelBakers", "client", true, "Deduplicates IModels too. Takes a few seconds more, but shaves off another bit of RAM.", false, true);
		clCleanRedundantModelRegistry = getBoolean("clearDuplicateModelRegistry", "client", true, "Clears the baked models generated in the first pass *before* entering the second pass, instead of *after*. While this doesn't reduce memory usage in-game, it does reduce it noticeably during loading.", true, true);
		clModelLoaderCleanup = getBoolean("modelLoaderCleanup", "client", true, "Remove unnecessary data from a pointlessly cached ModelLoader instance.", true, true);
		expUnpackBakedQuads = getBoolean("unpackBakedQuads", "experimental", false, "Unpacks all baked quads. Increases RAM usage, but might speed some things up.", false, true);
		gbEnableWrapper = getBoolean("enableDebuggingWrapper", "ghostbuster", false, "Wrap ChunkProviderServers to be able to provide the /ghostbuster command for debugging ghost chunkloads.", true, true);
		gbWrapperCountNotifyBlock = getBoolean("wrapperShowsNeighborUpdates", "ghostbuster", false, "Should the /ghostbuster debugger show neighbor updates?", false, true);
		clClearCachesOnUnload = getBoolean("clearCachesOnWorldUnload", "client", true, "Clears caches on world unload a bit faster than usual. Prevents temporary memory leaks. More effective in Anarchy.", true, true);

		gbForgeGCNonUnloaded = getBoolean("nonUnloadedWorldsForceGCOnCheck", "ghostbuster", false, "For FoamFix debugging/development purposes only.", false, false);
		gbNotifyNonUnloadedWorlds = getBoolean("checkNonUnloadedWorldClients", "ghostbuster", true, "Checks if worlds do not unload after a specified amount of time, and notifies the user if that is the case.", true, true);
		gbWorldUnloadTime = getInt("checkNonUnloadedWorldTimeout", "ghostbuster", 60, 10, 3600, "The amount of time FoamFix should wait for a world to be deemed non-unloaded.", true, true);

		if (isCoremod && getBoolean("forceDisable", "coremod", false, "Disables all coremod functionality.", true, true)) {
			isCoremod = false;
		}

		if (isCoremod) {
			int oldTxFasterAnimation = txFasterAnimation;
			int oldTxCacheAnimationMaxFrames = txCacheAnimationMaxFrames;
			int oldTxMaxAnimationMipLevel = txMaxAnimationMipLevel;
			boolean oldClWipeModelCache = clWipeModelCache;

			geMobSpawnerCheckSpeed = getInt("mobSpawnerCheckSpeed", "coremod", 10, 1, 200, "The amount of ticks per player presence check for mob spawners. Set to 1 to disable the patch and match vanilla behaviour.", true, true);

			gbPatchFluids = getBoolean("patchFluids", "ghostbuster", true, "Should fluids be prevented from ghost chunkloading?", true, true);
			gbPatchFarmland = getBoolean("patchFarmland", "ghostbuster", true, "Should farmland be prevented from ghost chunkloading?", true, true);
			gbPatchBopGrass = getBoolean("patchBopGrass", "ghostbuster", true, "Should BoP grass be prevented from ghost chunkloading?", true, true);
			gbPatchBeds = getBoolean("patchBeds", "ghostbuster", true, "Should beds be prevented from ghost chunkloading?", true, true);
			gbCustomRules = getStringList("customPatchRulesRadius", "ghostbuster", "Custom patch rules. Format: 'className;methodName;accessAloadPos;posAloadPos;radius'. An AloadPos is the position of the argument in the method - 1 for the first one, 2 for the second one, ...; the radius determines how many blocks have to be around the method for no early return. Untested - please use with care.", true, false);

			gePatchChunkSerialization = getBoolean("patchChunkSerialization", "coremod", true, "Fix a bug in chunk serialization leading to crashes. Originally found and fixed by Aaron1011 of Sponge.", true, true);

			clJeiCreativeSearch = getBoolean("jeiCreativeSearch", "client", true, "Makes vanilla creative tab search use JEI's lookups - saves a lot of RAM *and* gives you fancy JEI features!", true, true);
			clWipeModelCache = getBoolean("wipeModelCache", "client", true, "Wipes the IModel cache after baking is finished. Saves a lot of RAM, as most IModels will not be reloaded.", false, true);

			txEnable = getBoolean("enable", "textures", true, "If false, disables any patches from this category.", true, true);
			txFasterAnimation = getInt("fasterAnimation", "textures", 1, 0, 2,"Controls the faster animation path. 0 - disable, 2 - force enable, 1 (default) - enable on devices which have been shown to benefit from it.", false, true);
			txCacheAnimationMaxFrames = getInt("maxAnimationFramesForCache", "textures", 256, 0, Integer.MAX_VALUE, "The maximum amount of frames an animation can have for it to be cached. If you have a lot of VRAM, set higher.", false, true);
			txMaxAnimationMipLevel = getInt("maxAnimationMipLevel", "textures", -1, -1, 4, "Set to a number to disable animation updates past that mip level. -1 means update all. Higher numbers update more levels. To disable animation updates altogether, use the option for it.", false, true);

			if (refreshTimes > 1) {
				if (oldClWipeModelCache != clWipeModelCache || oldTxFasterAnimation != txFasterAnimation || oldTxCacheAnimationMaxFrames != txCacheAnimationMaxFrames || oldTxMaxAnimationMipLevel != txMaxAnimationMipLevel) {
					resourceDirty = true;
				}

				if (oldTxFasterAnimation != txFasterAnimation) {
					FoamFix.proxy.updateFasterAnimationFlag();
				}
			}

			clDisableTextureAnimations = getBoolean("disableTextureAnimations", "client", false, "Disables texture animations.", false, true);
			clInitOptions = getBoolean("initOptions", "client", true, "Initialize the options.txt and forge.cfg files with rendering performance-friendly defaults if not present.", true, false);
//			clCheapMinimumLighter = getBoolean("cheapMinimumLight", "experimental", true, "Replaces the Minimum Smooth Lighting option with a lighter which only provides ambient occlusion, but not smooth light per se.", true, true);
			clCheapMinimumLighter = false;
			// clTextureDoubleBuffering = getBoolean("textureDoubleBuffering", "experimental", true, "Makes texture animations double-buffered, letting the GPU process them independently of scene rendering.");
			twImmediateLightingUpdates = getBoolean("immediateLightingUpdates", "tweaks", false, "Do not delay lighting updates over other types of updates.", true, true);
			geBlacklistLibraryTransformers = getBoolean("blacklistLibraryTransformers", "coremod", true, "Stops certain non-Minecraft-related libraries from being ASM transformed. You shouldn't be transforming those anyway.", true, true);
			geSmallPropertyStorage = getBoolean("smallPropertyStorage", "coremod", true, "Replaces the default BlockState/ExtendedBlockState implementations with a far more memory-efficient variant.", true, true);
			geBlockPosPatch = getBoolean("optimizedBlockPos", "coremod", true, "Optimizes BlockPos mutable/immutable getters to run on the same variables, letting them be inlined and thus theoretically increasing performance.", true, true);
			clDynamicItemModels = getBoolean("dynamicItemModels", "coremod", true, "Make 3D forms of items be rendered dynamically and cached when necessary.", true, true);
			// geSmallLightingOptimize = getBoolean("smallLightingOptimize", "experimental", true, "Not fully benchmarked, experimental minor lighting calculation code optimization - according to preliminary tests, it doesn't impact performance while reducing GC churn.");
			// geFasterSideTransformer = getBoolean("fasterSideTransformer", "coremod", true, "Faster @SideOnly ASM transformer - makes the game load faster");
			if (!isDeobfEnvironment)
				geFasterEntityLookup = getBoolean("fasterEntityLookup", "coremod", true, "Speeds up entity lookup by optimizing ClassInheritanceMultiMap.getByClass.", true, true);
			geFasterAirLookup = getBoolean("fasterAirItemLookup", "coremod", true, "Optimizes ItemStack.isEmpty by removing a map lookup. Initially found by Aikar (Paper/0181).", true, true);
			geFasterPropertyComparisons = getBoolean("fasterPropertyComparisons", "coremod", true, "Optimizes blockstate property equals and hashCode methods.", true, true);
			geFasterEntityDataManager = getBoolean("fasterEntityDataManager", "experimental", false, "Optimizes the backing map for EntityDataManager, saving memory *and* CPU time! May cause issues, however - please test and report back!", true, true);
			geFasterHopper = getBoolean("fasterHopper", "coremod", true, "Speeds up the hopper's calculations.", true, true);
			geFixWorldEntityCleanup = getBoolean("fixWorldEntityCleanup", "coremod", true, "Fixes the server not removing unloaded entities/tile entities if no chunkloaders are active. Thanks to CreativeMD for finding this!", true, true);
		}

		if (refreshTimes > 1) {
			if (oldClJeiCreativeSearch != clJeiCreativeSearch && oldClDeduplicateIModels != clDeduplicateIModels || oldGeDeduplicate != geDeduplicate || oldClDeduplicateRecursionLevel != clDeduplicateRecursionLevel || oldExpUnpackBakedQuads != expUnpackBakedQuads) {
				resourceDirty = true;
			}
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
