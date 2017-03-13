package edu.umn.amicus.aligners;

import edu.umn.amicus.AlignedTuple;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.*;

/**
 * Aligner that will generate tuples of annotations only if ALL inputs overlap at at least one character.
 * Non-overlapping annotations may not be returned at all. Some annotations may be returned multiple times!
 * todo test
 *
 * Created by gpfinley on 10/21/16.
 */
public class RequireOverlapAligner implements Aligner {

    private AllUniqueGroupingsAligner aligner = new AllUniqueGroupingsAligner();

    /**
     * todo doc
     * Will always provide one annotation per system.
     * @param allAnnotations a list of lists of annotations as provided by MultiCasReader
     * @return
     */
    @Override
    public Iterator<AlignedTuple<Annotation>> alignAndIterate(List<List<Annotation>> allAnnotations) {
        List<AlignedTuple<Annotation>> overlappingAnnotationsOnly = new ArrayList<>();
        Iterator<AlignedTuple<Annotation>> iterator = aligner.alignAndIterate(allAnnotations);
        while (iterator.hasNext()) {
            AlignedTuple<Annotation> list = iterator.next();
            if (!list.contains(null)) {
                overlappingAnnotationsOnly.add(list);
            }
        }
        return overlappingAnnotationsOnly.iterator();
    }

}
