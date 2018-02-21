/*
 * Copyright (C) 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of FoamFix.
 *
 * FoamFix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoamFix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FoamFix.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or
 * combining it with the Minecraft game engine, the Mojang Launchwrapper,
 * the Mojang AuthLib and the Minecraft Realms library (and/or modified
 * versions of said software), containing parts covered by the terms of
 * their respective licenses, the licensors of this Program grant you
 * additional permission to convey the resulting work.
 */

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
