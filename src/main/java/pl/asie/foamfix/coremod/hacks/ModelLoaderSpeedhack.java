package pl.asie.foamfix.coremod.hacks;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelBlockDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.VariantList;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import pl.asie.foamfix.FoamFix;

/**
 * This is a speedhack and intended for personal use in development
 * environments as a last-ditch effort to speed up the game's
 * loading times.
 */
public class ModelLoaderSpeedhack extends ModelBakery {
	public ModelLoaderSpeedhack(IResourceManager resourceManagerIn, TextureMap textureMapIn, BlockModelShapes blockModelShapesIn) {
		super(resourceManagerIn, textureMapIn, blockModelShapesIn);
	}

	@Override
	protected void loadBlock(BlockStateMapper blockstatemapper, Block block, final ResourceLocation resourcelocation) {
		if (FoamFix.stage >= 1) {
			super.loadBlock(blockstatemapper, block, resourcelocation);
		}
	}
}
