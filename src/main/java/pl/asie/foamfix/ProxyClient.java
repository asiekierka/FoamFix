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

/**
 * This file is part of FoamFixAPI.
 *
 * FoamFixAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoamFixAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FoamFixAPI.  If not, see <http://www.gnu.org/licenses/>.
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
package pl.asie.foamfix;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import pl.asie.foamfix.client.*;
import pl.asie.foamfix.client.deduplicator.FoamFixModelDeduplicate;
import pl.asie.foamfix.common.WorldNuller;
import pl.asie.foamfix.shared.FoamFixShared;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.List;

public class ProxyClient extends ProxyCommon {
	public static int bakingStage = 0;

	public static final IBakedModel DUMMY_MODEL = new IBakedModel() {
		private final ItemOverrideList itemOverrideList = ItemOverrideList.NONE;

		@Override
		public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
			return ImmutableList.of();
		}

		@Override
		public boolean isAmbientOcclusion() {
			return false;
		}

		@Override
		public boolean isGui3d() {
			return false;
		}

		@Override
		public boolean isBuiltInRenderer() {
			return false;
		}

		@Override
		public TextureAtlasSprite getParticleTexture() {
			return Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(TextureMap.LOCATION_MISSING_TEXTURE.toString());
		}

		@Override
		public ItemOverrideList getOverrides() {
			return itemOverrideList;
		}
	};

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onModelRegistry(ModelRegistryEvent event) {
		bakingStage = 0;
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onModelBake(ModelBakeEvent event) {
		bakingStage = 1;

		FoamFixModelDeduplicate.INSTANCE.onModelBake(event);
	}

	@Override
	public void preInit() {
		super.preInit();

		if (FoamFixShared.isCoremod) {
			try {
				MinecraftForge.EVENT_BUS.register(Class.forName("pl.asie.foamfix.coremod.VertexLighterOverrideHandler").newInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (FoamFixShared.config.clDynamicItemModels) {
			FoamFixDynamicItemModels.register();
		}

		updateFasterAnimationFlag();
	}

	public void updateFasterAnimationFlag() {
		if (FoamFixShared.config.txFasterAnimation > 0) {
			ContextCapabilities caps = GLContext.getCapabilities();
			boolean copyImageSupported = caps.OpenGL43 || caps.GL_ARB_copy_image;
			if (!copyImageSupported) {
				if (FoamFixShared.config.txFasterAnimation == 2) {
					FoamFix.getLogger().warn("Fast animated textures require OpenGL 4.3 or ARB_copy_image extension, which were not detected. Using original slow path.");
				}
				FoamFix.shouldFasterAnimation = false;
			} else {
			    String vendor = GL11.glGetString(GL11.GL_VENDOR);
			    if (/* "Advanced Micro Devices, Inc.".equals(vendor) || "ATI Technologies Inc.".equals(vendor) || vendor.startsWith("AMD ") || vendor.startsWith("ATI ") || */ FoamFixShared.config.txFasterAnimation == 2) {
                    FoamFix.getLogger().info("Using fast animated textures.");
                    FoamFix.shouldFasterAnimation = true;
                } else {
                    // FoamFix.logger.warn("Fast animated textures currently only seem to boost performance on AMD cards. Using original slow path.");
                    FoamFix.shouldFasterAnimation = false;
                }
			}
		}
	}

	@Override
	public void init() {
		super.init();
		// MinecraftForge.EVENT_BUS.register(PleaseTrustMeLookImADolphin.INSTANCE);

		if (FoamFixShared.config.gbNotifyNonUnloadedWorlds) {
			WorldNuller.initClient();
		}

		if (FoamFixShared.config.clCleanRedundantModelRegistry) {
			MinecraftForge.EVENT_BUS.register(new FoamFixModelRegistryDuplicateWipe());
		}
	}

	@Override
	public void postInit() {
		super.postInit();
	}

	@Override
	public void refreshResources() {
		FMLClientHandler.instance().scheduleResourcesRefresh((type) -> true);
	}

	@Override
	public void openUrlLinux(URI url) {
		try {
			new ProcessBuilder().command("xdg-open", url.toString()).start();
		} catch (Throwable t) {
			FoamFix.getLogger().error("Couldn't open link!", t);
		}
	}
}
