package edu.umn.ensembles.pullers;

import edu.umn.ensembles.PreAnnotation;
import edu.umn.ensembles.processing.CuiMapper;
import edu.umn.ensembles.processing.Mapper;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Performs UMLS CUI lookup mapping for annotations that are UMLS CUIs. (E.g., NER in CLAMP.)
 *
 * Created by gpfinley on 10/20/16.
 */
public class CuiConceptPuller extends AnnotationPuller<String> {

    public CuiConceptPuller(String fieldName) {
        super(fieldName);
    }

    private final Mapper<String, String> mapper = CuiMapper.getInstance();

    @Override
    public PreAnnotation<String> transform(Annotation annotation) {
        return new PreAnnotation<>(mapper.map((String) callAnnotationGetter(annotation)), annotation);
    }

}
