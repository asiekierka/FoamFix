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

import com.google.common.base.Joiner;
import com.google.common.collect.Multimap;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.ITextureMapPopulator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.ProgressManager;

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

    public static void bakeParallel(Map<IModel, IBakedModel> bakedModels, Set<IModel> models, IModel missingModel, IBakedModel missingBaked) {
        ProgressManager.ProgressBar bakeBar = ProgressManager.push("ModelLoader: baking (parallel)", 1);
        bakeBar.step("please wait");

        models.parallelStream().forEach((model -> {
            if (model == missingModel) {
                bakedModels.put(model, missingBaked);
            } else {
                IBakedModel bakedModel = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
                bakedModels.put(model, bakedModel);
            }
        }));

        ProgressManager.pop(bakeBar);
    }

    public static void bake(Map<IModel, IBakedModel> bakedModels, Multimap<IModel, ModelResourceLocation> models, IModel missingModel, IBakedModel missingBaked) {
        ProgressManager.ProgressBar bakeBar = ProgressManager.push("ModelLoader: baking", models.size());

        for (IModel model : models.keySet()) {
            bakeBar.step("[" + Joiner.on(", ").join(models.get(model)) + "]");
            if (model == missingModel) {
                bakedModels.put(model, missingBaked);
            } else {
                IBakedModel bakedModel = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
                bakedModels.put(model, bakedModel);
            }
        }

        ProgressManager.pop(bakeBar);
    }
}
