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
