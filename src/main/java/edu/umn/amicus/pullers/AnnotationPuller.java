package edu.umn.amicus.pullers;

import edu.umn.amicus.AmicusException;
import edu.umn.amicus.uimacomponents.Util;
import org.apache.uima.jcas.tcas.Annotation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Transform an annotation, probably of known type, into a non-UIMA output of type T (almost certainly String).
 * Extend this class and override transform(...) to process the Annotation values.
 * Created by gpfinley on 10/20/16.
 */
public abstract class AnnotationPuller<T> {

    protected final String fieldName;

    public AnnotationPuller(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Call this from implementing subclasses that don't make use of the field name.
     */
    protected AnnotationPuller() {
        this.fieldName = null;
    }

    abstract public T transform(Annotation annotation);

    /**
     * Main method to get information out of an annotation.
     * Unless you have a compelling reason to do otherwise, most implementations of transform() should call this.
     * @param annotation
     * @return
     */
    protected Object callAnnotationGetter(Annotation annotation) {
        return callAnnotationGetter(fieldName, annotation);
    }

    /**
     * Static version if not using the fieldName class variable.
     * @param fieldName
     * @param annotation
     * @return
     */
    protected static Object callAnnotationGetter(String fieldName, Annotation annotation) {
        if (annotation == null) return null;
        Class<? extends Annotation> clazz = annotation.getClass();
        try {
            Method toCall = clazz.getMethod(Util.getGetterFor(fieldName));
            return toCall.invoke(annotation);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new AmicusException(e);
        }
    }

}
