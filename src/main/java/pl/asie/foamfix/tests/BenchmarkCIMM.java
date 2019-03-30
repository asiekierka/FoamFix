/*
 * Copyright (C) 2016, 2017, 2018, 2019 Adrian Siekierka
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
