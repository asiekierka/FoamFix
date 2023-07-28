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
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import java.util.Optional;

public class FoamyExtendedBlockStateContainer extends ExtendedBlockState {
	private PropertyValueMapper foamfix_mapper;

	public FoamyExtendedBlockStateContainer(Block blockIn, IProperty<?>[] properties, IUnlistedProperty<?>[] unlistedProperties) {
		super(blockIn, properties, unlistedProperties);
	}

	@Override
	protected BlockStateContainer.StateImplementation createState(Block block, ImmutableMap<IProperty<?>, Comparable<?>> properties, ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties)
	{
		if (block == null || block.getClass().getName().startsWith("jds.bibliocraft")) {
			return createState_foamfix_old(block, properties, unlistedProperties);
		}

		if (foamfix_mapper == null) {
			foamfix_mapper = new PropertyValueMapper(this);
		}

		if (foamfix_mapper.isValid()) {
			if (unlistedProperties == null || unlistedProperties.isEmpty()) {
				return new FoamyBlockState(foamfix_mapper, block, properties);
			} else {
				boolean has = false;
				for (Optional o : unlistedProperties.values()) {
					if (o.isPresent()) {
						has = true;
						break;
					}
				}

				return new FoamyExtendedBlockState(foamfix_mapper, block, properties, unlistedProperties, has);
			}
		} else {
			return createState_foamfix_old(block, properties, unlistedProperties);
		}
	}

	protected BlockStateContainer.StateImplementation createState_foamfix_old(Block block, ImmutableMap<IProperty<?>, Comparable<?>> properties, ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties) {
		return null;
	}
}
