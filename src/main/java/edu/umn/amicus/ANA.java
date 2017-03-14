package edu.umn.amicus;

import org.apache.uima.jcas.tcas.Annotation;

/**
 * 'Amicus-Native Annotation'.
 * "Struct" class for holding an Object from an annotation (probably String) along with its begin and end offsets.
 * Used at intermediate stages of processing (between pulling and pushing annotations). Immutable.
 *
 * Created by gpfinley on 1/20/17.
 */
public class ANA<T> {

    private final T value;
    private final int begin;
    private final int end;

    public ANA(T value, Annotation annotation) {
        this.value = value;
        this.begin = annotation.getBegin();
        this.end = annotation.getEnd();
    }

    public ANA(T value, int begin, int end) {
        this.value = value;
        this.begin = begin;
        this.end = end;
    }

    public T getValue() {
        return value;
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "(" + String.valueOf((Object) value) + ", " + begin + ":" + end + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ANA<?> ana = (ANA<?>) o;

        if (begin != ana.begin) return false;
        if (end != ana.end) return false;
        return value != null ? value.equals(ana.value) : ana.value == null;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + begin;
        result = 31 * result + end;
        return result;
    }
}