/*
 * Copyright (C) 2016, 2017, 2018 Adrian Siekierka
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

package pl.asie.foamfix.tests;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.block.*;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.EnumFacing;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import pl.asie.foamfix.coremod.injections.PropertyFasterComparisonsInject;

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
