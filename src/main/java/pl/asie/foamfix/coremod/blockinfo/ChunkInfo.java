package pl.asie.foamfix.coremod.blockinfo;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ChunkInfo {
	private final boolean[][][] initialized = new boolean[18][18][18];
	private final boolean[][][] translucent = new boolean[18][18][18];
	private final boolean[][][] fullCube = new boolean[18][18][18];
	private final int[][][] s = new int[18][18][18];
	private final int[][][] b = new int[18][18][18];
	private final float[][][] ao = new float[18][18][18];

	private final BlockPos pos;

	public ChunkInfo(BlockPos pos) {
		this.pos = pos.add(-1, -1, -1);
	}

	public boolean fill(IBlockAccess access, BlockPos blockPos, boolean[][][] translucent, int[][][] s, int[][][] b, float[][][] ao) {
		int xo = blockPos.getX() - 1 - pos.getX();
		int yo = blockPos.getY() - 1 - pos.getY();
		int zo = blockPos.getZ() - 1 - pos.getZ();

		for(int x = 0; x <= 2; x++) {
			for (int y = 0; y <= 2; y++) {
				for (int z = 0; z <= 2; z++) {
					int xb = xo+x;
					int yb = yo+y;
					int zb = zo+z;

					if (!initialized[xb][yb][zb])
						initialize(access,xb,yb,zb);

					translucent[x][y][z] = this.translucent[xb][yb][zb];
					s[x][y][z] = this.s[xb][yb][zb];
					b[x][y][z] = this.b[xb][yb][zb];
					ao[x][y][z] = this.ao[xb][yb][zb];
				}
			}
		}

		return fullCube[xo+1][yo+1][zo+1];
	}

	private void initialize(IBlockAccess access, int x, int y, int z) {
		BlockPos pos = this.pos.add(x, y, z);
		IBlockState state = access.getBlockState(pos);
		translucent[x][y][z] = state.isTranslucent();
		int brightness = state.getPackedLightmapCoords(access, pos);
		s[x][y][z] = (brightness >> 0x14) & 0xF;
		b[x][y][z] = (brightness >> 0x04) & 0xF;
		ao[x][y][z] = state.getAmbientOcclusionLightValue();
		fullCube[x][y][z] = state.isFullCube();
		initialized[x][y][z] = true;
	}

	@Override
	public int hashCode() {
		return pos.hashCode();
	}
}
