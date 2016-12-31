package pl.asie.foamfix.common;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import pl.asie.foamfix.common.FoamyExtendedBlockState;

public class FoamyExtendedBlockStateContainer extends ExtendedBlockState {
	public FoamyExtendedBlockStateContainer(Block blockIn, IProperty<?>[] properties, IUnlistedProperty<?>[] unlistedProperties) {
		super(blockIn, properties, unlistedProperties);
	}

	@Override
	protected BlockStateContainer.StateImplementation createState(Block block, ImmutableMap<IProperty<?>, Comparable<?>> properties, ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties)
	{
		if (unlistedProperties == null || unlistedProperties.isEmpty()) {
			return new FoamyBlockState(this, block, properties);
		} else {
			return new FoamyExtendedBlockState(this, block, properties, unlistedProperties);
		}
	}
}
