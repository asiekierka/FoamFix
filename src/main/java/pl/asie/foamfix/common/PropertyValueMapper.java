package pl.asie.foamfix.common;

import gnu.trove.impl.Constants;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.MathHelper;

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

		public Entry(IProperty property) {
			this.property = property;
			this.values = new TObjectIntHashMap();
			this.bitSize = MathHelper.roundUpToPowerOfTwo(property.getAllowedValues().size());
			int bits = 0;
			int b = bitSize;
			while (b != 0) {
				bits++;
				b >>= 1;
			}
			this.bits = bits;
			int i = 0;

			for (Object o : property.getAllowedValues()) {
				this.values.put(o, i++);
			}
		}

		public int get(Object v) {
			return values.get(v);
		}

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

	private static final Map<IProperty, Entry> entryMap = new IdentityHashMap<>();
	private static final Map<Object, Entry[]> blockEntryList = new IdentityHashMap<>();
	private static final Map<Object, TObjectIntMap<IProperty>> blockEntryPositionMap = new IdentityHashMap<>();
	private static final Map<Object, IProperty[]> blockPropertyMap = new IdentityHashMap<>();
	private static final Map<Object, IBlockState[]> blockStateMap = new IdentityHashMap<>();

	public static IProperty[] getPropertiesOrdered(Object owner, Collection<IProperty<?>> properties) {
		IProperty[] propertiesOrdered = blockPropertyMap.get(owner);
		if (propertiesOrdered == null) {
			propertiesOrdered = new IProperty[properties.size()];
			int i = 0;
			for (IProperty<?> property : properties) {
				propertiesOrdered[i++] = property;
			}
			blockPropertyMap.put(owner, propertiesOrdered);
		}
		return propertiesOrdered;
	}

	protected static Entry getPropertyEntry(IProperty property) {
		Entry e = entryMap.get(property);
		if (e == null) {
			e = new Entry(property);
			entryMap.put(property, e);
		}
		return e;
	}

	protected static Entry[] getPropertyEntryList(IBlockState state) {
		Object owner = ((IFoamBlockState) state).getStateContainer();
		Entry[] e = blockEntryList.get(owner);
		if (e == null) {
			e = new Entry[state.getPropertyNames().size()];
			List<IProperty<?>> props = new ArrayList<>(state.getPropertyNames());
			Collections.sort(props, COMPARATOR_BIT_FITNESS);
			int i = 0;
			for (IProperty p : props) {
				e[i++] = getPropertyEntry(p);
			}
			blockEntryList.put(owner, e);
		}
		return e;
	}

	protected static TObjectIntMap<IProperty> getPropertyEntryPositionMap(IBlockState state) {
		Object owner = ((IFoamBlockState) state).getStateContainer();
		TObjectIntMap<IProperty> e = blockEntryPositionMap.get(owner);
		if (e == null) {
			Entry[] entries = getPropertyEntryList(state);
			e = new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);
			int bitPos = 0;
			for (Entry ee : entries) {
				e.put(ee.property, bitPos);
				bitPos += ee.bits;
			}
			blockEntryPositionMap.put(owner, e);
		}
		return e;
	}

	public static int generateValue(IBlockState state) {
		Entry[] entries = getPropertyEntryList(state);
		int bitPos = 0;
		int value = 0;
		Entry lastEntry = null;
		for (Entry e : entries) {
			value |= e.get(state.getValue(e.property)) << bitPos;
			bitPos += e.bits;
			lastEntry = e;
		}

		Object owner = ((IFoamBlockState) state).getStateContainer();
		IBlockState[] states = blockStateMap.get(owner);
		if (states == null) {
			if (lastEntry == null) {
				states = new IBlockState[1 << bitPos];
			} else {
				states = new IBlockState[(1 << (bitPos - lastEntry.bits)) * lastEntry.property.getAllowedValues().size()];
			}
			blockStateMap.put(owner, states);
		}
		states[value] = state;

		return value;
	}

	public static <T extends Comparable<T>, V extends T> IBlockState withProperty(IBlockState state, int value, IProperty<T> property, V propertyValue) {
		Entry e = getPropertyEntry(property);
		if (e != null) {
			int bitPos = getPropertyEntryPositionMap(state).get(property);
			if (bitPos >= 0) {
				value &= ~((e.bitSize - 1) << bitPos);
				value |= e.get(propertyValue) << bitPos;

				Object owner = ((IFoamBlockState) state).getStateContainer();
				return blockStateMap.get(owner)[value];
			}
		}

		return null;
	}

	public static IBlockState getPropertyByValue(IBlockState state, int value) {
		Object owner = ((IFoamBlockState) state).getStateContainer();
		return blockStateMap.get(owner)[value];
	}

	public static <T extends Comparable<T>, V extends T> int withPropertyValue(IBlockState state, int value, IProperty<T> property, V propertyValue) {
		Entry e = getPropertyEntry(property);
		if (e != null) {
			int bitPos = getPropertyEntryPositionMap(state).get(property);
			if (bitPos >= 0) {
				value &= ~((e.bitSize - 1) << bitPos);
				value |= e.get(propertyValue) << bitPos;

				return value;
			}
		}

		return value;
	}
}
