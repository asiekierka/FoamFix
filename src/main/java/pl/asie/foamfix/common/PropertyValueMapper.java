/*
 * Copyright (C) 2016, 2017, 2018, 2019, 2020, 2021 Adrian Siekierka
 *
 * This file is part of FoamFix.
 *
 * FoamFix is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoamFix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FoamFix.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or
 * combining it with the Minecraft game engine, the Mojang Launchwrapper,
 * the Mojang AuthLib and the Minecraft Realms library (and/or modified
 * versions of said software), containing parts covered by the terms of
 * their respective licenses, the licensors of this Program grant you
 * additional permission to convey the resulting work.
 */

package pl.asie.foamfix.common;

import com.google.common.collect.Lists;
import gnu.trove.impl.Constants;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.MathHelper;
import pl.asie.foamfix.util.HashingStrategies;

import java.util.*;

public class PropertyValueMapper {
	private static final Comparator<? super IProperty<?>> COMPARATOR_BIT_FITNESS = (Comparator<IProperty<?>>) (first, second) -> {
		int diff1 = getPropertyEntry(first).bitSize - first.getAllowedValues().size();
		int diff2 = getPropertyEntry(second).bitSize - second.getAllowedValues().size();
		// We want to put properties with higher diff-values last,
		// so that the array is as small as possible.
		if (diff1 == diff2) {
			return first.getName().compareTo(second.getName());
		} else {
			return diff1 - diff2;
		}
	};

	public static abstract class Entry {
		private final IProperty property;
		private final int bitSize;
		private final int bits;

		private Entry(IProperty property) {
			this.property = property;

			this.bitSize = MathHelper.smallestEncompassingPowerOfTwo(property.getAllowedValues().size());
			int bits = 0;

			int b = bitSize - 1;
			while (b != 0) {
				bits++;
				b >>= 1;
			}
			this.bits = bits;

			/* List<Object> allowedValues = Lists.newArrayList(property.getAllowedValues());
			Collections.sort(allowedValues, (o1, o2) -> {
				if (o1 == null || o2 == null) {
					if (o1 == o2) {
						return 0;
					} else if (o1 == null) {
						return Integer.MIN_VALUE;
					} else {
						return Integer.MAX_VALUE;
					}
				} else {
					return o1.hashCode() - o2.hashCode();
				}
			}); */
		}

