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

import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockBedInject extends BlockBed {
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        EnumFacing enumfacing = state.getValue(FACING);

        if (state.getValue(PART) == BlockBed.EnumPartType.FOOT) {
            BlockPos pos2 = pos.offset(enumfacing);
            if ((fromPos == null || fromPos.equals(pos2)) && worldIn.getBlockState(pos2).getBlock() != this) {
                worldIn.setBlockToAir(pos);
            }
        } else {
            BlockPos pos2 = pos.offset(enumfacing.getOpposite());
            if ((fromPos == null || fromPos.equals(pos2)) && worldIn.getBlockState(pos2).getBlock() != this) {
                if (!worldIn.isRemote) {
                    this.dropBlockAsItem(worldIn, pos, state, 0);
                }

                worldIn.setBlockToAir(pos);
            }
        }
    }
}
