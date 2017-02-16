package edu.umn.amicus.pushers;

import edu.umn.amicus.AmicusException;
import edu.umn.amicus.PreAnnotation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * todo: doc
 * Created by gpfinley on 1/20/17.
 */
public class SimplePusher extends AnnotationPusher<String> {

    private final Constructor<? extends Annotation> annotationConstructor;
    private final Method setterMethod;

    public SimplePusher(String typeName, String fieldName) {
        Class<? extends Annotation> annotationClass = getClassFromName(typeName);
        annotationConstructor = getAnnotationConstructor(annotationClass);
        setterMethod = getSetterForField(annotationClass, fieldName);
    }

    @Override
    public void push(JCas jCas, PreAnnotation<String> value) {
        if (value == null) return;
        Annotation annotation;
        try {
            annotation = annotationConstructor.newInstance(jCas, value.getBegin(), value.getEnd());
            setterMethod.invoke(annotation, value.getValue());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            // todo: log
            throw new AmicusException(e);
        }
        annotation.addToIndexes();
    }

}