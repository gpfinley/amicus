package edu.umn.ensembles.creators;

import edu.umn.ensembles.Ensembles;
import edu.umn.ensembles.EnsemblesException;
import edu.umn.ensembles.PreAnnotation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Basic class for creating annotations on JCas objects
 *
 * Created by gpfinley on 1/20/17.
 */
public abstract class AnnotationCreator<T> {

    // Multi-field creators will probably ignore this
    protected String typeName;
    protected String fieldName;

    protected AnnotationCreator() {}

    public AnnotationCreator(String typeName, String fieldName) {
        this.typeName = typeName;
        this.fieldName = fieldName;
    }

    public abstract void set(JCas jCas, PreAnnotation<T> value);

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
        try {
            return clazz.getMethod(Ensembles.getSetterFor(name));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new EnsemblesException();
        }
    }

}
