package pl.asie.foamfix.client;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import pl.asie.foamfix.util.MethodHandleHelper;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Map;

// A LinkedHashMap for an iterated structure is quite a bit much to ask, IMO
public class FoamyMultipartBakedModel implements IBakedModel {
    private static final MethodHandle SELECTORS_GETTER = MethodHandleHelper.findFieldGetter(MultipartBakedModel.class, "selectors", "field_188626_f");

    private final Predicate[] predicates;
    private final IBakedModel[] models;
    private final boolean ambientOcclusion;
    private final boolean gui3d;
    private final TextureAtlasSprite particleTexture;
    private final ItemCameraTransforms cameraTransforms;
    private final ItemOverrideList overrides;

    @SuppressWarnings("unchecked")
    public FoamyMultipartBakedModel(MultipartBakedModel parent) {
        this.ambientOcclusion = parent.isAmbientOcclusion();
        this.gui3d = parent.isGui3d();
        this.particleTexture = parent.getParticleTexture();
        this.cameraTransforms = parent.getItemCameraTransforms();
        this.overrides = parent.getOverrides();

        try {
            Map<Predicate<IBlockState>, IBakedModel> map = (Map<Predicate<IBlockState>, IBakedModel>) SELECTORS_GETTER.invoke(parent);

            predicates = new Predicate[map.size()];
            models = new IBakedModel[predicates.length];
            int i = 0;

            for (Map.Entry<Predicate<IBlockState>, IBakedModel> entry : map.entrySet()) {
                predicates[i] = entry.getKey();
                models[i++] = entry.getValue();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @SuppressWarnings("unchecked")
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (state == null) {
            return ImmutableList.of();
        } else {
            List<BakedQuad> list = Lists.<BakedQuad>newArrayList();
            for (int i = 0; i < predicates.length; i++) {
                if (predicates[i].apply(state)) {
                    list.addAll(models[i].getQuads(state, side, rand++));
                }
            }
            return list;
        }
    }

    public boolean isAmbientOcclusion()
    {
        return this.ambientOcclusion;
    }

    public boolean isGui3d()
    {
        return this.gui3d;
    }

    public boolean isBuiltInRenderer()
    {
        return false;
    }

    public TextureAtlasSprite getParticleTexture()
    {
        return this.particleTexture;
    }

    public ItemCameraTransforms getItemCameraTransforms()
    {
        return this.cameraTransforms;
    }

    public ItemOverrideList getOverrides()
    {
        return this.overrides;
    }
}
