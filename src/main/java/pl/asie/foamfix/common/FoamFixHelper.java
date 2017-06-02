package pl.asie.foamfix.common;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import pl.asie.foamfix.api.IFoamFixHelper;

public class FoamFixHelper implements IFoamFixHelper {
	@Override
	public BlockState createBlockState(Block block, IProperty... properties) {
		return new FoamyBlockStateContainer(block, properties);
	}

	@Override
	public BlockState createExtendedBlockState(Block block, IProperty[] properties, IUnlistedProperty[] unlistedProperties) {
		return new FoamyExtendedBlockStateContainer(block, properties, unlistedProperties);
	}
}
