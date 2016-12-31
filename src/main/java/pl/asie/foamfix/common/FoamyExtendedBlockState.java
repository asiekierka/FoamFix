package pl.asie.foamfix.common;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by asie on 12/31/16.
 */
public class FoamyExtendedBlockState extends FoamyBlockState implements IExtendedBlockState {
	private final ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties;

	public FoamyExtendedBlockState(Object owner, Block block, ImmutableMap<IProperty<?>, Comparable<?>> properties, ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties) {
		super(owner, block, properties);
		this.unlistedProperties = unlistedProperties;
	}

	public FoamyExtendedBlockState(Object owner, Block block, ImmutableMap<IProperty<?>, Comparable<?>> properties, ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties, int value) {
		super(owner, block, properties);
		this.unlistedProperties = unlistedProperties;
		this.value = value;
	}


	@Override
	public <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V propertyValue)
	{
		if (!this.getProperties().containsKey(property))
		{
			throw new IllegalArgumentException("Cannot set property " + property + " as it does not exist in " + getBlock().getBlockState());
		}
		else
		{
			if (!property.getAllowedValues().contains(propertyValue))
			{
				throw new IllegalArgumentException("Cannot set property " + property + " to " + value + " on block " + Block.REGISTRY.getNameForObject(getBlock()) + ", it is not an allowed value");
			} else
			{
				if (this.getProperties().get(property) == propertyValue)
				{
					return this;
				}
				int newValue = PropertyValueMapper.withPropertyValue(this, value, property, propertyValue);
				IBlockState state = PropertyValueMapper.getPropertyByValue(this, value);
				if (Iterables.all(unlistedProperties.values(), Predicates.<Optional<?>>equalTo(Optional.absent()))) {
					return state;
				}
				return new FoamyExtendedBlockState(getFoamyOwner(), getBlock(), state.getProperties(), unlistedProperties, newValue);
			}
		}
	}

	@Override
	public <V> IExtendedBlockState withProperty(IUnlistedProperty<V> property, V value)
	{
		if(!this.unlistedProperties.containsKey(property))
		{
			throw new IllegalArgumentException("Cannot set unlisted property " + property + " as it does not exist in " + getBlock().getBlockState());
		}
		if(!property.isValid(value))
		{
			throw new IllegalArgumentException("Cannot set unlisted property " + property + " to " + value + " on block " + Block.REGISTRY.getNameForObject(getBlock()) + ", it is not an allowed value");
		}
		Map<IUnlistedProperty<?>, Optional<?>> newMap = new HashMap<IUnlistedProperty<?>, Optional<?>>(unlistedProperties);
		newMap.put(property, Optional.fromNullable(value));
		if(Iterables.all(newMap.values(), Predicates.<Optional<?>>equalTo(Optional.absent())))
		{ // no dynamic properties, lookup normal state
			return (IExtendedBlockState) PropertyValueMapper.getPropertyByValue(this, this.value);
		}
		return new FoamyExtendedBlockState(getFoamyOwner(), getBlock(), getProperties(), ImmutableMap.copyOf(newMap), this.value);
	}

	@Override
	public Collection<IUnlistedProperty<?>> getUnlistedNames()
	{
		return Collections.unmodifiableCollection(unlistedProperties.keySet());
	}

	@Override
	public <V>V getValue(IUnlistedProperty<V> property)
	{
		if(!this.unlistedProperties.containsKey(property))
		{
			throw new IllegalArgumentException("Cannot get unlisted property " + property + " as it does not exist in " + getBlock().getBlockState());
		}
		return property.getType().cast(this.unlistedProperties.get(property).orNull());
	}

	@Override
	public ImmutableMap<IUnlistedProperty<?>, Optional<?>> getUnlistedProperties()
	{
		return unlistedProperties;
	}

	@Override
	public IBlockState getClean() {
		return PropertyValueMapper.getPropertyByValue(this, this.value);
	}
}
