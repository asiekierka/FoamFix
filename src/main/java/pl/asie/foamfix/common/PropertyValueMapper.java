package pl.asie.foamfix.common;

import gnu.trove.impl.Constants;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.MathHelper;
import pl.asie.foamfix.FoamFix;

import java.util.*;

public class PropertyValueMapper {
	private static final Comparator<? super IProperty<?>> COMPARATOR_BIT_FITNESS = new Comparator<IProperty<?>>() {
		@Override
		public int compare(IProperty<?> first, IProperty<?> second) {
			int diff1 = getPropertyEntry(first).bitSize - first.getAllowedValues().size();
			int diff2 = getPropertyEntry(second).bitSize - second.getAllowedValues().size();
			// We want to put properties with higher diff-values last,
			// so that the array is as small as possible.
			return diff1 - diff2;
		}
	};

	public static class Entry {
		private final IProperty property;
		private final TObjectIntMap values;
		private final int bitSize;
		private final int bits;

		private Entry(IProperty property) {
			this.property = property;
			this.values = new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);
			this.bitSize = MathHelper.smallestEncompassingPowerOfTwo(property.getAllowedValues().size());
			int bits = 0;

			// Think: 8 values = 3 bits required, 9 values = 4 bits required
			// we want the location of the highest set bit to match this
			int b = bitSize - 1;
			while (b != 0) {
				bits++;
				b >>= 1;
			}
			this.bits = bits-1;
			int i = 0;

			for (Object o : property.getAllowedValues()) {
				this.values.put(o, i++);
			}
		}

		public int get(Object v) {
			return values.get(v);
		}

		/* @Override
		public boolean equals(Object other) {
			if (!(other instanceof Entry))
				return false;

			return ((Entry) other).property.equals(property);
		}

		@Override
		public int hashCode() {
			return property.hashCode();
		} */
	}

	private static final Map<IProperty, Entry> entryMap = new IdentityHashMap<>();
	private static final Map<BlockStateContainer, PropertyValueMapper> mapperMap = new IdentityHashMap<>();

	private final Entry[] entryList;
	private final TObjectIntMap<IProperty> entryPositionMap;
	private final IBlockState[] stateMap;

	public PropertyValueMapper(BlockStateContainer container) {
		Collection<IProperty<?>> properties = container.getProperties();

		entryList = new Entry[properties.size()];
		List<IProperty<?>> propertiesSortedFitness = new ArrayList<>(properties);
		Collections.sort(propertiesSortedFitness, COMPARATOR_BIT_FITNESS);
		int i = 0;
		for (IProperty p : propertiesSortedFitness) {
			entryList[i++] = getPropertyEntry(p);
		}

		entryPositionMap = new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);
		int bitPos = 0;
		Entry lastEntry = null;
		for (Entry ee : entryList) {
			entryPositionMap.put(ee.property, bitPos);
			bitPos += ee.bits;
			lastEntry = ee;
		}

		if (lastEntry == null) {
			stateMap = new IBlockState[1 << bitPos];
		} else {
			stateMap = new IBlockState[(1 << (bitPos - lastEntry.bits)) * lastEntry.property.getAllowedValues().size()];
		}
	}

	public static PropertyValueMapper getOrCreate(BlockStateContainer owner) {
		PropertyValueMapper e = mapperMap.get(owner);
		if (e == null) {
			e = new PropertyValueMapper(owner);
			mapperMap.put(owner, e);
		}
		return e;
	}

	protected static Entry getPropertyEntry(IProperty property) {
		Entry e = entryMap.get(property);
		if (e == null) {
			e = new Entry(property);
			entryMap.put(property, e);
		}
		return e;
	}

	public int generateValue(IBlockState state) {
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
		int bitPos = entryPositionMap.get(property);
		if (bitPos >= 0) {
			Entry e = getPropertyEntry(property);
			if (e != null) {
				int nv = e.get(propertyValue);
				if (nv < 0) return null;

				value &= ~((e.bitSize - 1) << bitPos);
				value |= nv << bitPos;
				return stateMap[value];
			}
		}

		return null;
	}

	public IBlockState getPropertyByValue(int value) {
		return stateMap[value];
	}

	public <T extends Comparable<T>, V extends T> int withPropertyValue(int value, IProperty<T> property, V propertyValue) {
		int bitPos = entryPositionMap.get(property);
		if (bitPos >= 0) {
			Entry e = getPropertyEntry(property);
			if (e != null) {
				int bitMask = (e.bitSize - 1);
				int nv = e.get(propertyValue);
				if (nv < 0) return -1;

				value &= ~(bitMask << bitPos);
				value |= nv << bitPos;

				return value;
			}
		}

		return -1;
	}
}
