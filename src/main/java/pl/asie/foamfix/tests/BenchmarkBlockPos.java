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

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
public class BenchmarkBlockPos {
    public static final A1 a = new A1();
    public static final B1 b = new B1();

    private static class A1 {
        int x;

        public int getX() {
            return x;
        }
    }

    private static class A2 extends A1 {
    }

    private static class B1 {
        int x;

        public int getX() {
            return x;
        }
    }

    private static class B2 extends B1 {
        int x;

        @Override
        public int getX() {
            return x;
        }
    }

    @Benchmark
    public void getNonOverridden() {
        int aa = a.getX();
    }

    @Benchmark
    public void getOverridden() {
        int aa = b.getX();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkBlockPos.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
