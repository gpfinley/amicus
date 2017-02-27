package edu.umn.amicus.pushers;

import edu.umn.amicus.AmicusException;
import edu.umn.amicus.PreAnnotation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * For setting multiple fields on a single annotation
 *
 * Created by gpfinley on 1/20/17.
 */
// todo: roll this into SetterPusher. One class for both.
public class MultiSetterPusher extends AnnotationPusher<List> {

    public static final String DELIMITER = ";";

    private final Constructor<? extends Annotation> annotationConstructor;
    private final List<Method> setterMethods;

    public MultiSetterPusher(String typeName, String fieldNamesDelimited) {
        Class<? extends Annotation> annotationClass = getClassFromName(typeName);
        annotationConstructor = getAnnotationConstructor(annotationClass);

        List<String> fieldNames = Arrays.asList(fieldNamesDelimited.split(DELIMITER));
        setterMethods = new ArrayList<>();
        for (String f : fieldNames) {
            if ("".equals(f)) {
                setterMethods.add(null);
            } else {
                setterMethods.add(getSetterForField(annotationClass, f));
            }
        }
    }

    @Override
    public void push(JCas jCas, PreAnnotation<List> value) {
        Annotation annotation;
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

}
