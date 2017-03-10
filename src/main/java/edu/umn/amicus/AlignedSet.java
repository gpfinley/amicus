package edu.umn.amicus;

import java.util.*;

/**
 * todo doc
 * todo use in place of List<PreAnnotation> where appropriate
 * Created by gpfinley on 3/10/17.
 */
//public class AlignedSet implements List<PreAnnotation> {
public class AlignedSet implements Iterable<PreAnnotation> {

    private final List<PreAnnotation> list;

    public AlignedSet(int n) {
        list = new ArrayList<>();
        for (int i=0; i<n; i++) {
            list.add(null);
        }
    }

    public PreAnnotation get(int i) {
        return list.get(i);
    }

    public Iterator<PreAnnotation> iterator() {
        return list.iterator();
    }

    public PreAnnotation set(int i, PreAnnotation value) {
        return list.set(i, value);
    }

}
