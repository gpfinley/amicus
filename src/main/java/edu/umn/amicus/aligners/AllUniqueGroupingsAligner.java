package edu.umn.amicus.aligners;

import edu.umn.amicus.AlignedTuple;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.*;

/**
 * Aligner that will generate tuples of annotations for every unique grouping.
 * Some annotations may be returned multiple times.
 * todo test
 *
 * Created by gpfinley on 10/21/16.
 */
public class AllUniqueGroupingsAligner implements Aligner {

    /**
     * todo doc
     * Will always provide zero or one annotations per system.
     * @param allAnnotations a list of lists of annotations as provided by MultiCasReader
     * @return
     */
    @Override
    public Iterator<AlignedTuple<Annotation>> alignAndIterate(List<List<Annotation>> allAnnotations) {
        Set<AlignedTuple<Annotation>> allSets = new LinkedHashSet<>();
        List<AlignedTuple<Annotation>> annotationsAtIndex = new ArrayList<>();
        for (int sysIndex = 0; sysIndex < allAnnotations.size(); sysIndex++) {
            for (Annotation annotation : allAnnotations.get(sysIndex)) {
                // not adding all set memberships yet--just singletons
                while (annotationsAtIndex.size() < annotation.getEnd()) {
                    annotationsAtIndex.add(new AlignedTuple<Annotation>(allAnnotations.size()));
                }
                for (int i = annotation.getBegin(); i < annotation.getEnd(); i++) {
                    annotationsAtIndex.get(i).set(sysIndex, annotation);
                }
            }
        }
        allSets.addAll(annotationsAtIndex);
        // remove a null tuple
        allSets.remove(new AlignedTuple<Annotation>(allAnnotations.size()));
        return allSets.iterator();
    }

//    static List<Annotation> getNullList(int n) {
//        List<Annotation> list = new ArrayList<>();
//        for (int i=0; i<n; i++) {
//            list.add(null);
//        }
//        return list;
//    }

}
