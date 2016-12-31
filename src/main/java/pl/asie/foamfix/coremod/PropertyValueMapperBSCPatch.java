package pl.asie.foamfix.coremod;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;

public class PropertyValueMapperBSCPatch {
	protected BlockStateContainer.StateImplementation createState(Block block, ImmutableMap<IProperty<?>, Comparable<?>> properties, ImmutableMap<net.minecraftforge.common.property.IUnlistedProperty<?>, com.google.common.base.Optional<?>> unlistedProperties) {
		return new StateImplementation(this, block, properties);
	}
}
