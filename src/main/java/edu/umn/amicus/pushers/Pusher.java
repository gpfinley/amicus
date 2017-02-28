package edu.umn.amicus.pushers;

import edu.umn.amicus.AmicusException;
import edu.umn.amicus.AnalysisPiece;
import edu.umn.amicus.PreAnnotation;
import edu.umn.amicus.pullers.Puller;
import edu.umn.amicus.uimacomponents.Util;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Basic class for creating annotations on JCas objects.
 * Can work with lists of objects or single objects and with single or multiple fields on the annotation.
 * (But in the case of a lists of objects AND fields, they should be the same length.
 *      Nulls are allowable in the lists and will be skipped.)
 *
 * Created by gpfinley on 1/20/17.
 */
public class Pusher implements AnalysisPiece {

    public static final String LIST_STRING_DELIMITER = "|";

    protected String typeName;
    protected List<String> fieldNames;

    protected Constructor<? extends Annotation> annotationConstructor;
    protected List<Method> setterMethods;

    protected Pusher() {}

    public Pusher(String typeName, String fieldNamesDelimited) {
        this.typeName = typeName;
        setterMethods = new ArrayList<>();
        Class<? extends Annotation> annotationClass = getClassFromName(typeName);
        annotationConstructor = getAnnotationConstructor(annotationClass);
        fieldNames = Arrays.asList(fieldNamesDelimited.split(Puller.FIELD_NAME_DELIMITER));
        setterMethods = new ArrayList<>();
        for (String f : fieldNames) {
            if ("".equals(f)) {
                setterMethods.add(null);
            } else {
                setterMethods.add(getSetterForField(annotationClass, f));
            }
        }
    }

    /**
     * ...todo: doc
     * Overriding methods might not call this.
     * @param jCas
     * @param preAnnotation
     */
    public void push(JCas jCas, PreAnnotation<Object> preAnnotation) {
        if (setterMethods == null) {
            throw new AmicusException("Need to provide setters for included Pusher implementations; " +
                    "check configuration.");
        }
        Annotation annotation;
        try {
            annotation = annotationConstructor.newInstance(jCas, preAnnotation.getBegin(), preAnnotation.getEnd());
        } catch (ReflectiveOperationException e) {
            throw new AmicusException(e);
        }
        Object value = preAnnotation.getValue();
        if (setterMethods.size() > 1) {
            List toSet;
            try {
                if (value instanceof List) {
                    toSet = (List) value;
                    assert toSet.size() == setterMethods.size();
                } else {
                    toSet = buildListFromString(value.toString());
                    assert toSet.size() == setterMethods.size();
                }
            } catch (AssertionError e) {
                throw new AmicusException("Length of values list from puller and length of setter methods list " +
                        "not equivalent. Check configuration and puller implementation.");
            }
            for (int i=0; i<setterMethods.size(); i++) {
                if (setterMethods.get(i) != null) {
                    try {
                        setterMethods.get(i).invoke(annotation, toSet.get(i));
//                        setValueOnAnnotation(annotation, setterMethods.get(i), toSet.get(i));
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new AmicusException(e);
                    }
                }
            }
        } else {
            Object toSet = value instanceof List ? buildStringFromList((List) value) : value;
            try {
                setterMethods.get(0).invoke(annotation, toSet);
//                setValueOnAnnotation(annotation, setterMethods.get(0), toSet);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new AmicusException(e);
            }
        }
        annotation.addToIndexes();
    }


    // utility methods for subclasses to use

    protected static Class<? extends Annotation> getClassFromName(String name) {
        try {
            return (Class<? extends Annotation>) Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new AmicusException(e);
        }
    }

    protected static Constructor<? extends Annotation> getAnnotationConstructor(Class<? extends Annotation> annotationClass) {
        try {
            return annotationClass.getConstructor(JCas.class, int.class, int.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new AmicusException(e);
        }
    }

    protected static Method getSetterForField(Class clazz, String name) {
        String setterName = Util.getSetterFor(name);
            for(Method method : clazz.getMethods()) {
                if (method.getName().equals(setterName)) {
                    return method;
                }
            }
            throw new AmicusException(new NoSuchMethodException(setterName));
    }

    // static methods

    public static String buildStringFromList(List list) {
        if (list.size() == 0) return "";
        StringBuilder builder = new StringBuilder();
        builder.append(list.get(0));
        for (int i=1; i<list.size(); i++) {
            builder.append(LIST_STRING_DELIMITER).append(list.get(i));
        }
        return builder.toString();
    }

    public static List<String> buildListFromString(String string) {
        return Arrays.asList(string.split(LIST_STRING_DELIMITER));
    }

}
