package pl.asie.foamfix.coremod;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Created by asie on 1/7/17.
 */
public class BlockInfoPatch {
	private IBlockAccess world;
	private IBlockState state;
	private BlockPos blockPos;

	private final boolean[][][] translucent = new boolean[3][3][3];
	private final int[][][] s = new int[3][3][3];
	private final int[][][] b = new int[3][3][3];
	private final float[][][][] skyLight = new float[3][2][2][2];
	private final float[][][][] blockLight = new float[3][2][2][2];
	private final float[][][] ao = new float[3][3][3];

	public void updateLightMatrix() {
		boolean full = false;

		// FOAMFIX: Instead of generating 27 objects, we can really generate
		// just one - and we don't save much speed by not doing so either.
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int xo = blockPos.getX() - 1;
		int yo = blockPos.getY() - 1;
		int zo = blockPos.getZ() - 1;

		for(int x = 0; x <= 2; x++) {
			for(int y = 0; y <= 2; y++) {
				for(int z = 0; z <= 2; z++) {
					pos.setPos(xo+x, yo+y, zo+z);
					IBlockState state = world.getBlockState(pos);
					translucent[x][y][z] = state.isTranslucent();
					//translucent[x][y][z] = world.getBlockState(pos).getBlock().getLightOpacity(world, pos) == 0;
					int brightness = state.getPackedLightmapCoords(world, pos);
					s[x][y][z] = (brightness >> 0x14) & 0xF;
					b[x][y][z] = (brightness >> 0x04) & 0xF;
					ao[x][y][z] = state.getAmbientOcclusionLightValue();
					if(x == 1 && y == 1 && z == 1) {
						full = state.isFullCube();
					}
				}
			}
		}

		if(!full) {
			for(EnumFacing side : EnumFacing.values()) {
				int x = side.getFrontOffsetX() + 1;
				int y = side.getFrontOffsetY() + 1;
				int z = side.getFrontOffsetZ() + 1;
				s[x][y][z] = Math.max(s[1][1][1] - 1, s[x][y][z]);
				b[x][y][z] = Math.max(b[1][1][1] - 1, b[x][y][z]);
			}
		}

		for(int x = 0; x < 2; x++) {
			int x1 = x * 2;
			for(int y = 0; y < 2; y++) {
				int y1 = y * 2;
				for(int z = 0; z < 2; z++) {
					int z1 = z * 2;

					boolean tx = translucent[x1][1][z1] || translucent[x1][y1][1];
					skyLight[0][x][y][z] = combine(s[x1][1][1], s[x1][1][z1], s[x1][y1][1], tx ? s[x1][y1][z1] : s[x1][1][1]);
					blockLight[0][x][y][z] = combine(b[x1][1][1], b[x1][1][z1], b[x1][y1][1], tx ? b[x1][y1][z1] : b[x1][1][1]);

					boolean ty = translucent[x1][y1][1] || translucent[1][y1][z1];
					skyLight[1][x][y][z] = combine(s[1][y1][1], s[x1][y1][1], s[1][y1][z1], ty ? s[x1][y1][z1] : s[1][y1][1]);
					blockLight[1][x][y][z] = combine(b[1][y1][1], b[x1][y1][1], b[1][y1][z1], ty ? b[x1][y1][z1] : b[1][y1][1]);

					// FOAMFIX: typo fix - should be in Forge soon
					boolean tz = translucent[1][y1][z1] || translucent[x1][1][z1];
					skyLight[2][x][y][z] = combine(s[1][1][z1], s[1][y1][z1], s[x1][1][z1], tz ? s[x1][y1][z1] : s[1][1][z1]);
					blockLight[2][x][y][z] = combine(b[1][1][z1], b[1][y1][z1], b[x1][1][z1], tz ? b[x1][y1][z1] : b[1][1][z1]);
				}
			}
		}
	}

	private float combine(int i, int i1, int i2, int i3) {
		return 0;
	}
}
