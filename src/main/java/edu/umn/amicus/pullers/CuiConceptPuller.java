package edu.umn.amicus.pullers;

import edu.umn.amicus.processing.CuiMapper;
import edu.umn.amicus.processing.Mapper;
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
    public String transform(Annotation annotation) {
        return mapper.map((String) callAnnotationGetter(annotation));
    }

}
