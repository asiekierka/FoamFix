package pl.asie.foamfix.coremod.injections.client;

import com.google.common.base.Joiner;
import com.google.common.collect.*;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.ProgressManager;
import pl.asie.foamfix.util.FoamUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ModelBakeryParallelInject extends ModelBakery {
    private final Map<ModelResourceLocation, IModel> stateModels = Maps.newHashMap();
    private final Set<ModelResourceLocation> missingVariants = Sets.newHashSet();
    private final Map<ResourceLocation, Exception> loadingExceptions = Maps.newHashMap();
    private IModel missingModel = null;

    private boolean isLoading = false;

    public ModelBakeryParallelInject(IResourceManager resourceManagerIn, TextureMap textureMapIn, BlockModelShapes blockModelShapesIn) {
        super(resourceManagerIn, textureMapIn, blockModelShapesIn);
    }

    // shim
    protected IModel getMissingModel() {
        return null;
    }

    @Override
    public IRegistry<ModelResourceLocation, IBakedModel> setupModelRegistry() {
        if (FMLClientHandler.instance().hasError()) // skip loading models if we're just going to show a fatal error screen
            return bakedRegistry;

        isLoading = true;
        loadBlocks();
        loadVariantItemModels();
        missingModel = ModelLoaderRegistry.getMissingModel();
        stateModels.put(MODEL_MISSING, missingModel);

        try {
            final Set<ResourceLocation> textures = Sets.newHashSet((Iterable<ResourceLocation>) FoamUtils.MLR_GET_TEXTURES.invokeExact());
            textures.remove(TextureMap.LOCATION_MISSING_TEXTURE);
            textures.addAll(LOCATIONS_BUILTIN_TEXTURES);

            ModelLoaderParallelHelper.textures = textures;
            textureMap.loadSprites(resourceManager, ModelLoaderParallelHelper.POPULATOR);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        IBakedModel missingBaked = missingModel.bake(missingModel.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
        Map<IModel, IBakedModel> bakedModels = Maps.newConcurrentMap();
        HashMultimap<IModel, ModelResourceLocation> modelsParallel = HashMultimap.create();
        HashMultimap<IModel, ModelResourceLocation> models = HashMultimap.create();

        boolean foundFancyMissingModel = false;
        for (Map.Entry<ModelResourceLocation, IModel> modelEntry : stateModels.entrySet()) {
            if (!foundFancyMissingModel && modelEntry.getValue().getClass().getName().equals("net.minecraftforge.client.model.FancyMissingModel")) {
                models.put(modelEntry.getValue(), modelEntry.getKey());
                foundFancyMissingModel = true;
            } else {
                modelsParallel.put(modelEntry.getValue(), modelEntry.getKey());
            }
        }

        ModelLoaderParallelHelper.bake(bakedModels, models, getMissingModel(), missingBaked, false);
        ModelLoaderParallelHelper.bake(bakedModels, modelsParallel, getMissingModel(), missingBaked, true);

        for (Map.Entry<ModelResourceLocation, IModel> e : stateModels.entrySet())
        {
            bakedRegistry.putObject(e.getKey(), bakedModels.get(e.getValue()));
        }
        return bakedRegistry;
    }
}
