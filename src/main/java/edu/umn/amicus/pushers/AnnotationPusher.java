package edu.umn.amicus.pushers;

import edu.umn.amicus.Amicus;
import edu.umn.amicus.AmicusException;
import edu.umn.amicus.AnalysisPiece;
import edu.umn.amicus.PreAnnotation;
import edu.umn.amicus.pullers.AnnotationPuller;
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
 * Basic class for creating annotations on JCas objects
 *
 * Created by gpfinley on 1/20/17.
 */
public abstract class AnnotationPusher<T> extends AnalysisPiece {

    public static final String DEFAULT_PUSHER = SetterPusher.class.getName();
//    public static final String DEFAULT_MULTI_PUSHER = MultiSetterPusher.class.getName();

    protected String typeName;
    protected List<String> fieldNames;

    protected Constructor<? extends Annotation> annotationConstructor;
    protected List<Method> setterMethods;

    protected AnnotationPusher() {}

//    protected AnnotationPusher(String typeName, String fieldName) {
//        this.typeName = typeName;
//        this.fieldName = fieldName;
//    }

    public AnnotationPusher(String typeName, String fieldNamesDelimited) {
        this.typeName = typeName;
        setterMethods = new ArrayList<>();
        Class<? extends Annotation> annotationClass = getClassFromName(typeName);
        annotationConstructor = getAnnotationConstructor(annotationClass);
        fieldNames = Arrays.asList(fieldNamesDelimited.split(AnnotationPuller.FIELD_NAME_DELIMITER));
        setterMethods = new ArrayList<>();
        for (String f : fieldNames) {
            if ("".equals(f)) {
                setterMethods.add(null);
            } else {
                setterMethods.add(getSetterForField(annotationClass, f));
            }
        }
    }

    public abstract void push(JCas jCas, PreAnnotation<T> value);

    /**
     * ...todo: doc
     * Overriding methods might not call this.
     * @param jCas
     * @param value
     */
    public void createNewAnnotation(JCas jCas, PreAnnotation<T> value) {
//        Annotation annotation;
//        try {
//            annotation = annotationConstructor.newInstance(jCas, value.getBegin(), value.getEnd());
//            for (Method setterMethod : setterMethods) {
//                setterMethod.invoke(annotation, value.getValue());
//            }
//        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
//            throw new AmicusException(e);
//        }
//        annotation.addToIndexes();
        Annotation annotation;
        List<Object> valuesToSet;
        // todo: finish
        try {
            annotation = annotationConstructor.newInstance(jCas, value.getBegin(), value.getEnd());
            try {
                assert setterMethods.size() == value.getValue().size();
            } catch (AssertionError e) {
                throw new AmicusException("Length of values list from puller and length of setter methods list " +
                        "not equivalent. Check configuration and puller implementation.");
            }
            for (int i=0; i<setterMethods.size(); i++) {
                if (setterMethods.get(i) != null) {
                    setterMethods.get(i).invoke(annotation, value.getValue().get(i));
                }
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new AmicusException(e);
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

    public static AnnotationPusher create(String pusherClassName, String typeName, String fieldName) {
        if (pusherClassName == null) {
            if (typeName == null || fieldName == null) {
                throw new AmicusException("Need to provide output annnotation fields and types UNLESS using" +
                        " a custom AnnotationPusher implementation that can ignore them.");
            }
//            pusherClassName = fieldName.contains(MultiSetterPusher.DELIMITER) ? DEFAULT_MULTI_PUSHER : DEFAULT_PUSHER;
            pusherClassName = DEFAULT_PUSHER;
        }
        return Amicus.getPieceInstance(AnnotationPusher.class, pusherClassName, typeName, fieldName);
    }

}
