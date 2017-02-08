package edu.umn.ensembles.pullers;

import edu.umn.ensembles.PreAnnotation;
import edu.umn.ensembles.processing.CuiMapper;
import edu.umn.ensembles.processing.Mapper;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Parse the FSArray of UmlsConcepts provided by cTAKES and choose the best string for this annotation.
 * This Transformer is equivalent to a mini-pipeline of the CtakesCuiPuller and the CuiConceptPuller.
 * Created by gpfinley on 10/20/16.
 */
public class CtakesConceptPuller extends CtakesCuiPuller {

    private Mapper<String, String> mapper;

    public CtakesConceptPuller(String fieldName) {
        super(fieldName);
        mapper = CuiMapper.getInstance();
    }

    /**
     * Loop through the FSArray in the cTAKES system and return the string form of the first result with a CUI.
     * @param annotation
     * @return
     */
    @Override
    public PreAnnotation<String> transform(Annotation annotation) {
        return new PreAnnotation<>(mapper.map(getCui(annotation)), annotation);
    }

}