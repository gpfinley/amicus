package edu.umn.amicus.distillers;

import edu.umn.amicus.ANA;
import edu.umn.amicus.AlignedTuple;

/**
 * Distiller that will simply count the non-null annotations passed to it.
 * Can be used in an idiom with VotingDistiller and a Translator to enforce a minimum annotation threshold
 *      (e.g., do not write an output annotation unless at least n systems have one here).
 *
 * Created by gpfinley on 2/17/17.
 */
public class CountingDistiller implements Distiller<Integer> {

    /**
     * Save all annotations into a list. Null values and annotations will be added to the list to maintain length
     *      predictable from the number of inputs.
     *
     * @param annotations
     */
    @Override
    public ANA<Integer> distill(AlignedTuple annotations) {
        if (annotations.size() == 0) return null;

        int n = 0;
        Integer begin = null;
        Integer end = null;
        for (ANA pa : annotations) {
            if (pa != null) {
                n++;
                if (begin == null) {
                    begin = pa.getBegin();
                    end = pa.getEnd();
                }
            }
        }
        return new ANA<>(n, begin, end);
    }

}
