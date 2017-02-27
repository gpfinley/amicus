package edu.umn.amicus.pullers;

import org.apache.uima.jcas.tcas.Annotation;

/**
 * A generic getter-calling puller.
 * Created by gpfinley on 10/20/16.
 */
public class GetterPuller extends AnnotationPuller<Object> {

    public GetterPuller(String fieldName) {
        super(fieldName);
    }

    @Override
    public Object transform(Annotation annotation) {
        return callAnnotationGetters(annotation);
    }

}