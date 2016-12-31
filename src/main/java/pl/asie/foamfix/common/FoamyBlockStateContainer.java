package pl.asie.foamfix.common;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import pl.asie.foamfix.common.FoamyBlockState;

public class FoamyBlockStateContainer extends BlockStateContainer {
	public FoamyBlockStateContainer(Block blockIn, IProperty<?>... properties) {
		super(blockIn, properties);
	}

	@Override
	protected BlockStateContainer.StateImplementation createState(Block block, ImmutableMap<IProperty<?>, Comparable<?>> properties, ImmutableMap<net.minecraftforge.common.property.IUnlistedProperty<?>, com.google.common.base.Optional<?>> unlistedProperties) {
		return new FoamyBlockState(PropertyValueMapper.getOrCreate(this), block, properties);
	}
}
