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

package pl.asie.foamfix.mixin.client;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.asie.foamfix.FoamFixBootstrap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(SimpleBakedModel.class)
public class MixinSimpleBakedModel {
	@Inject(method = "<init>", at = @At("TAIL"))
	public void construct(List<BakedQuad> p_i46535_1_, Map<EnumFacing, List<BakedQuad>> p_i46535_2_, boolean p_i46535_3_, boolean p_i46535_4_, TextureAtlasSprite p_i46535_5_, ItemCameraTransforms p_i46535_6_, ItemOverrideList p_i46535_7_, CallbackInfo ci) {
		if (FoamFixBootstrap.optimizeBakedModelStorage) {
			if (p_i46535_1_ instanceof ArrayList) {
				((ArrayList<BakedQuad>) p_i46535_1_).trimToSize();
			}

			for (List<BakedQuad> l : p_i46535_2_.values()) {
				if (l instanceof ArrayList) {
					((ArrayList<BakedQuad>) l).trimToSize();
				}
			}
		}
	}
}