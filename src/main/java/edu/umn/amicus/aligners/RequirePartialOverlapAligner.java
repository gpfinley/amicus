package edu.umn.amicus.aligners;

import edu.umn.amicus.AlignedTuple;
import edu.umn.amicus.ANA;

import java.util.*;

/**
 * Aligner that will generate tuples of annotations only if ALL inputs overlap at at least one character.
 * Non-overlapping annotations may not be returned at all. Some annotations may be returned multiple times!
 * todo test
 *
 * Created by gpfinley on 10/21/16.
 */
public class RequirePartialOverlapAligner implements Aligner {

    private AllUniqueGroupingsAligner aligner = new AllUniqueGroupingsAligner();

    /**
     * todo doc
     * Will always provide one annotation per system.
     * @param allAnnotations a list of lists of annotations as provided by MultiCasReader
     * @return
     */
    @Override
    public Iterator<AlignedTuple> alignAndIterate(List<List<ANA>> allAnnotations) {
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
