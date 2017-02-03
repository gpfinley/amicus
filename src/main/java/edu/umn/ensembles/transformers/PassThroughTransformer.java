package edu.umn.ensembles.transformers;

import edu.umn.ensembles.PreAnnotation;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * This is a true null transformer: it doesn't even call the getter, just passes the Annotation object through.
 * This could be used if your Distiller class uses annotation-specific logic.
 * (A more elegant approach for some situations may be to simply access the same annotation type multiple times,
 * with a different getter each time.)
 * Created by gpfinley on 12/19/16.
 */
public class PassThroughTransformer extends AnnotationTransformer<Annotation> {

    public PassThroughTransformer(String fieldName) {
        super(fieldName);
    }

    @Override
    public PreAnnotation<Annotation> transform(Annotation annotation) {
        return new PreAnnotation<>(annotation, annotation);
    }

}