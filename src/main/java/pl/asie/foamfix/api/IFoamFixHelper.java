package pl.asie.foamfix.api;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public interface IFoamFixHelper {
	class Default implements IFoamFixHelper {
		@Override
		public BlockState createBlockState(Block block, IProperty<?>... properties) {
			return new BlockState(block, properties);
		}

		@Override
		public BlockState createExtendedBlockState(Block block, IProperty<?>[] properties, IUnlistedProperty<?>[] unlistedProperties) {
			return new ExtendedBlockState(block, properties, unlistedProperties);
		}
	}

	BlockState createBlockState(Block block, IProperty<?>... properties);
	BlockState createExtendedBlockState(Block block, IProperty<?>[] properties, IUnlistedProperty<?>[] unlistedProperties);
}
