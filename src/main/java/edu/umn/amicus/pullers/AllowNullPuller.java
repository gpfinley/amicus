package edu.umn.amicus.pullers;

import org.apache.uima.jcas.tcas.Annotation;

/**
 * Keep all pulled annotations, even when the requested fields are null.
 * (Default Puller behavior is to discard annotations with no non-null fields.)
 * Created by gpfinley on 10/20/16.
 */
public class AllowNullPuller extends Puller {

    @Override
    protected Object callThisGetter(String fieldName, Annotation annotation) throws ReflectiveOperationException {
        Object object = super.callThisGetter(fieldName, annotation);
        return object == null ? "" : object;
    }

}
