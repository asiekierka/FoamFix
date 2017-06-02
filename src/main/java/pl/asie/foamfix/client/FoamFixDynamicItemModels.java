package pl.asie.foamfix.client;

import com.google.common.base.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.foamfix.util.MethodHandleHelper;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.Set;

public class FoamFixDynamicItemModels {
    public enum Loader implements ICustomModelLoader {
        INSTANCE;

        private static final IModel model = new FoamyItemLayerModel(ItemLayerModel.instance);

        public void onResourceManagerReload(IResourceManager resourceManager) {
            ItemLayerModel.Loader.instance.onResourceManagerReload(resourceManager);
        }

        public boolean accepts(ResourceLocation modelLocation) {
            return ItemLayerModel.Loader.instance.accepts(modelLocation);
        }

        public IModel loadModel(ResourceLocation modelLocation) {
            return model;
        }
    }

    public static void register() {
        MethodHandle LOADERS = MethodHandleHelper.findFieldGetter(ModelLoaderRegistry.class, "loaders");
        try {
            Set<ICustomModelLoader> loaders = (Set<ICustomModelLoader>) LOADERS.invoke();
            loaders.remove(ItemLayerModel.Loader.instance);
            loaders.add(Loader.INSTANCE);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public IFlexibleBakedModel bake(final IModelState state, final VertexFormat format, final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return FoamyItemLayerModel.bake(((ItemLayerModel) ((Object) this)), state, format, bakedTextureGetter);
    }
}
