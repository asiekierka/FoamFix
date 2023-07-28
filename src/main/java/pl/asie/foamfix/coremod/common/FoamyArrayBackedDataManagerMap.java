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
package pl.asie.foamfix.coremod.common;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import java.util.*;

/**
 * Methods whose performance we care about:
 * - containsKey, get, values
 * Methods whose performance matters slightly, but we really don't:
 * - put, remove
 * Assumptions we can make:
 * - all keys are non-null
 */
public class FoamyArrayBackedDataManagerMap<V> implements Map<Integer, V> {
    private final Collection<Object> objects = new ArrayList<>();
    private Object[] keys = new Object[32];
    private int size = 0;
    private final IntOpenHashSet keySet = new IntOpenHashSet();

    @Override
    public Set<Integer> keySet() {
        return keySet;
    }

    @Override
    public Set<Entry<Integer, V>> entrySet() {
        HashSet<Entry<Integer, V>> e = new HashSet<>();
        IntIterator ii = keySet.iterator();
        while (ii.hasNext()) {
            int i = ii.nextInt();
            e.add(new Entry<Integer, V>() {
                @Override
                public Integer getKey() {
                    return i;
                }

                @Override
                public V getValue() {
                    return get(i);
                }

                @Override
                public V setValue(V v) {
                    return put(i, v);
                }
            });
        }
        return e;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object o) {
        return get(o) != null;
    }

    public boolean containsKey(int i) {
        return get(i) != null;
    }

    @Override
    public boolean containsValue(Object o) {
        return objects.contains(o);
    }

    @SuppressWarnings("unchecked")
    public V get(int i) {
        return i >= 0 && i < keys.length ? (V) keys[i] : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object o) {
        int i = (Integer) o;
        return i >= 0 && i < keys.length ? (V) keys[i] : null;
    }
    @Override
    @SuppressWarnings("unchecked")
    public V put(Integer integer, V v) {
        int i = integer;
        if (i >= 0 && i < 256) {
            if (i >= keys.length) {
                int newSize = keys.length;
                while (newSize <= i) newSize *= 2;

                Object[] newKeys = new Object[newSize];
                System.arraycopy(keys, 0, newKeys, 0, keys.length);
                keys = newKeys;
            }
            Object old = keys[i];
            keys[i] = v;
            if (old == null) {
                keySet.add(i);
                size++;
            } else {
                objects.remove(old);
            }
            objects.add(v);
            return (V) old;
        } else {
            throw new RuntimeException("EntityDataManager entry outside of the [0, 255] range (" + i + ") was attempted to be added. Please disable EntityDataManager-related optimizations and report this to the FoamFix developers.");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public V remove(Object o) {
        int i = (Integer) o;
        if (i >= 0 && i < keys.length) {
            Object old = keys[i];
            keys[i] = null;
            keySet.rem(i);
            objects.remove(old);
            if (old != null) size--;
            return (V) old;
        } else return null;
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends V> map) {
        map.forEach(this::put);
    }

    @Override
    public void clear() {
        for (int i : keySet()) {
            keys[i] = null;
        }
        keySet().clear();
        size = 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<V> values() {
        return (Collection<V>) objects;
    }
}
