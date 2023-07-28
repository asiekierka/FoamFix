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
package pl.asie.foamfix.common.nbt;

import net.minecraft.nbt.NBTBase;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FoamNBTTagCompoundMap implements Map<String, NBTBase> {
	private final FoamNBTTagCompound delegate;

	FoamNBTTagCompoundMap(FoamNBTTagCompound delegate) {
		this.delegate = delegate;
	}

	@Override
	public int size() {
		return delegate.size;
	}

	@Override
	public boolean isEmpty() {
		return delegate.size == 0;
	}

	@Override
	public boolean containsKey(Object key) {
		return delegate.ffGetTag((String) key) == null;
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public NBTBase get(Object key) {
		return delegate.ffGetTag((String) key);
	}

	@Override
	public NBTBase put(String key, NBTBase value) {
		return delegate.ffPutTag(key, value);
	}

	@Override
	public NBTBase remove(Object key) {
		return delegate.ffRemove((String) key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends NBTBase> map) {
		for (Map.Entry<? extends String, ? extends NBTBase> entry : map.entrySet()) {
			// TODO
		}
	}

	@Override
	public void clear() {
		delegate.ffClear();
	}

	@Override
	public Set<String> keySet() {
		// TODO: this could be made faster
		Set<String> keys = new HashSet<>(delegate.data.length);
		for (int i = 0; i < delegate.data.length; i += 2) {
			Object key = delegate.data[i];
			if (key != null) keys.add((String) key);
		}
		return keys;
	}

	@Override
	public Collection<NBTBase> values() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Entry<String, NBTBase>> entrySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Map)) {
			return false;
		}

		Map other = (Map) obj;
		if (other.size() != size()) {
			return false;
		}

		for (int i = 0; i < delegate.data.length; i += 2) {
			Object key = delegate.data[i];
			if (key != null) {
				Object value = delegate.data[i + 1];
				if (!Objects.equals(other.get(key), value)) {
					return false;
				}
			}
		}

		// all keys present + equal size = equal map
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		for (int i = 0; i < delegate.data.length; i += 2) {
			Object key = delegate.data[i];
			if (key != null) {
				Object value = delegate.data[i + 1];
				hash += key.hashCode() + (value != null ? value.hashCode() : 0);
			}
		}
		return hash;
	}
}
