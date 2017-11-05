package edu.umn.amicus.aligners;

import edu.umn.amicus.util.AlignedTuple;
import edu.umn.amicus.AmicusException;
import edu.umn.amicus.util.AnalysisPiece;
import edu.umn.amicus.util.ANA;

import java.util.Iterator;
import java.util.List;

/**
 * Align tuples from all systems to get an iterator over aligned tuples (a tuple may have null elements).
 *
 * Created by gpfinley on 10/21/16.
 */
public interface Aligner extends AnalysisPiece {

    /**
     * @param allAnnotations A list of lists of annotations, one list per input.
     * @return An iterator over AlignedTuple objects, which contain Amicus-Native Annotation objects.
     * @throws AmicusException
     */
    Iterator<AlignedTuple> alignAndIterate(List<List<ANA>> allAnnotations) throws AmicusException;

}
