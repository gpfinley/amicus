package edu.umn.amicus.aligners;

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
    public Iterator<List<Annotation>> alignAndIterate(List<List<Annotation>> allAnnotations) {
        List<List<Annotation>> overlappingAnnotationsOnly = new ArrayList<>();
        Iterator<List<Annotation>> iterator = aligner.alignAndIterate(allAnnotations);
        while (iterator.hasNext()) {
            List<Annotation> list = iterator.next();
            if (!list.contains(null)) {
                overlappingAnnotationsOnly.add(list);
            }
        }
        return overlappingAnnotationsOnly.iterator();
    }

}
