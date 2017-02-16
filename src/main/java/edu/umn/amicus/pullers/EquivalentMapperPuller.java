package edu.umn.amicus.pullers;

import edu.umn.amicus.processing.EquivalentAnswerMapper;
import edu.umn.amicus.processing.Mapper;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * This is a generic getter-calling and mapping transformer.
 * Uses the Mapper class; other Transformers might use extensions of Mapper.
 * Should not have more than one of these in the pipeline unless they all use the same Mapper.
 * Created by gpfinley on 10/20/16.
 */
public class EquivalentMapperPuller extends AnnotationPuller {

    private final Mapper mapper;

    public EquivalentMapperPuller(String fieldName) {
        super(fieldName);
        mapper = EquivalentAnswerMapper.getInstance();
    }

    @Override
    public Object transform(Annotation annotation) {
        Object contents = callAnnotationGetter(annotation);
        return contents == null ? null : mapper.map(contents);
//        return mapper.map(callAnnotationGetter(annotation));
    }

}