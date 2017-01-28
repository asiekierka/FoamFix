package pl.asie.foamfix.common;

import com.google.common.collect.*;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

import java.util.Map;

public class FoamyBlockState extends BlockStateContainer.StateImplementation {
	protected final PropertyValueMapper owner;
	protected final ImmutableMap<IProperty<?>, Comparable<?>> properties;
	protected int value;

	public FoamyBlockState(PropertyValueMapper owner, Block blockIn, ImmutableMap < IProperty<?>, Comparable<? >> propertiesIn) {
		super(blockIn, propertiesIn);
		this.owner = owner;
		this.properties = propertiesIn;
	}

	@Override
	public <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V value) {
		Comparable<?> comparable = this.properties.get(property);

		if (comparable == null) {
			throw new IllegalArgumentException("Cannot set property " + property + " as it does not exist in " + this.getBlock().getBlockState());
		} else if (comparable == value) {
			return this;
		} else {
			IBlockState state = owner.withProperty(this.value, property, value);

			if (state == null) {
				throw new IllegalArgumentException("Cannot set property " + property + " to " + value + " on block " + Block.REGISTRY.getNameForObject(this.getBlock()) + ", it is not an allowed value");
			} else {
				return state;
			}
		}
	}

	@Override
	public void buildPropertyValueTable(Map <Map< IProperty<?>, Comparable<? >>, BlockStateContainer.StateImplementation > map) {
		this.value = owner.generateValue(this);
	}
}
