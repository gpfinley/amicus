package edu.umn.ensembles.transformers;

import edu.umn.ensembles.PreAnnotation;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Parse the FSArray of UmlsConcepts provided by cTAKES and return the first CUI.
 * Created by gpfinley on 10/20/16.
 */
public class CtakesCuiTransformer extends AnnotationTransformer<String> {

    public CtakesCuiTransformer(String fieldName) {
        super(fieldName);
    }

    /**
     * Loop through the FSArray in the cTAKES system and return the first result with a CUI.
     * @param annotation
     * @return
     */
    @Override
    public PreAnnotation<String> transform(Annotation annotation) {
        return new PreAnnotation(getCui(annotation), annotation);
    }

    protected String getCui(Annotation annotation) {
        String cui = "";
        FSArray conceptArray = (FSArray) callAnnotationGetter(annotation);
        if (conceptArray != null) {
            for (int i = 0; i < conceptArray.size(); i++) {
                if (conceptArray.get(i) instanceof UmlsConcept) {
                    UmlsConcept ontologyConcept = (UmlsConcept) conceptArray.get(0);
                    cui = ontologyConcept.getCui();
                    break;
                }
            }
        }
        return cui;
    }
}
