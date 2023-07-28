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
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

import java.util.Map;

public class FoamyBlockState extends BlockStateContainer.StateImplementation {
	protected final PropertyValueMapper owner;
	protected int value;

	public FoamyBlockState(PropertyValueMapper owner, Block blockIn, ImmutableMap < IProperty<?>, Comparable<? >> propertiesIn) {
		super(blockIn, propertiesIn);
		this.owner = owner;
	}

	@Override
	public <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V value) {
		IBlockState state = owner.withProperty(this.value, property, value);

		if (state == null) {
			Comparable<?> comparable = this.properties.get(property);
			if (comparable == null) {
				throw new IllegalArgumentException("Cannot set property " + property + " as it does not exist in " + this.getBlock().getBlockState());
			} else {
				throw new IllegalArgumentException("Cannot set property " + property + " to " + value + " on block " + Block.REGISTRY.getNameForObject(this.getBlock()) + ", it is not an allowed value");
			}
		} else {
			return state;
		}
	}

	@Override
	public void buildPropertyValueTable(Map <Map< IProperty<?>, Comparable<? >>, BlockStateContainer.StateImplementation > map) {
		this.value = owner.generateValue(this);
	}
}
