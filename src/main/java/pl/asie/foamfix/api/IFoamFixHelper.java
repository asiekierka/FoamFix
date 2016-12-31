package pl.asie.foamfix.api;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public interface IFoamFixHelper {
	class Default implements IFoamFixHelper {
		@Override
		public BlockStateContainer createBlockState(Block block, IProperty<?>... properties) {
			return new BlockStateContainer(block, properties);
		}

		@Override
		public BlockStateContainer createExtendedBlockState(Block block, IProperty<?>[] properties, IUnlistedProperty<?>[] unlistedProperties) {
			return new ExtendedBlockState(block, properties, unlistedProperties);
		}
	}

	BlockStateContainer createBlockState(Block block, IProperty<?>... properties);
	BlockStateContainer createExtendedBlockState(Block block, IProperty<?>[] properties, IUnlistedProperty<?>[] unlistedProperties);
}
