package edu.umn.ensembles.transformers;

import edu.umn.ensembles.Ensembles;
import edu.umn.ensembles.EnsemblesException;
import edu.umn.ensembles.PreAnnotation;
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
     * Unless you have a compelling reason to do otherwise, implementations of transform() should probably call this.
     * @param annotation
     * @return
     */
    protected Object callAnnotationGetter(Annotation annotation) {
        Class<? extends Annotation> clazz = annotation.getClass();
        try {
            Method toCall = clazz.getMethod(Ensembles.getGetterFor(fieldName));
            return toCall.invoke(annotation);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // todo: log
            throw new EnsemblesException();
        }
    }

}
