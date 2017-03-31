package edu.umn.amicus.pullers;

import edu.umn.amicus.AmicusException;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.ArrayList;
import java.util.List;

/**
 * Parse the FSArray of UmlsConcepts provided by cTAKES and return the first CUI.
 * Created by gpfinley on 10/20/16.
 */
public class CtakesCuisPuller extends Puller {

    private final String fieldName;

    public CtakesCuisPuller() {
        this(null);
    }

    public CtakesCuisPuller(String fieldName) {
        this.fieldName = "ontologyConceptArr";
    }

    /**
     * Loop through an FSArray in the cTAKES system and return the first result with a CUI.
     * @param annotation
     * @return
     */
    @Override
    public List<String> pull(Annotation annotation) throws AmicusException {
        List<String> cuis = new ArrayList<>();
        FSArray conceptArray;
        try {
            conceptArray = (FSArray) callThisGetter(fieldName, annotation);
        } catch (ReflectiveOperationException e) {
            throw new AmicusException(e);
        }
        if (conceptArray != null) {
            for (int i = 0; i < conceptArray.size(); i++) {
                if (conceptArray.get(i) instanceof UmlsConcept) {
                    UmlsConcept ontologyConcept = (UmlsConcept) conceptArray.get(0);
                    cuis.add(ontologyConcept.getCui());
                }
            }
        }
        return cuis;
    }
}
