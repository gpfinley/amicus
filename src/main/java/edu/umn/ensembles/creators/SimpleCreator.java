package edu.umn.ensembles.creators;

import edu.umn.ensembles.EnsemblesException;
import edu.umn.ensembles.PreAnnotation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * todo: doc
 * Created by gpfinley on 1/20/17.
 */
public class SimpleCreator extends AnnotationCreator<String> {

    private final Constructor<? extends Annotation> annotationConstructor;
    private final Method setterMethod;

    public SimpleCreator(String typeName, String fieldName) {
        Class<? extends Annotation> annotationClass = getClassFromName(typeName);
        annotationConstructor = getAnnotationConstructor(annotationClass);
        setterMethod = getSetterForField(annotationClass, fieldName);
    }

    @Override
    public void set(JCas jCas, PreAnnotation<String> value) {
        if (value == null) return;
        Annotation annotation;
        try {
            annotation = annotationConstructor.newInstance(jCas, value.getBegin(), value.getEnd());
            setterMethod.invoke(annotation, value.getValue());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            // todo: log
            throw new EnsemblesException(e);
        }
        annotation.addToIndexes();
    }

}