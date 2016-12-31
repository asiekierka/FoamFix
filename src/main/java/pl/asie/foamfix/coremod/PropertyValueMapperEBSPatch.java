package pl.asie.foamfix.coremod;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class PropertyValueMapperEBSPatch extends BlockStateContainer {
	public PropertyValueMapperEBSPatch(Block blockIn, IProperty<?>... properties) {
		super(blockIn, properties);
	}

	protected BlockStateContainer.StateImplementation createState(Block block, ImmutableMap<IProperty<?>, Comparable<?>> properties, ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties)
	{
		if (unlistedProperties == null || unlistedProperties.isEmpty()) return super.createState(block, properties, unlistedProperties);
		return new ExtendedStateImplementation(this, block, properties, unlistedProperties);
	}
}
