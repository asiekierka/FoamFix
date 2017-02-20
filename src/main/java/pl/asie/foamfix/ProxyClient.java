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
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.ResourceIndex;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import pl.asie.foamfix.client.Deduplicator;
import pl.asie.foamfix.client.FoamFixDynamicItemModels;
import pl.asie.foamfix.client.FoamFixModelDeduplicate;
import pl.asie.foamfix.client.FoamFixModelRegistryDuplicateWipe;
import pl.asie.foamfix.client.FoamyDefaultResourcePack;
import pl.asie.foamfix.shared.FoamFixShared;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
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
		public ItemCameraTransforms getItemCameraTransforms() {
			return ItemCameraTransforms.DEFAULT;
		}

		@Override
		public ItemOverrideList getOverrides() {
			return itemOverrideList;
		}
	};

	@Override
	public void preInit() {
		super.preInit();

		if (!FoamFixShared.config.clDeduplicate) {
			deduplicator = null;
		}

		if (FoamFixShared.config.clDynamicItemModels) {
			FoamFixDynamicItemModels.register();
		}

		if (FoamFixShared.config.clFasterResourceLoading && !FoamFixShared.coremodEnabled) {
			try {
				Field f1 = ReflectionHelper.findField(Minecraft.class, "mcDefaultResourcePack", "field_110450_ap");
				Field f2 = ReflectionHelper.findField(Minecraft.class, "defaultResourcePacks", "field_110449_ao");
				Field f3 = ReflectionHelper.findField(DefaultResourcePack.class, "resourceIndex", "field_188549_b");
				Object o = f1.get(Minecraft.getMinecraft());
				List l = (List) f2.get(Minecraft.getMinecraft());
				int i = l.indexOf(o);
				o = new FoamyDefaultResourcePack((ResourceIndex) f3.get(o));
				l.set(i, o);
				f1.set(Minecraft.getMinecraft(), o);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void init() {
		super.init();
		MinecraftForge.EVENT_BUS.register(new FoamFixModelDeduplicate());

		if (FoamFixShared.config.clCleanRedundantModelRegistry) {
			MinecraftForge.EVENT_BUS.register(new FoamFixModelRegistryDuplicateWipe());
		}
	}

	@Override
	public void postInit() {
		super.postInit();
		// clear successful deduplication count - the coremod variant
		// deduplicates in init's modelbake as well, so we don't want
		// to count that in to make the numbers (more or less) match
		if (deduplicator != null) {
			deduplicator.successfuls = 0;
		}
	}
}
