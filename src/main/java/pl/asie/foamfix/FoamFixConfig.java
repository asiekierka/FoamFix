package pl.asie.foamfix;

import gnu.trove.set.hash.TIntHashSet;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class FoamFixConfig {
	public boolean lwWeakenResourceCache, lwDummyPackageManifestMap;
	public boolean clDeduplicate, clCleanRedundantModelRegistry;
	public boolean geImprovedChunkProvider;
	public boolean geBlockPosPatch;
	public TIntHashSet gePermanentWorldsSet = new TIntHashSet();

	private Configuration config;
	private int[] gePermanentWorlds;

	public void init(File file, boolean isCoremod) {
		if (config == null) {
			config = new Configuration(file);

			lwDummyPackageManifestMap = config.getBoolean("dummyPackageManifestMap", "launchwrapper", true, "Dummy out LaunchWrapper's unused package manifest map. This will only break things if some other mod reflects into the LaunchClassLoader to get the private map, which as far as I know is not the case.");
			lwWeakenResourceCache = config.getBoolean("weakenResourceCache", "launchwrapper", true, "Weaken LaunchWrapper's byte[] resource cache to make it cleanuppable by the GC. Safe.");
			clDeduplicate = config.getBoolean("deduplicateModels", "client", true, "Enable deduplication of redundant objects in memory.");
			clCleanRedundantModelRegistry = config.getBoolean("clearDuplicateModelRegistry", "client", true, "Clears the baked models generated in the first pass *before* entering the second pass, instead of *after*. While this doesn't reduce memory usage in-game, it does reduce it noticeably during loading.");
			geImprovedChunkProvider = config.getBoolean("improvedChunkProvider", "experimental", true, "Replaces the server chunk provider with an optimized variant.");
			gePermanentWorlds = config.get("experimental", "improvedChunkProviderPermanentWorlds", new int[]{0, -1, 1}, "The list of permanent worlds which will never be fully unloaded with improvedChunkProvider.").getIntList();
			gePermanentWorldsSet.addAll(gePermanentWorlds);
			if (isCoremod) {
				geBlockPosPatch = config.getBoolean("optimizedBlockPos", "experimental", true, "Optimizes BlockPos mutable/immutable getters to run on the same variables, letting them be inlined and thus theoretically increasing performance.");
			}

			config.save();
		}
	}
}
