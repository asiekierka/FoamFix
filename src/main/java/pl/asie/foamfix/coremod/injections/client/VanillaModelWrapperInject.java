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

package pl.asie.foamfix.coremod.injections.client;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.animation.ModelBlockAnimation;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import pl.asie.foamfix.util.FoamUtils;

import java.util.List;
import java.util.function.Function;

public class VanillaModelWrapperInject {
	private ModelBlockAnimation animation;

	private IBakedModel bakeNormal(ModelBlock model, IModelState perState, final IModelState modelState, List<TRSRTransformation> newTransforms, final VertexFormat format, final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter, boolean uvLocked) {
		IBakedModel bakedModel = bakeNormal_foamfix_old(model, perState, modelState, newTransforms, format, bakedTextureGetter, uvLocked);
		if (bakedModel instanceof PerspectiveMapWrapper && (animation == null || animation.getClips().isEmpty())) {
			try {
				return new PerspectiveMapWrapper((IBakedModel) FoamUtils.PMW_GET_PARENT.invokeExact((PerspectiveMapWrapper) bakedModel), perState);
			} catch (Throwable t) {
				t.printStackTrace();
				return bakedModel;
			}
		} else {
			return bakedModel;
		}
	}

	private IBakedModel bakeNormal_foamfix_old(ModelBlock model, IModelState perState, final IModelState modelState, List<TRSRTransformation> newTransforms, final VertexFormat format, final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter, boolean uvLocked) {
		return null;
	}
}
