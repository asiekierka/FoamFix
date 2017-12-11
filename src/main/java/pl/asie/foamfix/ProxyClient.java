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
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;
import pl.asie.foamfix.client.*;
import pl.asie.foamfix.shared.FoamFixShared;
import pl.asie.foamfix.util.MethodHandleHelper;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.util.List;

public class ProxyClient extends ProxyCommon {
	public static Deduplicator deduplicator = new Deduplicator();

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

	@Override
	public void preInit() {
		super.preInit();
		MinecraftForge.EVENT_BUS.register(new FoamFixModelDeduplicate());

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
		if (FoamFixShared.config.txFasterAnimation) {
			ContextCapabilities caps = GLContext.getCapabilities();
			boolean copyImageSupported = caps.OpenGL43 || caps.GL_ARB_copy_image;
			if(!copyImageSupported) {
				FoamFix.logger.warn("Fast animated textures require OpenGL 4.3 or ARB_copy_image extension, which were not detected. Using original slow path.");
				FoamFix.shouldFasterAnimation = false;
			} else {
				FoamFix.logger.info("Using fast animated textures.");
				FoamFix.shouldFasterAnimation = true;
			}
		}
	}

	@Override
	public void init() {
		super.init();
		// MinecraftForge.EVENT_BUS.register(PleaseTrustMeLookImADolphin.INSTANCE);

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
