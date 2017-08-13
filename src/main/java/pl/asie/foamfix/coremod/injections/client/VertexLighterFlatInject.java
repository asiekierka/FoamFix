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
