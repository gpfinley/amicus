package edu.umn.amicus.pushers;

import edu.umn.amicus.Amicus;
import edu.umn.amicus.AmicusException;
import edu.umn.amicus.Piece;
import edu.umn.amicus.PreAnnotation;
import edu.umn.amicus.uimacomponents.Util;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Basic class for creating annotations on JCas objects
 *
 * Created by gpfinley on 1/20/17.
 */
public abstract class AnnotationPusher<T> extends Piece {

    public static final String DEFAULT_PUSHER = SetterPusher.class.getName();
    public static final String DEFAULT_MULTI_PUSHER = MultiSetterPusher.class.getName();

    // Multi-field pushers will probably ignore this
    protected String typeName;
    protected String fieldName;

    protected AnnotationPusher() {}

    protected AnnotationPusher(String typeName, String fieldName) {
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
            pusherClassName = fieldName.contains(MultiSetterPusher.DELIMITER) ? DEFAULT_MULTI_PUSHER : DEFAULT_PUSHER;
        }
        return Amicus.getPieceInstance(AnnotationPusher.class, pusherClassName, typeName, fieldName);
    }

}
