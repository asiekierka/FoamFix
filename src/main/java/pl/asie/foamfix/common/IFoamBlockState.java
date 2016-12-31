package pl.asie.foamfix.common;

import net.minecraft.block.state.IBlockState;

public interface IFoamBlockState extends IBlockState {
	Object getStateContainer();
}
