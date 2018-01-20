package pl.asie.foamfix.ghostbuster.injections;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class GBWrapUpdateTick {
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		if (worldIn.isAreaLoaded(pos, 1)) {
			updateTick_foamfix_old(worldIn, pos, state, rand);
		}
	}

	public void updateTick_foamfix_old(World worldIn, BlockPos pos, IBlockState state, Random rand) {

	}
}
