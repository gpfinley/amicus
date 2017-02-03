package edu.umn.ensembles.transformers;

import edu.umn.ensembles.PreAnnotation;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * This is a generic getter-calling transformer (i.e., do no processing; needs String values).
 * If no transformer is specified, this is the default.
 * Created by gpfinley on 10/20/16.
 */
public class GetterTransformer extends AnnotationTransformer {

    public GetterTransformer(String fieldName) {
        super(fieldName);
    }

    @Override
    public PreAnnotation transform(Annotation annotation) {
        return new PreAnnotation(callAnnotationGetter(annotation), annotation);
    }

}