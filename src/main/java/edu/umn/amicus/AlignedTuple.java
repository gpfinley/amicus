package edu.umn.amicus;

import java.util.*;

/**
 * Data structure for holding an aligned tuple of AMICUS-native annotations.
 * Output for Aligners, input for Distillers and Summarizers.
 *
 * Created by gpfinley on 3/10/17.
 */
public class AlignedTuple implements Iterable<ANA> {

    private final List<ANA> list;

    public AlignedTuple(int n) {
        list = new ArrayList<>();
        for (int i=0; i<n; i++) {
            list.add(null);
        }
    }

    public AlignedTuple(List<ANA> list) {
        this.list = new ArrayList<>();
        for (int i=0; i<list.size(); i++) {
            ANA ana = list.get(i);
            ANA newAna = new ANA<>(ana.getValue(), ana.getBegin(), ana.getEnd());
            newAna.setInputIndex(i);
            this.list.add(newAna);
        }
    }

    public ANA get(int i) {
        return list.get(i);
    }

    public Iterator<ANA> iterator() {
        return list.iterator();
    }

    public ANA set(int i, ANA value) {
        if (value != null) value.setInputIndex(i);
        return list.set(i, value);
    }

    public int size() {
        return list.size();
    }

    public boolean contains(ANA t) {
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
        return other instanceof List && list.equals(other);
    }

}
