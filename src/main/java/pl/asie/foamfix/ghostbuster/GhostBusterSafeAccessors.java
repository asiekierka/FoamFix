package pl.asie.foamfix.ghostbuster;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class GhostBusterSafeAccessors {
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
