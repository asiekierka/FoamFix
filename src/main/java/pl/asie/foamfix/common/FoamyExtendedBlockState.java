/**
 * Copyright (C) 2016, 2017, 2018, 2019, 2020, 2021 Adrian Siekierka
 *
 * This file is part of FoamFix.
 *
 * FoamFix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoamFix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FoamFix.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.asie.foamfix.common;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Created by asie on 12/31/16.
 */
public class FoamyExtendedBlockState extends FoamyBlockState implements IExtendedBlockState {
	private final ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties;
	private final boolean hasUnlistedProperty;

	public FoamyExtendedBlockState(PropertyValueMapper owner, Block block, ImmutableMap<IProperty<?>, Comparable<?>> properties, ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties, boolean hasUnlistedProperty) {
		super(owner, block, properties);
		this.unlistedProperties = unlistedProperties;
		this.hasUnlistedProperty = hasUnlistedProperty;
	}

	public FoamyExtendedBlockState(PropertyValueMapper owner, Block block, ImmutableMap<IProperty<?>, Comparable<?>> properties, ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties, boolean hasUnlistedProperty, int value) {
		super(owner, block, properties);
		this.unlistedProperties = unlistedProperties;
		this.hasUnlistedProperty = hasUnlistedProperty;
		this.value = value;
	}

	@Override
	public <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V propertyValue) {
		int newValue = owner.withPropertyValue(value, property, propertyValue);
		if (newValue == value) {
			return this;
		} else if (newValue < 0) {
			if (!this.getProperties().containsKey(property)) {
				throw new IllegalArgumentException("Cannot set property " + property + " as it does not exist in " + getBlock().getBlockState());
			} else if (!property.getAllowedValues().contains(propertyValue)) {
				throw new IllegalArgumentException("Cannot set property " + property + " to " + value + " on block " + Block.REGISTRY.getNameForObject(getBlock()) + ", it is not an allowed value");
			} else {
				throw new IllegalArgumentException("Cannot set property " + property + " because FoamFix could not find a mapping for it! Please reproduce without FoamFix first!");
			}
		}

		IBlockState state = owner.getPropertyByValue(newValue);
		if (state == null) {
			throw new IllegalArgumentException("Incomplete? list of values when trying to set property " + property + "! Please reproduce without FoamFix first! (Info: " + getBlock().getRegistryName() + " " + value + " -> " + newValue + ")");
		}

		if (hasUnlistedProperty) {
			return new FoamyExtendedBlockState(owner, getBlock(), state.getProperties(), unlistedProperties, true, newValue);
		} else {
			return state;
		}
	}

	@Override
	public <V> IExtendedBlockState withProperty(IUnlistedProperty<V> property, V value) {
		if (!property.isValid(value)) {
			throw new IllegalArgumentException("Cannot set unlisted property " + property + " to " + value + " on block " + Block.REGISTRY.getNameForObject(getBlock()) + ", it is not an allowed value");
		}

		boolean hasOpt = false;
		boolean setValue = false;

		ImmutableMap.Builder<IUnlistedProperty<?>, Optional<?>> newMap = new ImmutableMap.Builder<>();
		for (Map.Entry<IUnlistedProperty<?>, Optional<?>> entry : unlistedProperties.entrySet()) {
			IUnlistedProperty<?> entryKey = entry.getKey();
			if (!setValue && entryKey.equals(property)) {
				newMap.put(entryKey, Optional.ofNullable(value));
				setValue = true;
			} else {
				Optional<?> entryValue = entry.getValue();
				newMap.put(entryKey, entryValue);
				hasOpt |= entryValue.isPresent();
			}
		}

		if (!setValue) {
			throw new IllegalArgumentException("Cannot set unlisted property " + property + " as it does not exist in " + getBlock().getBlockState());
		}

		if (value != null || hasOpt) {
			return new FoamyExtendedBlockState(owner, getBlock(), getProperties(), newMap.build(), true, this.value);
		} else {
			return (IExtendedBlockState) owner.getPropertyByValue(this.value);
		}
	}

	@Override
	public Collection<IUnlistedProperty<?>> getUnlistedNames() {
		return unlistedProperties.keySet();
	}

	@Override
	public <V> V getValue(IUnlistedProperty<V> property) {
		Optional optional = this.unlistedProperties.get(property);

		if (optional == null) {
			throw new IllegalArgumentException("Cannot get unlisted property " + property + " as it does not exist in " + getBlock().getBlockState());
		}

		return property.getType().cast(optional.orElse(null));
	}

	@Override
	public ImmutableMap<IUnlistedProperty<?>, Optional<?>> getUnlistedProperties() {
		return unlistedProperties;
	}

	@Override
	public IBlockState getClean() {
		return owner.getPropertyByValue(this.value);
	}
}
