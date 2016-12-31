package pl.asie.foamfix.common;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraftforge.common.property.IUnlistedProperty;
import pl.asie.foamfix.api.IFoamFixHelper;

public class FoamFixHelper implements IFoamFixHelper {
	@Override
	public BlockStateContainer createBlockState(Block block, IProperty<?>... properties) {
		return new FoamyBlockStateContainer(block, properties);
	}

	@Override
	public BlockStateContainer createExtendedBlockState(Block block, IProperty<?>[] properties, IUnlistedProperty<?>[] unlistedProperties) {
		return new FoamyExtendedBlockStateContainer(block, properties, unlistedProperties);
	}
}
