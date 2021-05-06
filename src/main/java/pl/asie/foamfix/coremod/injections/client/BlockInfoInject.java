/*
 * Copyright (C) 2016, 2017, 2018, 2019, 2020, 2021 Adrian Siekierka
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

package pl.asie.foamfix.coremod.injections.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Created by asie on 4/4/17.
 */
public class BlockInfoInject implements IFoamFixPatchedBlockInfo {
    private BlockPos blockPos;
    private IBlockAccess world;
    private IBlockState state;
    private final int[][][] s = new int[3][3][3];
    private final int[][][] b = new int[3][3][3];
    private final float[][][] ao = new float[3][3][3];

    @Override
    public int[][][] getRawS() {
        return s;
    }

    @Override
    public int[][][] getRawB() {
        return b;
    }

    @Override
    public void updateAO() {
        for (int x = 0; x <= 2; x++)
            for (int y = 0; y <= 2; y++)
                for (int z = 0; z <= 2; z++) {
                    BlockPos pos = blockPos.add(x - 1, y - 1, z - 1);
                    IBlockState state = world.getBlockState(pos);
                    ao[x][y][z] = state.getAmbientOcclusionLightValue();
                }
    }
}