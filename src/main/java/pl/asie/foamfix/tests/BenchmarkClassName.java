package pl.asie.foamfix.tests;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ClassInheritanceMultiMap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import pl.asie.foamfix.common.FoamyClassInheritanceMultiMap;

@State(Scope.Thread)
public class BenchmarkClassName {
	@Benchmark
	public void getName() {
		Entity.class.getName();
	}

	@Benchmark
	public void getSimpleName() {
		Entity.class.getSimpleName();
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
				.include(BenchmarkClassName.class.getSimpleName())
				.forks(1)
				.build();

		new Runner(opt).run();
	}
}
