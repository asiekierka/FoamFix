package pl.asie.foamfix.coremod.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Created by asie on 4/4/17.
 */
public class BlockInfoPatch implements IFoamFixBlockInfoDataProxy {
    private BlockPos blockPos;
    private IBlockAccess world;
    private IBlockState state;
    private final int[][][] s = new int[3][3][3];
    private final int[][][] b = new int[3][3][3];

    @Override
    public int[][][] getRawS() {
        return s;
    }

    @Override
    public int[][][] getRawB() {
        return b;
    }

    private final void updateRawBS(int x, int y, int z) {
        BlockPos pos = blockPos.add(x - 1, y - 1, z - 1);
        int brightness = state.getPackedLightmapCoords(world, pos);
        s[x][y][z] = (brightness >> 0x14) & 0xF;
        b[x][y][z] = (brightness >> 0x04) & 0xF;
    }

    @Override
    public void updateRawBS() {
        updateRawBS(1, 1, 1);
        updateRawBS(1, 0, 1);
        updateRawBS(1, 2, 1);
        updateRawBS(0, 1, 1);
        updateRawBS(2, 1, 1);
        updateRawBS(1, 1, 0);
        updateRawBS(1, 1, 2);
    }
}