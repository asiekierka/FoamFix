/*
 * Copyright (C) 2016, 2017, 2018, 2019, 2020, 2021 Adrian Siekierka
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

package pl.asie.foamfix.client;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import pl.asie.foamfix.util.MethodHandleHelper;

import java.lang.invoke.MethodHandle;
import java.util.Set;
import java.util.function.Function;

public class FoamFixDynamicItemModels {
    public enum Loader implements ICustomModelLoader {
        INSTANCE;

        private static final IModel model = new FoamyItemLayerModel(ItemLayerModel.INSTANCE);

        public void onResourceManagerReload(IResourceManager resourceManager) {
            ItemLayerModel.Loader.INSTANCE.onResourceManagerReload(resourceManager);
        }

        public boolean accepts(ResourceLocation modelLocation) {
            return ItemLayerModel.Loader.INSTANCE.accepts(modelLocation);
        }

        public IModel loadModel(ResourceLocation modelLocation) {
            return model;
        }
    }

    public static void register() {
        MethodHandle LOADERS = MethodHandleHelper.findFieldGetter(ModelLoaderRegistry.class, "loaders");
        try {
            Set<ICustomModelLoader> loaders = (Set<ICustomModelLoader>) LOADERS.invoke();
            loaders.remove(ItemLayerModel.Loader.INSTANCE);
            loaders.add(Loader.INSTANCE);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public IBakedModel bake(final IModelState state, final VertexFormat format, final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        return FoamyItemLayerModel.bakeStatic(((ItemLayerModel) ((Object) this)), state, format, bakedTextureGetter);
    }
}
