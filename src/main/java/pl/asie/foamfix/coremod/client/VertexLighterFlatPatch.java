package pl.asie.foamfix.coremod.client;

import com.google.common.base.Objects;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.BlockInfo;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.QuadGatheringTransformer;
import pl.asie.foamfix.client.FoamyConstants;

public class VertexLighterFlatPatch extends QuadGatheringTransformer {
    protected BlockInfo blockInfo;
    protected int posIndex, normalIndex, colorIndex, lightmapIndex;

    @Override
    public void setParent(IVertexConsumer parent) {
        super.setParent(parent);

        if(Objects.equal(getVertexFormat(), parent.getVertexFormat())) return;
        VertexFormat targetFormat = getVertexFormatWithNormal(parent);
        if(Objects.equal(getVertexFormat(), targetFormat)) return;

        setVertexFormat(targetFormat);

        for(int i = 0; i < getVertexFormat().getElementCount(); i++) {
            // FIXME: could really use a switch statement...
            VertexFormatElement.EnumUsage usage = getVertexFormat().getElement(i).getUsage();
            if (usage == VertexFormatElement.EnumUsage.POSITION)
                posIndex = i;
            else if (usage == VertexFormatElement.EnumUsage.NORMAL)
                normalIndex = i;
            else if (usage == VertexFormatElement.EnumUsage.COLOR)
                colorIndex = i;
            else if (usage == VertexFormatElement.EnumUsage.UV)
                if(getVertexFormat().getElement(i).getIndex() == 1)
                    lightmapIndex = i;
        }

        if(posIndex == -1)
            throw new IllegalArgumentException("vertex lighter needs format with position");
        if(lightmapIndex == -1)
            throw new IllegalArgumentException("vertex lighter needs format with lightmap");
        if(colorIndex == -1)
            throw new IllegalArgumentException("vertex lighter needs format with color");
    }

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

    public static VertexFormat getVertexFormatWithNormal(IVertexConsumer parent) {
        VertexFormat format = parent.getVertexFormat();
        if(format.hasNormal()) return format;
        format = new VertexFormat(format);
        format.addElement(FoamyConstants.VLF_NORMAL);
        return format;
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
