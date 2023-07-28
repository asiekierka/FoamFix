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

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.properties.IProperty;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
public class BenchmarkPropertyComparisons {
    private final TObjectIntMap<String> entryPositionMapOne = new TObjectIntHashMap<>();
    private final TObjectIntMap<IProperty> entryPositionMapTwo = new TObjectIntHashMap<>();

    public BenchmarkPropertyComparisons() {
        entryPositionMapOne.put(BlockLog.AXIS.getName(), 22);
        entryPositionMapOne.put(BlockPlanks.VARIANT.getName(), 26);

        entryPositionMapTwo.put(BlockLog.AXIS, 22);
        entryPositionMapTwo.put(BlockPlanks.VARIANT, 26);
    }

    @Benchmark
    public void getString() {
        entryPositionMapOne.get(BlockLog.AXIS.getName());
    }

    @Benchmark
    public void getProperty() {
        entryPositionMapTwo.get(BlockLog.AXIS);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkPropertyComparisons.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
