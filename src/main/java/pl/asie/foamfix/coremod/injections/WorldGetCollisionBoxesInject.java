package pl.asie.foamfix.coremod.injections;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;

import javax.annotation.Nullable;
import java.util.List;

/* Forge PR #3916 */
public class WorldGetCollisionBoxesInject extends World {
    protected WorldGetCollisionBoxesInject(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
    }

    @Override
    public boolean getCollisionBoxes(@Nullable Entity p_191504_1_, AxisAlignedBB p_191504_2_, boolean p_191504_3_, @Nullable List<AxisAlignedBB> p_191504_4_) {
        int i = MathHelper.floor(p_191504_2_.minX) - 1;
        int j = MathHelper.ceil(p_191504_2_.maxX) + 1;
        int k = MathHelper.floor(p_191504_2_.minY) - 1;
        int l = MathHelper.ceil(p_191504_2_.maxY) + 1;
        int i1 = MathHelper.floor(p_191504_2_.minZ) - 1;
        int j1 = MathHelper.ceil(p_191504_2_.maxZ) + 1;
        WorldBorder worldborder = this.getWorldBorder();
        boolean flag = p_191504_1_ != null && p_191504_1_.isOutsideBorder();
        boolean flag1 = p_191504_1_ != null && this.isInsideWorldBorder(p_191504_1_);
        IBlockState iblockstate = Blocks.STONE.getDefaultState();
        BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

        if (p_191504_3_) {
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.GetCollisionBoxesEvent(this, null, p_191504_2_, p_191504_4_));
            if (!p_191504_4_.isEmpty())
                return true;
        }
        try {
            for(int k1 = i; k1 < j; ++k1) {
                for(int l1 = i1; l1 < j1; ++l1) {
                    boolean flag2 = k1 == i || k1 == j - 1;
                    boolean flag3 = l1 == i1 || l1 == j1 - 1;
                    if((!flag2 || !flag3) && this.isBlockLoaded(blockpos$pooledmutableblockpos.setPos(k1, 64, l1))) {
                        for(int i2 = k; i2 < l; ++i2) {
                            if(!flag2 && !flag3 || i2 != l - 1) {
                                if(p_191504_3_) {
                                    if(k1 < -30000000 || k1 >= 30000000 || l1 < -30000000 || l1 >= 30000000) {
                                        return true;
                                    }
                                } else if(p_191504_1_ != null && flag == flag1) {
                                    p_191504_1_.setOutsideBorder(!flag1);
                                }

                                blockpos$pooledmutableblockpos.setPos(k1, i2, l1);
                                IBlockState iblockstate1;
                                if(!p_191504_3_ && !worldborder.contains(blockpos$pooledmutableblockpos) && flag1) {
                                    iblockstate1 = iblockstate;
                                } else {
                                    iblockstate1 = this.getBlockState(blockpos$pooledmutableblockpos);
                                }

                                iblockstate1.addCollisionBoxToList(this, blockpos$pooledmutableblockpos, p_191504_2_, p_191504_4_, p_191504_1_, false);
                                MinecraftForge.EVENT_BUS.post(new GetCollisionBoxesEvent(this, (Entity)null, p_191504_2_, p_191504_4_));

                                if (p_191504_3_ && !p_191504_4_.isEmpty())
                                {
                                    net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.GetCollisionBoxesEvent(this, null, p_191504_2_, p_191504_4_));
                                    if (!p_191504_4_.isEmpty())
                                        return true;
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            blockpos$pooledmutableblockpos.release();
        }

        return !p_191504_4_.isEmpty();
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return null;
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return false;
    }
}