		public abstract int get(Object v);

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Entry))
				return false;

			return ((Entry) other).property.equals(property);
		}

		@Override
		public int hashCode() {
			return property.hashCode();
		}
	}

	public static class BooleanEntry extends Entry {
		private BooleanEntry(IProperty property) {
			super(property);
		}

		@Override
		public int get(Object v) {
			return v == Boolean.TRUE ? 1 : 0;
		}
	}

	public static class ObjectEntry extends Entry {
		private Object2IntMap values;

		private ObjectEntry(IProperty property, boolean identity) {
			super(property);

			//noinspection unchecked
			this.values = identity ? new Object2IntOpenCustomHashMap(HashingStrategies.FASTUTIL_IDENTITY) : new Object2IntOpenHashMap();
			this.values.defaultReturnValue(-1);
			//noinspection unchecked
			Collection<Object> allowedValues = property.getAllowedValues();

			int i = 0;
			for (Object o : allowedValues) {
				this.values.put(o, i++);
			}
		}

		@Override
		public int get(Object v) {
			return values.getInt(v);
		}
	}

	public static class EnumEntrySorted extends Entry {
		private EnumEntrySorted(IProperty property, int count) {
			super(property);
		}

		@Override
		public int get(Object v) {
			return ((Enum) v).ordinal();
		}

		public static Entry create(PropertyEnum entry) {
			Object[] values = entry.getValueClass().getEnumConstants();

			if (entry.getAllowedValues().size() == values.length) {
				return new EnumEntrySorted(entry, values.length);
			} else {
				return new ObjectEntry(entry, true);
			}
		}
	}

	public static class IntegerEntrySorted extends Entry {
		private final int minValue, count;

		private IntegerEntrySorted(IProperty property, int minValue, int count) {
			super(property);

			this.minValue = minValue;
			this.count = count;
		}

		@Override
		public int get(Object v) {
			int vv = ((int) v) - minValue;
			// if vv < 0, it will be rejected anyway
			return vv < count ? vv : -1;
		}
	}

	public static class IntegerEntry extends Entry {
		private Int2IntMap values;

		private IntegerEntry(IProperty property) {
			super(property);

			this.values = new Int2IntOpenHashMap();
			this.values.defaultReturnValue(-1);
			Collection<Object> allowedValues = property.getAllowedValues();

			int i = 0;
			for (Object o : allowedValues) {
				this.values.put((int) o, i++);
			}
		}

		@Override
		public int get(Object v) {
			return values.get((int) v);
		}

		public static Entry create(PropertyInteger entry) {
			List<Integer> sorted = Lists.newArrayList(entry.getAllowedValues());
			sorted.sort(Comparator.naturalOrder());

			int min = sorted.get(0);
			for (int i = 1; i < sorted.size(); i++) {
				if ((sorted.get(i) - sorted.get(i - 1)) != 1) {
					return new IntegerEntry(entry);
				}
			}

			return new IntegerEntrySorted(entry, min, sorted.size());
		}
	}

	private static final Map<IProperty<?>, Entry> entryMap = new IdentityHashMap<>();
	private static final Map<BlockStateContainer, PropertyValueMapper> mapperMap = new IdentityHashMap<>();
	private static final int MAX_BIT_POS = 31;

	private final Entry[] entryList;
	private final TObjectIntMap<String> entryPositionMap;
	private final IBlockState[] stateMap;

	public PropertyValueMapper(BlockStateContainer container) {
		Collection<IProperty<?>> properties = container.getProperties();

		entryList = new Entry[properties.size()];
		List<IProperty<?>> propertiesSortedFitness = Lists.newArrayList(properties);
		propertiesSortedFitness.sort(COMPARATOR_BIT_FITNESS);
		int i = 0;
		for (IProperty<?> p : propertiesSortedFitness) {
			entryList[i++] = getPropertyEntry(p);
		}

//		entryPositionMap = new Object2IntOpenHashMap<>();
//		entryPositionMap.defaultReturnValue(-1);
		entryPositionMap = new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);

		int bitPos = 0;
		Entry lastEntry = null;
		for (Entry ee : entryList) {
			entryPositionMap.put(ee.property.getName(), bitPos);
			bitPos += ee.bits;
			lastEntry = ee;
		}

		if (bitPos <= MAX_BIT_POS) {
			if (lastEntry == null) {
				stateMap = new IBlockState[1 << bitPos];
			} else {
				stateMap = new IBlockState[(1 << (bitPos - lastEntry.bits)) * lastEntry.property.getAllowedValues().size()];
			}
		} else {
			stateMap = null;
		}
	}

	public boolean isValid() {
		return stateMap != null;
	}

	public static PropertyValueMapper getOrCreate(BlockStateContainer owner) {
		synchronized (mapperMap) {
			return mapperMap.computeIfAbsent(owner, PropertyValueMapper::new);
		}
	}

	protected static Entry getPropertyEntry(IProperty property) {
		Entry e = entryMap.get(property);
		if (e == null) {
			Class<?> propertyClass = property.getClass();
			if (propertyClass == PropertyInteger.class) {
				e = IntegerEntry.create((PropertyInteger) property);
			} else if (propertyClass == PropertyBool.class && property.getAllowedValues().size() == 2) {
				e = new BooleanEntry(property);
			} else if (propertyClass == PropertyEnum.class || propertyClass == PropertyDirection.class) {
				e = EnumEntrySorted.create((PropertyEnum) property);
			} else {
				e = new ObjectEntry(property, false);
			}
			entryMap.put(property, e);
		}
		return e;
	}

	protected int generateValue(IBlockState state) {
		int bitPos = 0;
		int value = 0;
		for (Entry e : entryList) {
			value |= e.get(state.getValue(e.property)) << bitPos;
			bitPos += e.bits;
		}

		stateMap[value] = state;
		return value;
	}

	public <T extends Comparable<T>, V extends T> IBlockState withProperty(int value, IProperty<T> property, V propertyValue) {
		int bitPos = entryPositionMap.get(property.getName());
		if (bitPos >= 0) {
			Entry e = getPropertyEntry(property);
			int nv = e.get(propertyValue);
			if (nv < 0) return null;

			int bitMask = (e.bitSize - 1);
			value = (value & (~(bitMask << bitPos)) | (nv << bitPos));

			return stateMap[value];
		}

		return null;
	}

	public IBlockState getPropertyByValue(int value) {
		return stateMap[value];
	}

	public <T extends Comparable<T>, V extends T> int withPropertyValue(int value, IProperty<T> property, V propertyValue) {
		int bitPos = entryPositionMap.get(property.getName());
		if (bitPos >= 0) {
			Entry e = getPropertyEntry(property);
			int nv = e.get(propertyValue);
			if (nv < 0) return -1;

			int bitMask = (e.bitSize - 1);
			value = (value & (~(bitMask << bitPos)) | (nv << bitPos));

			return value;
		}

		return -1;
	}

}
