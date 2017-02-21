package pl.asie.foamfix.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.ResourceIndex;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import pl.asie.foamfix.shared.FoamFixShared;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class FoamyDefaultResourcePack extends DefaultResourcePack {
	private final Cache<ResourceLocation, Boolean> cache =
			CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();
	private final ResourceIndex resourceIndex;

	public FoamyDefaultResourcePack(ResourceIndex resourceIndexIn) {
		super(resourceIndexIn);
		this.resourceIndex = resourceIndexIn;
	}

	@Override
	public InputStream getInputStream(ResourceLocation location) throws IOException {
		Boolean b = cache.getIfPresent(location);
		if (b != null && !b) {
			return null;
		}

		return super.getInputStream(location);
	}

	@Override
	public boolean resourceExists(ResourceLocation location) {
		final ResourceLocation loc = location;
		try {
			return cache.get(location, new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					return DefaultResourcePack.class.getResource("/assets/" + loc.getResourceDomain() + "/" + loc.getResourcePath()) != null || FoamyDefaultResourcePack.this.resourceIndex.isFileExisting(loc);
				}
			});
		} catch (Exception e) {
			return DefaultResourcePack.class.getResource("/assets/" + loc.getResourceDomain() + "/" + loc.getResourcePath()) != null || FoamyDefaultResourcePack.this.resourceIndex.isFileExisting(loc);
		}
	}

	public static String getClassName() {
		if (FoamFixShared.hasOptifine() || FoamFixShared.config.clFasterResourceLoading == 1) {
			return "pl.asie.foamfix.client.FoamyDefaultResourcePackLight";
		} else {
			return "pl.asie.foamfix.client.FoamyDefaultResourcePack";
		}
	}

	public static DefaultResourcePack create(ResourceIndex resourceIndex) {
		if (FoamFixShared.hasOptifine() || FoamFixShared.config.clFasterResourceLoading == 1) {
			return new FoamyDefaultResourcePackLight(resourceIndex);
		} else {
			return new FoamyDefaultResourcePack(resourceIndex);
		}
	}
}
