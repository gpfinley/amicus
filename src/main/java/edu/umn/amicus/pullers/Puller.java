package edu.umn.amicus.pullers;

import edu.umn.amicus.AmicusException;
import edu.umn.amicus.AnalysisPiece;
import edu.umn.amicus.uimacomponents.Util;
import org.apache.uima.jcas.tcas.Annotation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Transform an annotation, probably of known type, into a non-UIMA output of type T (almost certainly String).
 * Extend this class and override pull(...) to process the Annotation values.
 * Created by gpfinley on 10/20/16.
 */
public class Puller implements AnalysisPiece {

    public static final String FIELD_NAME_DELIMITER = ";";

    protected final String[] fieldNames;

    public Puller(String delimitedFieldnames) {
        fieldNames = delimitedFieldnames == null ? null : delimitedFieldnames.split(FIELD_NAME_DELIMITER);
    }

    /**
     * Call this from implementing subclasses that don't make use of the field name.
     */
    protected Puller() {
        this.fieldNames = null;
    }

    /**
     * Main method to get information out of an annotation.
     * Unless you have a compelling reason to do otherwise, most implementations of pull() should call this.
     * Will return a List if there are multiple getters.
     * @param annotation
     * @return
     */
    public Object pull(Annotation annotation) {
        if (fieldNames.length > 1) {
            List<Object> objectList = new ArrayList<>();
            for (String fieldName : fieldNames) {
                objectList.add(callThisGetter(fieldName, annotation));
            }
            return objectList;
        }
        return callThisGetter(fieldNames[0], annotation);
    }

    /**
     * Static version if not using the fieldName class variable.
     * @param fieldName
     * @param annotation
     * @return
     */
    protected static Object callThisGetter(String fieldName, Annotation annotation) {
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