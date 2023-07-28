/**
 * Copyright (C) 2016, 2017, 2018, 2019, 2020, 2021 Adrian Siekierka
 *
 * This file is part of FoamFix.
 *
 * FoamFix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoamFix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FoamFix.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.asie.foamfix.tests;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.item.EnumDyeColor;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
public class BenchmarkFastutil {
	private final TObjectIntMap<String> stringMap1 = new TObjectIntHashMap<>();
	private final Object2IntMap<String> stringMap2 = new Object2IntOpenHashMap<>();
	private final TObjectIntMap<EnumDyeColor> enumMap1 = new TObjectIntHashMap<>();
	private final Object2IntMap<EnumDyeColor> enumMap2 = new Object2IntOpenHashMap<>();
	private final TIntIntMap intMap1 = new TIntIntHashMap();
	private final Int2IntMap intMap2 = new Int2IntOpenHashMap();

	public BenchmarkFastutil() {
		for (int i = 0; i < 16; i++) {
			stringMap1.put("test" + i, i*3-1);
			stringMap2.put("test" + i, i*3-1);
			intMap1.put(i, i*3-1);
			intMap2.put(i, i*3-1);
		}
		for (EnumDyeColor color : EnumDyeColor.values()) {
			enumMap1.put(color, color.getColorValue());
			enumMap2.put(color, color.getColorValue());
		}
	}


	@Benchmark
	public void get1() {
		stringMap1.get("test11");
		stringMap1.get("test5");
//		intMap1.get(11);
//		intMap1.get(5);
//		enumMap1.get(EnumDyeColor.RED);
//		enumMap1.get(EnumDyeColor.ORANGE);
	}

	@Benchmark
	public void get2() {
		stringMap2.get("test11");
		stringMap2.get("test5");
//		intMap2.get(11);
//		intMap2.get(5);
//		enumMap2.get(EnumDyeColor.RED);
//		enumMap2.get(EnumDyeColor.ORANGE);
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
				.include(BenchmarkFastutil.class.getSimpleName())
				.forks(1)
				.build();

		new Runner(opt).run();
	}
}
