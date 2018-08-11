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

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer;
import net.minecraftforge.client.model.pipeline.VertexLighterSmoothAo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import pl.asie.foamfix.client.*;
import pl.asie.foamfix.common.WorldNuller;
import pl.asie.foamfix.coremod.VertexLighterOverrideHandler;
import pl.asie.foamfix.coremod.injections.VertexLighterSmoothAoCheap;
import pl.asie.foamfix.shared.FoamFixShared;
import pl.asie.foamfix.util.FoamUtils;
import pl.asie.foamfix.util.MethodHandleHelper;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.util.List;

public class ProxyClient extends ProxyCommon {
	public static Deduplicator deduplicator = new Deduplicator();
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

	private ModelLoaderCleanup cleanup;

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

		if (!FoamFixShared.config.geDeduplicate) {
			deduplicator = null;
		}

		if (FoamFixShared.config.clDynamicItemModels) {
			FoamFixDynamicItemModels.register();
		}

		if (FoamFixShared.config.clModelLoaderCleanup) {
			cleanup = new ModelLoaderCleanup();
			MinecraftForge.EVENT_BUS.register(cleanup);
		}

		updateFasterAnimationFlag();
	}

	public void updateFasterAnimationFlag() {
		if (FoamFixShared.config.txFasterAnimation > 0) {
			ContextCapabilities caps = GLContext.getCapabilities();
			boolean copyImageSupported = caps.OpenGL43 || caps.GL_ARB_copy_image;
			if(!copyImageSupported) {
				FoamFix.logger.warn("Fast animated textures require OpenGL 4.3 or ARB_copy_image extension, which were not detected. Using original slow path.");
				FoamFix.shouldFasterAnimation = false;
			} else {
			    String vendor = GL11.glGetString(GL11.GL_VENDOR);
			    if ("Advanced Micro Devices, Inc.".equals(vendor) || "ATI Technologies Inc.".equals(vendor) || vendor.startsWith("AMD ") || vendor.startsWith("ATI ") || FoamFixShared.config.txFasterAnimation == 2) {
                    FoamFix.logger.info("Using fast animated textures.");
                    FoamFix.shouldFasterAnimation = true;
                } else {
                    FoamFix.logger.warn("Fast animated textures currently only seem to boost performance on AMD cards. Using original slow path.");
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

		if (cleanup != null) {
			cleanup.tick();
		}
	}

	@Override
	public void postInit() {
		super.postInit();
	}

	@Override
	public void refreshResources() {
		Minecraft.getMinecraft().scheduleResourcesRefresh();
	}
}
