package pl.asie.foamfix;

import com.google.common.cache.CacheBuilder;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import pl.asie.foamfix.common.FoamFixChunkSaveHandler;
import pl.asie.foamfix.shared.FoamFixShared;
import pl.asie.foamfix.util.PretendPackageMap;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by asie on 12/7/16.
 */
public class ProxyCommon {
	public static FoamFixChunkSaveHandler chunkSaveHandler;

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
		if (FoamFixShared.config.geImprovedChunkProvider) {
			chunkSaveHandler = new FoamFixChunkSaveHandler();
			MinecraftForge.EVENT_BUS.register(chunkSaveHandler);
		}
	}

	public void postInit() {
		optimizeLaunchWrapper();
	}
}
