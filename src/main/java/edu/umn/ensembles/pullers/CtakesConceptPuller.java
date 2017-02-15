package edu.umn.ensembles.pullers;

import edu.umn.ensembles.PreAnnotation;
import edu.umn.ensembles.processing.CuiMapper;
import edu.umn.ensembles.processing.Mapper;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Parse the FSArray of UmlsConcepts provided by cTAKES and choose the best string for this annotation.
 * This Transformer is equivalent to a mini-pipeline of the CtakesCuiPuller and the CuiConceptPuller.
 *
 * Deprecated as of Feb 2017. Use CtakesCuiPuller, then CuiConceptPuller.
 * I deprecated this to avoid having to simultaneously develop for this and the two above Pullers.
 * But maybe it's best to not deprecate it since it could save someone a step?
 *
 * Created by gpfinley on 10/20/16.
 */
@Deprecated
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
    public String transform(Annotation annotation) {
        return mapper.map(getCui(annotation));
    }

}