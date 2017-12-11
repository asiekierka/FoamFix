/*
 * Copyright (C) 2016, 2017 Adrian Siekierka
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
public abstract class FoamyArrayBackedDataManagerMap<V> implements Map<Integer, V> {
    public static class OneTwelve<V> extends FoamyArrayBackedDataManagerMap<V> {
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
        protected void addKey(int i) {
            keySet.add(i);
        }

        @Override
        protected void removeKey(int i) {
            keySet.rem(i);
        }
    }

    private final Collection<Object> objects = new ArrayList<>();
    private Object[] keys = new Object[32];
    private int size = 0;

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

    protected abstract void addKey(int i);
    protected abstract void removeKey(int i);

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
                addKey(i);
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
            removeKey(i);
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
