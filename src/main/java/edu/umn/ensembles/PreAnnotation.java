package edu.umn.ensembles;

import org.apache.uima.jcas.tcas.Annotation;

/**
 * "Struct" class for holding an Object to be annotated (probably String) along with its begin and end offsets.
 * Used during intermediate stages (from Transformation to Creation).
 *
 * Created by gpfinley on 1/20/17.
 */
public class PreAnnotation<T> {

    private final T value;
    private final int begin;
    private final int end;

    public PreAnnotation(T value, Annotation annotation) {
        this.value = value;
        this.begin = annotation.getBegin();
        this.end = annotation.getEnd();
    }

    public PreAnnotation(T value, int begin, int end) {
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

}
