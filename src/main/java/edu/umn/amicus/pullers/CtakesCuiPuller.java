package edu.umn.amicus.pullers;

import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Parse the FSArray of UmlsConcepts provided by cTAKES and return the first CUI.
 * Created by gpfinley on 10/20/16.
 */
public class CtakesCuiPuller extends Puller {

    private final String fieldName;

    public CtakesCuiPuller() {
        this(null);
    }

    public CtakesCuiPuller(String fieldName) {
        // todo: find out the field name to use by default here
        this.fieldName = fieldName == null ? "TODO DEFAULT CTAKES FIELDNAME" : fieldName;
    }

    /**
     * Loop through an FSArray in the cTAKES system and return the first result with a CUI.
     * @param annotation
     * @return
     */
    @Override
    public String pull(Annotation annotation) {
        String cui = "";
        FSArray conceptArray = (FSArray) callThisGetter(fieldName, annotation);
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
