package pl.asie.foamfix.tests;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.ResourceIndex;
import net.minecraft.util.ResourceLocation;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by asie on 2/20/17.
 */
@State(Scope.Thread)
public class BenchmarkResourceLookup {
	private static final ResourceLocation LOC1 = new ResourceLocation("minecraft:models/block/stone.json");
	private static final ResourceLocation LOC2 = new ResourceLocation("minecraft:models/block/stone_fake.json");

	private final Cache<ResourceLocation, Boolean> cache =
			CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();
	private final ResourceIndex resourceIndex = new ResourceIndex(
			new File("/home/asie/MultiMC5-0.4.11/assets"),
			"1.10"
	);

	private InputStream getResourceStream(ResourceLocation location) {
		return DefaultResourcePack.class.getResourceAsStream("/assets/" + location.getResourceDomain() + "/" + location.getResourcePath());
	}

	private InputStream getResourceStreamFastCached(ResourceLocation location) {
		return resourceExistsFastCached(location) ? DefaultResourcePack.class.getResourceAsStream("/assets/" + location.getResourceDomain() + "/" + location.getResourcePath()) : null;
	}

	public boolean resourceExists(ResourceLocation location) {
        return this.getResourceStream(location) != null || this.resourceIndex.isFileExisting(location);
    }

	public boolean resourceExistsFast(ResourceLocation location) {
		return DefaultResourcePack.class.getResource("/assets/" + location.getResourceDomain() + "/" + location.getResourcePath()) != null || this.resourceIndex.isFileExisting(location);
	}

	public boolean resourceExistsFastCached(ResourceLocation location) {
		final ResourceLocation loc = location;
		try {
			return cache.get(location, new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					return DefaultResourcePack.class.getResource("/assets/" + loc.getResourceDomain() + "/" + loc.getResourcePath()) != null || BenchmarkResourceLookup.this.resourceIndex.isFileExisting(loc);
				}
			});
		} catch (Exception e) {
			return DefaultResourcePack.class.getResource("/assets/" + loc.getResourceDomain() + "/" + loc.getResourcePath()) != null || BenchmarkResourceLookup.this.resourceIndex.isFileExisting(loc);
		}
	}

	@Benchmark
	public void resourceExistsAndGetStreamSlow() {
		if (resourceExists(LOC1)) {
			getResourceStream(LOC1);
		}
		if (resourceExists(LOC2)) {
			getResourceStream(LOC2);
		}
	}

	@Benchmark
	public void resourceExistsAndGetStreamFast() {
		if (resourceExistsFast(LOC1)) {
			getResourceStream(LOC1);
		}
		if (resourceExistsFast(LOC2)) {
			getResourceStream(LOC2);
		}
	}

	@Benchmark
	public void resourceExistsAndGetStreamFastCached() {
		if (resourceExistsFastCached(LOC1)) {
			getResourceStreamFastCached(LOC1);
		}
		if (resourceExistsFastCached(LOC2)) {
			getResourceStreamFastCached(LOC2);
		}
	}


	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
				.include(BenchmarkResourceLookup.class.getSimpleName())
				.forks(1)
				.build();

		new Runner(opt).run();
	}
}
