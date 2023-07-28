/**
 * Copyright (C) 2016, 2017, 2018, 2019, 2020, 2021 Adrian Siekierka
 *
 * This file is part of FoamFix.
 *
 * FoamFix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoamFix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FoamFix.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// A LinkedHashMap for a structure that will ever only be iterated is quite a bit much to ask, IMO
public class FoamyMultipartBakedModel implements IBakedModel {
    private static final MethodHandle SELECTORS_GETTER = MethodHandleHelper.findFieldGetter(MultipartBakedModel.class, "selectors", "field_188626_f");

    @SuppressWarnings("Guava")
    public final Predicate[] predicates;
    public final IBakedModel[] models;

    @SuppressWarnings("unchecked")
    public FoamyMultipartBakedModel(MultipartBakedModel parent) {
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
            List<BakedQuad> list = new ArrayList<>(predicates.length);
            for (int i = 0; i < predicates.length; i++) {
                if (predicates[i].apply(state)) {
                    list.addAll(models[i].getQuads(state, side, rand++));
                }
            }
            return list;
        }
    }

    public boolean isAmbientOcclusion() {
        return models[0].isAmbientOcclusion();
    }

    public boolean isGui3d() {
        return models[0].isGui3d();
    }

    public boolean isBuiltInRenderer() {
        return false;
    }

    public TextureAtlasSprite getParticleTexture() {
        return models[0].getParticleTexture();
    }

    @SuppressWarnings("deprecation")
    public ItemCameraTransforms getItemCameraTransforms() {
        return models[0].getItemCameraTransforms();
    }

    public ItemOverrideList getOverrides() {
        return models[0].getOverrides();
    }
}
