/*
 * Copyright (C) 2016, 2017 Adrian Siekierka
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
import org.apache.commons.lang3.tuple.Pair;
import pl.asie.foamfix.util.FoamUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

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
        Map<IModel, IBakedModel> bakedModels = new MapMaker().concurrencyLevel(ForkJoinPool.commonPool().getPoolSize() + 1).initialCapacity(stateModels.size()).makeMap();
        Set<IModel> modelsParallel = new HashSet<>(stateModels.size() - 1);
        Multimap<IModel, ModelResourceLocation> models = MultimapBuilder.hashKeys().linkedListValues().build();

        boolean foundFancyMissingModel = false;
        for (Map.Entry<ModelResourceLocation, IModel> modelEntry : stateModels.entrySet()) {
            if (!foundFancyMissingModel && modelEntry.getValue().getClass().getName().equals("net.minecraftforge.client.model.FancyMissingModel")) {
                models.put(modelEntry.getValue(), modelEntry.getKey());
                foundFancyMissingModel = true;
            } else {
                modelsParallel.add(modelEntry.getValue());
            }
        }

        ModelLoaderParallelHelper.bake(bakedModels, models, getMissingModel(), missingBaked);
        ModelLoaderParallelHelper.bakeParallel(bakedModels, modelsParallel, getMissingModel(), missingBaked);

        for (Map.Entry<ModelResourceLocation, IModel> e : stateModels.entrySet()) {
            bakedRegistry.putObject(e.getKey(), bakedModels.get(e.getValue()));
        }
        return bakedRegistry;
    }
}
