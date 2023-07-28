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
package pl.asie.foamfix.coremod.injections;

import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.asie.foamfix.api.IFoamFixMobSpawnerLogic;
import pl.asie.foamfix.shared.FoamFixShared;

public class CachingMobSpawnerLogicInject extends MobSpawnerBaseLogic implements IFoamFixMobSpawnerLogic {
    private boolean foamfix_activatedCache;
    private int foamfix_activatedCachePESize;
    private long foamfix_activatedCacheTime;

    private boolean foamfix_forcedCache;
    private long foamfix_forcedCacheTime;

    @Override
    public boolean isActivated() {
        World world = getSpawnerWorld();
        int peSize = world.playerEntities.size();
        long time = world.getTotalWorldTime();
        if (time == foamfix_forcedCacheTime) {
            return foamfix_forcedCache;
        }

        // Try to detect mods like PNC:Repressurized, which add a fake player
        // to the list for a fraction of a tick.
        if (peSize != foamfix_activatedCachePESize) {
            foamfix_activatedCacheTime = 0;
        }

        if (time < foamfix_activatedCacheTime) {
            return foamfix_activatedCache;
        }

        //System.out.println("update? " + time + " " + foamfix_activatedCacheTime);
        foamfix_activatedCacheTime = time + (world.rand.nextInt()&1) + FoamFixShared.config.geMobSpawnerCheckSpeed;
        foamfix_activatedCachePESize = peSize;
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

    @Override
    public boolean forceSpawnActivationFlag(boolean value) {
        World world = getSpawnerWorld();
        long time = world.getTotalWorldTime();

        foamfix_forcedCacheTime = time;
        foamfix_forcedCache = value;

        return true;
    }
}
