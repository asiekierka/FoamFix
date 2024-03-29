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
package pl.asie.foamfix.ghostbuster;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class GhostBusterSafeAccessors {
	public static boolean isAreaLoaded(IBlockAccess access, BlockPos pos, int radius) {
		if (access instanceof World) {
			return ((World) access).isAreaLoaded(pos, radius);
		} else {
			return true;
		}
	}

	public static boolean isBlockLoaded(IBlockAccess access, BlockPos pos) {
		if (access instanceof World) {
			return ((World) access).isBlockLoaded(pos);
		} else {
			return true;
		}
	}

	public static IBlockState getBlockState(IBlockAccess access, BlockPos pos) {
		if (access instanceof World) {
			if (!((World) access).isOutsideBuildHeight(pos)) {
				Chunk c = ((World) access).getChunkProvider().getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4);
				if (c != null) {
					return c.getBlockState(pos);
				}
			}

			return Blocks.AIR.getDefaultState();
		} else {
			return access.getBlockState(pos);
		}
	}
}
