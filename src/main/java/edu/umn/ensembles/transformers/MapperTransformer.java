package edu.umn.ensembles.transformers;

import edu.umn.ensembles.PreAnnotation;
import edu.umn.ensembles.processing.Mapper;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * This is a generic getter-calling and mapping transformer.
 * Uses the Mapper class; other Transformers might use extensions of Mapper.
 * Should not have more than one of these in the pipeline unless they all use the same Mapper.
 * Created by gpfinley on 10/20/16.
 */
public class MapperTransformer extends AnnotationTransformer {

    private final Mapper mapper;

    public MapperTransformer(String fieldName) {
        super(fieldName);
        mapper = Mapper.getInstance();
    }

    @Override
    public PreAnnotation transform(Annotation annotation) {
        return new PreAnnotation(mapper.map(callAnnotationGetter(annotation)), annotation);
    }

}