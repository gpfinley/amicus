package edu.umn.ensembles.transformers;

import edu.umn.ensembles.EnsemblesException;
import edu.umn.ensembles.PreAnnotation;
import edu.umn.ensembles.Util;
import org.apache.uima.jcas.tcas.Annotation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Transform an annotation, probably of known type, into a non-UIMA output of type T (almost certainly String).
 * Extend this class and override transform(...) to process the Annotation values.
 * Created by gpfinley on 10/20/16.
 */
public abstract class AnnotationTransformer<T> {

    protected final String fieldName;

    protected AnnotationTransformer(String fieldName) {
        this.fieldName = fieldName;
    }

    abstract public PreAnnotation<T> transform(Annotation annotation);

    /**
     * Main method to get information out of an annotation.
     * Unless you have a compelling reason to do otherwise, most implementations of transform() should call this.
     * @param annotation
     * @return
     */
    protected Object callAnnotationGetter(Annotation annotation) {
        Class<? extends Annotation> clazz = annotation.getClass();
        try {
            Method toCall = clazz.getMethod(Util.getGetterFor(fieldName));
            return toCall.invoke(annotation);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new EnsemblesException(e);
        }
    }

}