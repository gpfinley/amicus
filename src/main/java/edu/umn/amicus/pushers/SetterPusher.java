package edu.umn.amicus.pushers;

import edu.umn.amicus.AmicusException;
import edu.umn.amicus.PreAnnotation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * todo: doc
 * Created by gpfinley on 1/20/17.
 */
public class SetterPusher extends AnnotationPusher<Object> {

//    private final Constructor<? extends Annotation> annotationConstructor;
//    private final Method setterMethod;
//    private final List<Method> setterMethods;

    public SetterPusher(String typeName, String fieldName) {
        super(typeName, fieldName);
//        Class<? extends Annotation> annotationClass = getClassFromName(typeName);
//        annotationConstructor = getAnnotationConstructor(annotationClass);
//        setterMethods = new ArrayList<>();
//
//        setterMethod = getSetterForField(annotationClass, fieldName);
    }

    @Override
    public void push(JCas jCas, PreAnnotation<Object> value) {
        createNewAnnotation(jCas, value);
    }

}