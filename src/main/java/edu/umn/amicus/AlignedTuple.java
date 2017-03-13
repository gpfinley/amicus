package edu.umn.amicus;

import java.util.*;

/**
 * todo doc
 * todo use in place of List<PreAnnotation> where appropriate
 * Created by gpfinley on 3/10/17.
 */
// todo should it implement List?
//public class AlignedTuple implements List<PreAnnotation> {
public class AlignedTuple<T> implements Iterable<T> {

    private final List<T> list;

    public AlignedTuple(int n) {
        list = new ArrayList<>();
        for (int i=0; i<n; i++) {
            list.add(null);
        }
    }

    public AlignedTuple(List<T> list) {
        this.list = list;
    }

    public T get(int i) {
        return list.get(i);
    }

    public Iterator<T> iterator() {
        return list.iterator();
    }

    public T set(int i, T value) {
        return list.set(i, value);
    }

    public int size() {
        return list.size();
    }

    public boolean contains(T t) {
        return list.contains(t);
    }

    @Override
    public String toString() {
        return list.toString();
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof AlignedTuple) {
            return list.equals(((AlignedTuple) other).list);
        }
        if (other instanceof List) {
            // todo; test (if this is ever going to be used?)
            return list.equals(other);
        }
        return false;
    }

}
