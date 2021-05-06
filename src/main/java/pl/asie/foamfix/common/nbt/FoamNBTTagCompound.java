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

package pl.asie.foamfix.common.nbt;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class FoamNBTTagCompound implements IFoamFixNBTTagCompound {
	private static final Object[] EMPTY = new Object[0];
	/**
	 * odd fields are keys, even values
	 */
	Object[] data = EMPTY;
	int size, mask;
	private final Map<String, NBTBase> tagMap = new FoamNBTTagCompoundMap(this);

	public FoamNBTTagCompound() {
		size = 0;
		mask = 0;
	}

	// internal methods

	private NBTBase ffPutAfterRehash(Object key, Object base) {
		int hash = key.hashCode() & mask;
		while (true) {
			int hKey = hash << 1;
			Object cmpKey = data[hKey];
			if (cmpKey == null) {
				data[hKey] = key;
				data[hKey|1] = base;
				size++;
				return null;
			}
			if (key.equals(cmpKey)) {
				NBTBase oldBase = (NBTBase) data[hKey|1];
				data[hKey|1] = base;
				return oldBase;
			}
			hash = (hash + 1) & mask;
		}
	}

	private void ffRehash(int newExpectedSize) {
		int idealSize = MathHelper.smallestEncompassingPowerOfTwo(((newExpectedSize * 4) + 2) / 3);
		if (data.length != idealSize * 2) {
			if (newExpectedSize == 0) {
				data = EMPTY;
				size = 0;
				mask = 0;
				return;
			}

			Object[] oldData = data;
			data = new Object[idealSize * 2];
			size = 0;
			mask = idealSize - 1;
			for (int i = 0; i < oldData.length; i += 2) {
				if (oldData[i] != null) {
					ffPutAfterRehash(oldData[i], oldData[i|1]);
				}
			}
		}
	}

	NBTBase ffGetTag(String key) {
		if (key == null || size == 0) {
			return null;
		}

		int hash = key.hashCode() & mask;
		while (true) {
			int hKey = hash << 1;
			Object cmpKey = data[hKey];
			if (cmpKey == null) return null;
			if (key.equals(cmpKey)) return (NBTBase) data[hKey|1];
			hash = (hash + 1) & mask;
		}
	}

	NBTBase ffPutTag(String key, NBTBase base) {
		if (key == null) {
			throw new IllegalArgumentException("key = null");
		}

		ffRehash(size + 1);
		return ffPutAfterRehash(key, base);
	}

	NBTBase ffRemove(String key) {
		if (key == null) {
			return null;
		}

		int hash = key.hashCode() & mask;
		while (true) {
			int hKey = hash << 1;
			Object cmpKey = data[hKey];
			if (cmpKey == null) {
				return null;
			}
			if (key.equals(cmpKey)) {
				NBTBase base = (NBTBase) data[hKey|1];
				data[hKey] = null;
				data[hKey|1] = null;
				size--;
				ffRehash(size);
				return base;
			}
			hash = (hash + 1) & mask;
		}
	}

	void ffClear() {
		data = null;
		size = 0;
		mask = 0;
	}

	// NBTTagCompound patchery/kludges
	// TODO: override methods using the tagMap interface

	@Override
	public Map<String, NBTBase> foamfix_getTagMap() {
		return tagMap;
	}

	// we don't implement entrySet()
	public boolean equals(Object p_equals_1_) {
		return super.equals(p_equals_1_) && this.tagMap.equals(((IFoamFixNBTTagCompound) p_equals_1_).foamfix_getTagMap());
	}
}
