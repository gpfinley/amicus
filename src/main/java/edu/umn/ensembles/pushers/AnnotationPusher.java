package edu.umn.ensembles.pushers;

import edu.umn.ensembles.EnsemblesException;
import edu.umn.ensembles.PreAnnotation;
import edu.umn.ensembles.uimacomponents.Util;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Basic class for creating annotations on JCas objects
 *
 * Created by gpfinley on 1/20/17.
 */
public abstract class AnnotationPusher<T> {

    // Multi-field pushers will probably ignore this
    protected String typeName;
    protected String fieldName;

    protected AnnotationPusher() {}

    public AnnotationPusher(String typeName, String fieldName) {
        this.typeName = typeName;
        this.fieldName = fieldName;
    }

    public abstract void push(JCas jCas, PreAnnotation<T> value);

    // utility methods for subclasses to use

    protected static Class<? extends Annotation> getClassFromName(String name) {
        try {
            return (Class<? extends Annotation>) Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new EnsemblesException();
        }
    }

    protected static Constructor<? extends Annotation> getAnnotationConstructor(Class<? extends Annotation> annotationClass) {
        try {
            return annotationClass.getConstructor(JCas.class, int.class, int.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new EnsemblesException();
        }
    }

    protected static Method getSetterForField(Class clazz, String name) {
        String setterName = Util.getSetterFor(name);
            for(Method method : clazz.getMethods()) {
                if (method.getName().equals(setterName)) {
                    return method;
                }
            }
            throw new EnsemblesException(new NoSuchMethodException(setterName));
    }

}
