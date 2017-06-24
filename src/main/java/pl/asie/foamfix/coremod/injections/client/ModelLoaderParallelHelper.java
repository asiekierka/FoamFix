package pl.asie.foamfix.coremod.injections.client;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.client.renderer.texture.ITextureMapPopulator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.ProgressManager;
import pl.asie.foamfix.FoamFix;
import pl.asie.foamfix.util.FoamUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModelLoaderParallelHelper extends ModelBakery {
    public static Set<ResourceLocation> textures;

    public static final ITextureMapPopulator POPULATOR = new ITextureMapPopulator() {
        public void registerSprites(TextureMap map) {
            for (ResourceLocation t : textures) {
                map.registerSprite(t);
            }
        }
    };

    public ModelLoaderParallelHelper(IResourceManager manager, TextureMap map, BlockModelShapes shapes) {
        super(manager, map, shapes);
    }

    public static void bake(Map<IModel, IBakedModel> bakedModels, Multimap<IModel, ModelResourceLocation> models, IModel missingModel, IBakedModel missingBaked, boolean parallel) {
        ProgressManager.ProgressBar bakeBar = ProgressManager.push("ModelLoader: baking", models.keySet().size());

        if (parallel) {
            models.keySet().parallelStream().forEach((model -> {
                synchronized (bakeBar) {
                    bakeBar.step("[" + Joiner.on(", ").join(models.get(model)) + "]");
                }
                if (model == missingModel) {
                    bakedModels.put(model, missingBaked);
                } else {
                    bakedModels.put(model, model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter()));
                }
            }));
        } else {
            for (IModel model : models.keys()) {
                synchronized (bakeBar) {
                    bakeBar.step("[" + Joiner.on(", ").join(models.get(model)) + "]");
                }
                if (model == missingModel) {
                    bakedModels.put(model, missingBaked);
                } else {
                    bakedModels.put(model, model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter()));
                }
            }
        }

        ProgressManager.pop(bakeBar);
    }
}
