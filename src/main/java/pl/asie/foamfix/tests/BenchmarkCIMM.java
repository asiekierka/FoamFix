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
import pl.asie.foamfix.coremod.common.FoamyClassInheritanceMultiMap;

@State(Scope.Thread)
public class BenchmarkCIMM {
	private static final EntityZombie[] zombies;
	private static final EntityItem[] items;

	static {
		zombies = new EntityZombie[100];
		items = new EntityItem[100];
		for (int i = 0; i < 100; i++) {
			zombies[i] = new EntityZombie(null);
			items[i] = new EntityItem(null);
		}
	}

	@Benchmark
	public void addEntities() {
		ClassInheritanceMultiMap<Entity> cimm1 = new ClassInheritanceMultiMap<Entity>(Entity.class);
		for (int i = 0; i < 100; i++) {
			cimm1.add(zombies[i]);
			cimm1.add(items[i]);
		}
	}

	@Benchmark
	public void addEntitiesThenLookupOriginal() {
		ClassInheritanceMultiMap<Entity> cimm1 = new ClassInheritanceMultiMap<Entity>(Entity.class);
		for (int i = 0; i < 100; i++) {
			cimm1.add(zombies[i]);
			cimm1.add(items[i]);
		}
		for (int i = 0; i < 100; i++) {
			for (EntityMob mob : cimm1.getByClass(EntityMob.class)) {
				mob.addedToChunk = false;
			}
		}
	}

	@Benchmark
	public void addEntitiesThenLookupFoamy() {
		FoamyClassInheritanceMultiMap<Entity> cimm2 = new FoamyClassInheritanceMultiMap<Entity>(Entity.class);
		for (int i = 0; i < 100; i++) {
			cimm2.add(zombies[i]);
			cimm2.add(items[i]);
		}
		for (int i = 0; i < 100; i++) {
			for (EntityMob mob : cimm2.getByClass(EntityMob.class)) {
				mob.addedToChunk = false;
			}
		}
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
				.include(BenchmarkCIMM.class.getSimpleName())
				.forks(1)
				.build();

		new Runner(opt).run();
	}
}
