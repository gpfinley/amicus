package edu.umn.amicus.pullers;

import edu.umn.amicus.Amicus;
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
 * Extend this class and override transform(...) to process the Annotation values.
 * Created by gpfinley on 10/20/16.
 */
public abstract class AnnotationPuller<T> extends AnalysisPiece {

    public static final String DEFAULT_PULLER = GetterPuller.class.getName();
//    public static final String DEFAULT_MULTI_PULLER = MultiGetterPuller.class.getName();

    public static final String FIELD_NAME_DELIMITER = ";";

//    protected final String fieldName;
    protected final String[] fieldNames;

    public AnnotationPuller(String delimitedFieldnames) {
//        this.fieldName = fieldName;
        fieldNames = delimitedFieldnames == null ? null : delimitedFieldnames.split(FIELD_NAME_DELIMITER);
    }

    /**
     * Call this from implementing subclasses that don't make use of the field name.
     */
    protected AnnotationPuller() {
        this.fieldNames = null;
    }

    abstract public T transform(Annotation annotation);

    /**
     * Main method to get information out of an annotation.
     * Unless you have a compelling reason to do otherwise, most implementations of transform() should call this.
     * Will return a List if there are multiple getters.
     * @param annotation
     * @return
     */
    protected Object callAnnotationGetters(Annotation annotation) {
        if (fieldNames.length > 1) {
            List<Object> objectList = new ArrayList<>();
            for (String fieldName : fieldNames) {
                objectList.add(callThisGetter(fieldName, annotation));
            }
            return objectList;
        }
        return callThisGetter(fieldNames[0], annotation);
//        return callThisGetter(fieldName, annotation);
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

    public static AnnotationPuller create(String pullerClassName, String fieldName) throws AmicusException {
        if (pullerClassName == null) {
            if (fieldName == null) {
                // todo: severe log, then throw back the exception and have the AE catch/rethrow it
                throw new AmicusException("Need to provide an input annnotation field UNLESS using" +
                        " a custom AnnotationPuller implementation that can ignore them.");
            }
//            pullerClassName = fieldName.contains(MultiGetterPuller.FIELD_NAME_DELIMITER) ? DEFAULT_MULTI_PULLER : DEFAULT_PULLER;
            pullerClassName = DEFAULT_PULLER;
        }

        return Amicus.getPieceInstance(AnnotationPuller.class, pullerClassName, fieldName);
    }

}
