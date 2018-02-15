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

package pl.asie.foamfix.coremod.injections.client;

import com.google.common.base.Objects;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.BlockInfo;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.QuadGatheringTransformer;
import pl.asie.foamfix.client.FoamyConstants;

public class VertexLighterFlatInject extends QuadGatheringTransformer {
    protected BlockInfo blockInfo;

    // lightmap patch

    @Override
    protected void processQuad() {
        // shim
    }

    protected void updateLightmap(float[] normal, float[] lightmap, float x, float y, float z) {
        float e1 = 1 - 1e-2f;
        float e2 = 0.95f;
        int posX = 1, posY = 1, posZ = 1;

        boolean full = blockInfo.getState().isFullCube();

        if((full || y < -e1) && normal[1] < -e2) posY--;
        else if((full || y >  e1) && normal[1] >  e2) posY++;

        if((full || z < -e1) && normal[2] < -e2) posZ--;
        else if((full || z >  e1) && normal[2] >  e2) posZ++;

        if((full || x < -e1) && normal[0] < -e2) posX--;
        else if((full || x >  e1) && normal[0] >  e2) posX++;

        lightmap[0] = ((float)((IFoamFixBlockInfoDataProxy) blockInfo).getRawB()[posX][posY][posZ] * 0x20) / 0xFFFF;
        lightmap[1] = ((float)((IFoamFixBlockInfoDataProxy) blockInfo).getRawS()[posX][posY][posZ] * 0x20) / 0xFFFF;
    }

    @Override
    public void setQuadTint(int tint) {

    }

    @Override
    public void setQuadOrientation(EnumFacing orientation) {

    }

    @Override
    public void setApplyDiffuseLighting(boolean diffuse) {

    }

    @Override
    public void setTexture(TextureAtlasSprite texture) {

    }

    public void updateBlockInfo_foamfix_old() {
    }

    public void updateBlockInfo() {
        updateBlockInfo_foamfix_old();
        ((IFoamFixBlockInfoDataProxy) blockInfo).updateRawBS();
    }
}
