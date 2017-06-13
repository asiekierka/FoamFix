package pl.asie.foamfix.common;

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
    private final IntOpenHashSet keySet = new IntOpenHashSet();
    private final Collection<Object> objects = new ArrayList<>();
    private Object[] keys = new Object[32];
    private int size = 0;

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size > 0;
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
        return i >= 0 && i < 256 ? (V) keys[i] : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object o) {
        int i = (Integer) o;
        return i >= 0 && i < 256 ? (V) keys[i] : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V put(Integer integer, V v) {
        int i = integer;
        if (i >= 0 && i < 256) {
            if (i >= keys.length) {
                Object[] newKeys = new Object[keys.length * 2];
                System.arraycopy(keys, 0, newKeys, 0, keys.length);
                keys = newKeys;
            }
            Object old = keys[i];
            keys[i] = v;
            objects.add(v);
            if (old == null) size++;
            return (V) old;
        } else {
            throw new RuntimeException("EntityDataManager entry outside of the [0, 255] range (" + i + ") was attempted to be added. Please disable EntityDataManager-related optimizations and report this to the FoamFix developers.");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public V remove(Object o) {
        int i = (Integer) o;
        if (i >= 0 && i < 256) {
            Object old = keys[i];
            keys[i] = null;
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
        for (int i = 0; i < 256; i++)
            keys[i] = null;
        size = 0;
    }

    @Override
    public Set<Integer> keySet() {
        return keySet;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<V> values() {
        return (Collection<V>) objects;
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
}
