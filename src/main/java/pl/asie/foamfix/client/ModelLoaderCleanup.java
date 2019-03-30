/*
 * Copyright (C) 2016, 2017, 2018, 2019 Adrian Siekierka
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

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import pl.asie.foamfix.FoamFix;
import pl.asie.foamfix.util.MethodHandleHelper;

import java.lang.invoke.MethodHandle;
import java.util.Map;

public class ModelLoaderCleanup {
	public class Ticker {
		@SubscribeEvent
		public void onClientTick(TickEvent.ClientTickEvent event) {
			if (loader != null) {
				FoamFix.logger.info("Cleaning up ModelLoader...");
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

				loader = null;
				MinecraftForge.EVENT_BUS.unregister(this);
			}
		}
	}

	private static final MethodHandle LOADING_EXCEPTIONS_GETTER = MethodHandleHelper.findFieldGetter(ModelLoader.class, "loadingExceptions");
	private static final MethodHandle STATE_MODELS_GETTER = MethodHandleHelper.findFieldGetter(ModelLoader.class, "stateModels");
	private final Ticker ticker = new Ticker();
	private ModelLoader loader;

	public void tick() {
		ticker.onClientTick(null);
	}

	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		loader = event.getModelLoader();
		MinecraftForge.EVENT_BUS.register(ticker);
	}
}
