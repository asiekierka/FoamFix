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
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import pl.asie.foamfix.client.Deduplicator;
import pl.asie.foamfix.client.FoamFixChunkProviderClient;
import pl.asie.foamfix.client.FoamFixModelDeduplicate;
import pl.asie.foamfix.client.FoamFixModelRegistryDuplicateWipe;
import pl.asie.foamfix.shared.FoamFixShared;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by asie on 12/7/16.
 */
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
	}

	@Override
	public void init() {
		super.init();
		MinecraftForge.EVENT_BUS.register(new FoamFixModelDeduplicate());
		if (FoamFixShared.config.clCleanRedundantModelRegistry) {
			MinecraftForge.EVENT_BUS.register(new FoamFixModelRegistryDuplicateWipe());
		}
		if (FoamFixShared.config.geImprovedChunkProvider) {
			MinecraftForge.EVENT_BUS.register(new FoamFixChunkProviderClient.Registrar());
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
