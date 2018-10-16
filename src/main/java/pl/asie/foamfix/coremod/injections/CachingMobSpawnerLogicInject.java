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

package pl.asie.foamfix.coremod.injections;

import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.foamfix.shared.FoamFixShared;

public class CachingMobSpawnerLogicInject extends MobSpawnerBaseLogic {
    private boolean foamfix_activatedCache;
    private long foamfix_activatedCacheTime;

    @Override
    public boolean isActivated() {
        World world = getSpawnerWorld();
        long time = world.getTotalWorldTime();
        if (time < foamfix_activatedCacheTime) {
            return foamfix_activatedCache;
        }

        //System.out.println("update? " + time + " " + foamfix_activatedCacheTime);
        foamfix_activatedCacheTime = time + (world.rand.nextInt()&1) + FoamFixShared.config.geMobSpawnerCheckSpeed;
        //System.out.println("update= " + time + " " + foamfix_activatedCacheTime);
        return (foamfix_activatedCache = isActivated_foamfix_old());
    }

    public boolean isActivated_foamfix_old() {
        // shim
        return false;
    }

    @Override
    public void broadcastEvent(int id) {

    }

    @Override
    public World getSpawnerWorld() {
        return null;
    }

    @Override
    public BlockPos getSpawnerPosition() {
        return null;
    }
}
