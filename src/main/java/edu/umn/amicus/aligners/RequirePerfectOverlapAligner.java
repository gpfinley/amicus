package edu.umn.amicus.aligners;

import edu.umn.amicus.util.AlignedTuple;
import edu.umn.amicus.AmicusException;
import edu.umn.amicus.util.ANA;

import java.util.*;

/**
 * Aligner that will generate tuples of annotations only if ALL inputs overlap in both begin and end.
 * Non-overlapping annotations may not be represented at all.
 *
 * Created by gpfinley on 10/21/16.
 */
public class RequirePerfectOverlapAligner implements Aligner {

    Aligner aligner = new PerfectOverlapAligner();

    /**
     * todo: doc
     * Will always provide zero one annotation per system.
     * @param allAnnotations a list of lists of annotations as provided by MultiCasReader
     * @return
     */
    @Override
    public Iterator<AlignedTuple> alignAndIterate(List<List<ANA>> allAnnotations) throws AmicusException {

        List<AlignedTuple> overlappingAnnotationsOnly = new ArrayList<>();
        Iterator<AlignedTuple> iterator = aligner.alignAndIterate(allAnnotations);
        while (iterator.hasNext()) {
            AlignedTuple list = iterator.next();
            if (!list.contains(null)) {
                overlappingAnnotationsOnly.add(list);
            }
        }
        return overlappingAnnotationsOnly.iterator();

    }

}
