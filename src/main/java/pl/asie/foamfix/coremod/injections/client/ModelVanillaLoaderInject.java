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

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import pl.asie.foamfix.ProxyClient;

// Currently unused
public class ModelVanillaLoaderInject {
	public IModel loadModel(ResourceLocation modelLocation) throws Exception {
		IModel model = loadModel_foamfix_old(modelLocation);
		if (model != null && ProxyClient.bakingStage >= 1) {
			model.getDependencies();
			model.getTextures();
		}
		return model;
	}

	public IModel loadModel_foamfix_old(ResourceLocation modelLocation) {
		return null;
	}
}
