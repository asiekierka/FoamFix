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

package pl.asie.foamfix.coremod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer;
import net.minecraftforge.client.model.pipeline.VertexLighterSmoothAo;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import pl.asie.foamfix.coremod.injections.VertexLighterSmoothAoCheap;
import pl.asie.foamfix.shared.FoamFixShared;
import pl.asie.foamfix.util.MethodHandleHelper;

import java.lang.invoke.MethodHandle;

public class VertexLighterOverrideHandler {
	private static final MethodHandle SMOOTH_LIGHT_GETTER = MethodHandleHelper.findFieldGetter(ForgeBlockModelRenderer.class, "lighterSmooth");
	private static final MethodHandle SMOOTH_LIGHT_SETTER = MethodHandleHelper.findFieldSetter(ForgeBlockModelRenderer.class, "lighterSmooth");
	private boolean isUsingMinimumLighter;

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END && FoamFixShared.isCoremod) {
			updateSmoothLighting();
		}
	}

	public void updateSmoothLighting() {
		BlockModelRenderer bmr = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer();
		if (bmr instanceof ForgeBlockModelRenderer && Minecraft.getMinecraft().world == null) {
			boolean newUse = (Minecraft.getMinecraft().gameSettings.ambientOcclusion == 1);
			newUse &= FoamFixShared.config.clCheapMinimumLighter;

			if (isUsingMinimumLighter != newUse) {
				isUsingMinimumLighter = newUse;

				try {
					SMOOTH_LIGHT_SETTER.invoke(Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer(),
							isUsingMinimumLighter ? ThreadLocal.withInitial(() -> new VertexLighterSmoothAoCheap(Minecraft.getMinecraft().getBlockColors()))
									: ThreadLocal.withInitial(() -> new VertexLighterSmoothAo(Minecraft.getMinecraft().getBlockColors())));
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}
}
