package edu.umn.ensembles.processing;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Special map that maintains counts of objects.
 * Custom objects counted MUST implement equals/hashCode!
 *
 * Created by gpfinley on 10/27/16.
 */
public class Counter<T> implements Map<T, Integer> {

    private final Map<T, MutableInt> counts;

    public Counter() {
        counts = new HashMap<>();
    }

    public Counter(Collection<T> itemsToCount) {
        counts = new HashMap<>();
        itemsToCount.stream().forEach(this::increment);
    }

    public int increment(T t) {
        MutableInt mi = counts.get(t);
        if (mi == null) {
            counts.put(t, new MutableInt(1));
            return 1;
        }
        return mi.increment();
    }

    public int decrement(T t) {
        MutableInt mi = counts.get(t);
        if (mi == null) {
            counts.put(t, new MutableInt(-1));
            return -1;
        }
        return mi.decrement();
    }

    public int add(T t, int toAdd) {
        MutableInt mi = counts.get(t);
        if (mi == null) {
            counts.put(t, new MutableInt(toAdd));
            return toAdd;
        }
        return mi.add(toAdd);
    }

    public int subtract(T t, int toSubtract) {
        MutableInt mi = counts.get(t);
        if (mi == null) {
            counts.put(t, new MutableInt(-toSubtract));
            return -toSubtract;
        }
        return mi.subtract(toSubtract);
    }

    public int total() {
        int total = 0;
        for (MutableInt i : counts.values()) total += i.get();
        return total;
    }

    public TreeMap<T, Integer> createSortedMap() {
        TreeMap<T, Integer> map = new TreeMap<>((x, y) -> {
            int comp = get(x).compareTo(get(y));
            return comp == 0 ? ((Comparable) x).compareTo(y) : comp;
        });
        counts.forEach((k, v) -> map.put(k, v.get()));
        return map;
    }

    @Override
    public void clear() {
        counts.clear();
    }

    @Override
    public void putAll(Map<? extends T, ? extends Integer> map) {
        map.entrySet().forEach(e -> counts.put(e.getKey(), new MutableInt(e.getValue())));
    }

    @Override
    public boolean containsValue(Object value) {
        for (MutableInt mi : counts.values()) {
            if (value.equals(mi.get())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int size() {
        return counts.size();
    }

    @Override
    public boolean isEmpty() {
        return counts.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return counts.containsKey(key);
    }

    @Override
    public List<Integer> values() {
        return counts.values().stream().map(MutableInt::get).collect(Collectors.toList());
    }

    @Override
    public Integer remove(Object key) {
        return counts.remove(key).get();
    }

    @Override
    public Integer get(Object t) {
        return counts.getOrDefault(t, new MutableInt(0)).get();
    }

    @Override
    public Integer put(T t, Integer count) {
        return counts.put(t, new MutableInt(count)).get();
    }

    @Override
    public Set<T> keySet() {
        return counts.keySet();
    }

    @Override
    public Set<Map.Entry<T, Integer>> entrySet() {
        return counts.entrySet().stream().map(Entry::new).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return counts.toString();
    }

    private static class MutableInt {
        private int value;
        public MutableInt(int value) {
            this.value = value;
        }
        public int increment() {
            return ++value;
        }
        public int decrement() {
            return --value;
        }
        public int add(int toAdd) {
            value += toAdd;
            return value;
        }
        public int subtract(int toSubtract) {
            value -= toSubtract;
            return value;
        }
        public int get() {
            return value;
        }
        public void set(int value) {
            this.value = value;
        }
        @Override
        public int hashCode() {
            return ((Integer) value).hashCode();
        }
        @Override
        public boolean equals(Object other) {
            return other.equals(value);
        }
        @Override
        public String toString() {
            return "" + value;
        }
    }

    private class Entry implements Map.Entry<T, Integer> {
        private Map.Entry<T, MutableInt> entry;

        Entry(Map.Entry<T, MutableInt> entry) {
            this.entry = entry;
        }

        @Override
        public T getKey() {
            return entry.getKey();
        }

        @Override
        public Integer getValue() {
            return entry.getValue().get();
        }

        @Override
        public Integer setValue(Integer value) {
            Integer old = entry.getValue().get();
            entry.getValue().set(value);
            return old;
        }

        @Override
        public int hashCode() {
            return entry.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Map.Entry)) return false;
            return ((Map.Entry) other).getKey().equals(entry.getKey())
                && ((Map.Entry) other).getValue().equals(entry.getValue().get());
        }
    }

}
