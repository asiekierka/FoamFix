package pl.asie.foamfix.coremod.injections;

import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.VertexLighterSmoothAo;
import pl.asie.foamfix.coremod.injections.client.IFoamFixPatchedBlockInfo;

public class VertexLighterSmoothAoCheap extends VertexLighterSmoothAo {
	public VertexLighterSmoothAoCheap(BlockColors colors) {
		super(colors);
	}

	@Override
	protected void updateLightmap(float[] normal, float[] lightmap, float x, float y, float z) {
		final float e1 = 1f - 1e-2f;
		final float e2 = 0.95f;

		boolean full = blockInfo.isFullCube();
		EnumFacing side = null;

		if((full || y < -e1) && normal[1] < -e2) side = EnumFacing.DOWN;
		else if((full || y >  e1) && normal[1] >  e2) side = EnumFacing.UP;
		else if((full || z < -e1) && normal[2] < -e2) side = EnumFacing.NORTH;
		else if((full || z >  e1) && normal[2] >  e2) side = EnumFacing.SOUTH;
		else if((full || x < -e1) && normal[0] < -e2) side = EnumFacing.WEST;
		else if((full || x >  e1) && normal[0] >  e2) side = EnumFacing.EAST;

		int i = side == null ? 0 : side.ordinal() + 1;
		int brightness = blockInfo.getPackedLight()[i];

		lightmap[0] = ((float)((brightness >> 0x04) & 0xF) * 0x20) / 0xFFFF;
		lightmap[1] = ((float)((brightness >> 0x14) & 0xF) * 0x20) / 0xFFFF;
	}

	@Override
	public void updateBlockInfo() {
		blockInfo.updateShift();
		blockInfo.updateFlatLighting();
		((IFoamFixPatchedBlockInfo) blockInfo).updateAO();
	}
}
