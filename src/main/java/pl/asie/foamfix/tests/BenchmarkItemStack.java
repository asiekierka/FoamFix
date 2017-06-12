package pl.asie.foamfix.tests;

import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
public class BenchmarkItemStack {
    private static final Item item, itemair;

    static {
        Bootstrap.register();
        item = Items.BIRCH_DOOR;
        itemair = Items.AIR;
    }

    @Benchmark
    public void airLookupMap() {
        boolean b = item == Item.getItemFromBlock(Blocks.AIR);
    }

    @Benchmark
    public void airLookupCached() {
        Item i = itemair == null ? Item.getItemFromBlock(Blocks.AIR) : itemair;
        boolean b = item == i;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkItemStack.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
