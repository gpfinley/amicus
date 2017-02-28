package edu.umn.amicus.pullers;

import org.apache.uima.jcas.tcas.Annotation;

/**
 * This is a true null Puller: it doesn't even call the getter, just passes the Annotation object through.
 * This could be used if your Distiller class uses annotation-specific logic.
 * (A more elegant approach for some situations may be to simply access the same annotation type multiple times,
 * with a different getter each time.)
 * Created by gpfinley on 12/19/16.
 */
public class PassthroughPuller extends Puller {

    public PassthroughPuller(String fieldName) {
        super(fieldName);
    }

    @Override
    public Annotation pull(Annotation annotation) {
        return annotation;
    }

}