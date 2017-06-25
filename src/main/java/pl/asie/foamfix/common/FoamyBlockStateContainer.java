package pl.asie.foamfix.common;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraftforge.common.property.IUnlistedProperty;
import pl.asie.foamfix.common.FoamyBlockState;

import java.util.Optional;

public class FoamyBlockStateContainer extends BlockStateContainer {
	public FoamyBlockStateContainer(Block blockIn, IProperty<?>... properties) {
		super(blockIn, properties);
	}

	@Override
	protected BlockStateContainer.StateImplementation createState(Block block, ImmutableMap<IProperty<?>, Comparable<?>> properties, ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties) {
		return new FoamyBlockState(PropertyValueMapper.getOrCreate(this), block, properties);
	}
}
