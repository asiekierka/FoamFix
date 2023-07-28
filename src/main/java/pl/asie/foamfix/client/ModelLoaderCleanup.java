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

import net.minecraftforge.client.model.ModelLoader;
import pl.asie.foamfix.FoamFix;
import pl.asie.foamfix.shared.FoamFixShared;
import pl.asie.foamfix.util.MethodHandleHelper;

import java.lang.invoke.MethodHandle;
import java.util.Map;

public class ModelLoaderCleanup {
	private static final MethodHandle LOADING_EXCEPTIONS_GETTER = MethodHandleHelper.findFieldGetter(ModelLoader.class, "loadingExceptions");
	private static final MethodHandle STATE_MODELS_GETTER = MethodHandleHelper.findFieldGetter(ModelLoader.class, "stateModels");

	public static void cleanup(ModelLoader loader) {
		if (FoamFixShared.config.clModelLoaderCleanup) {
			FoamFix.getLogger().info("Cleaning up ModelLoader...");
			try {
				((Map) LOADING_EXCEPTIONS_GETTER.invoke(loader)).clear();
			} catch (Throwable t) {
				t.printStackTrace();
			}

			try {
				((Map) STATE_MODELS_GETTER.invoke(loader)).clear();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
}
