package pl.asie.foamfix.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.ResourceIndex;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class FoamyDefaultResourcePack extends DefaultResourcePack {
//	private final Cache<ResourceLocation, Boolean> cache =
//			CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();
	private final Set<ResourceLocation> foundResources = new HashSet<>();
	private final ResourceIndex resourceIndex;

	public FoamyDefaultResourcePack(ResourceIndex resourceIndexIn) {
		super(resourceIndexIn);
		this.resourceIndex = resourceIndexIn;
	}

	// TODO: Verify what happens when non-mod files get added in this manner
	/* @Override
	public InputStream getInputStream(ResourceLocation location) throws IOException {
		if (!resourceExists(location)) {
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
	} */

	// I can still cache resources, however, as it's unlikely for something to get *removed* from the ClassLoader
	@Override
	public boolean resourceExists(ResourceLocation loc) {
		if (foundResources.contains(loc)) return true;

		if (DefaultResourcePack.class.getResource("/assets/" + loc.getResourceDomain() + "/" + loc.getResourcePath()) != null || FoamyDefaultResourcePack.this.resourceIndex.isFileExisting(loc)) {
			foundResources.add(loc);
			return true;
		}

		return false;
	}
}
