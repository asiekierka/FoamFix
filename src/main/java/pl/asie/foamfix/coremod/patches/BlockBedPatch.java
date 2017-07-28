package pl.asie.foamfix.coremod.patches;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockBedPatch extends BlockBed {
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        EnumFacing enumfacing = state.getValue(FACING);

        if (state.getValue(PART) == BlockBed.EnumPartType.FOOT) {
            pos = pos.offset(enumfacing);
            if ((fromPos == null || fromPos.equals(pos)) && worldIn.getBlockState(pos).getBlock() != this) {
                worldIn.setBlockToAir(pos);
            }
        } else {
            pos = pos.offset(enumfacing.getOpposite());
            if ((fromPos == null || fromPos.equals(pos)) && worldIn.getBlockState(pos).getBlock() != this) {
                if (!worldIn.isRemote) {
                    this.dropBlockAsItem(worldIn, pos, state, 0);
                }

                worldIn.setBlockToAir(pos);
            }
        }
    }
}
