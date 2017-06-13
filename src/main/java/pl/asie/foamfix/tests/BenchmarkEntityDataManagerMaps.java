package pl.asie.foamfix.tests;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import pl.asie.foamfix.common.FoamyArrayBackedDataManagerMap;

import java.util.*;

@State(Scope.Thread)
public class BenchmarkEntityDataManagerMaps {

	private static final Object[] o = new Object[256];
	private static final Map<Integer, Object> mapJDK = new HashMap<>();
	private static final Map<Integer, Object> mapArray = new FoamyArrayBackedDataManagerMap<>();
	private static final Map<Integer, Object> mapFastutil = new Int2ObjectOpenHashMap<>();
	private static final FoamyArrayBackedDataManagerMap<Object> mapArrayUnboxed =(FoamyArrayBackedDataManagerMap<Object>) mapArray;
	private static final Int2ObjectOpenHashMap<Object> mapFastutilUnboxed =(Int2ObjectOpenHashMap<Object>) mapFastutil;

	private static final Map<Class<? extends Entity>, Integer> mapReverseJDK = new HashMap<>();
	private static final Map<Class<? extends Entity>, Integer> mapReverseFastutil = new Object2IntOpenHashMap<>();

	static {
		for (int i = 192; i < 254; i++) {
			o[i] = new Object();
			mapJDK.put(i, o[i]);
			mapArray.put(i, o[i]);
			mapFastutil.put(i, o[i]);
		}

		Bootstrap.register();
		for (ResourceLocation r : EntityList.getEntityNameList()) {
			mapReverseFastutil.put(EntityList.getClass(r), (int) (Math.random() * 256));
			mapReverseJDK.put(EntityList.getClass(r), (int) (Math.random() * 256));
		}
	}

	@Benchmark
	public void containsKeyReverseJDK() {
		mapReverseJDK.containsKey(EntityDonkey.class);
	}

	@Benchmark
	public void containsKeyReverseFastutil() {
		mapReverseFastutil.containsKey(EntityDonkey.class);
	}

	@Benchmark
	public void getReverseJDK() {
		int i = mapReverseJDK.get(EntityDonkey.class) + 1;
	}

	@Benchmark
	public void getReverseFastutil() {
	    int i = mapReverseFastutil.get(EntityDonkey.class) + 1;
	}

	@Benchmark
	public void containsKeyJDK() {
		Object z;
		for (int j = 128; j < 256; j++)
			z = mapJDK.containsKey(j);
	}

	@Benchmark
	public void containsKeyArray() {
		Object z;
		for (int j = 128; j < 256; j++)
			z = mapArray.containsKey(j);
	}

	@Benchmark
	public void containsKeyFastutil() {
		Object z;
		for (int j = 128; j < 256; j++)
			z = mapFastutil.containsKey(j);
	}

	@Benchmark
	public void containsKeyArrayUnboxed() {
		Object z;
		for (int j = 128; j < 256; j++)
			z = mapArrayUnboxed.containsKey(j);
	}

	@Benchmark
	public void containsKeyFastutilUnboxed() {
		Object z;
		for (int j = 128; j < 256; j++)
			z = mapFastutilUnboxed.containsKey(j);
	}

	@Benchmark
	public void getJDK() {
		Object z;
		for (int j = 128; j < 256; j++)
			z = mapJDK.get(j);
	}

	@Benchmark
	public void getArray() {
		Object z;
		for (int j = 128; j < 256; j++)
			z = mapArray.get(j);
	}

	@Benchmark
	public void getFastutil() {
		Object z;
		for (int j = 128; j < 256; j++)
			z = mapFastutil.get(j);
	}

	@Benchmark
	public void getArrayUnboxed() {
		Object z;
		for (int j = 128; j < 256; j++)
			z = mapArrayUnboxed.get(j);
	}

	@Benchmark
	public void getFastutilUnboxed() {
		Object z;
		for (int j = 128; j < 256; j++)
			z = mapFastutilUnboxed.get(j);
	}

	@Benchmark
	public void valueIterateJDK() {
		for (Object j : mapJDK.values()) {
			Object o = j;
		}
	}

	@Benchmark
	public void valueIterateArray() {
		for (Object j : mapArray.values()) {
			Object o = j;
		}
	}

	@Benchmark
	public void valueIterateFastutil() {
		for (Object j : mapFastutil.values()) {
			Object o = j;
		}
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
				.include(BenchmarkEntityDataManagerMaps.class.getSimpleName())
				.forks(2)
				.build();

		new Runner(opt).run();
	}
}
