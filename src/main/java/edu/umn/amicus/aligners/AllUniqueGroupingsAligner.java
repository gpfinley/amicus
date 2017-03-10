package edu.umn.amicus.aligners;

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
    public Iterator<List<Annotation>> alignAndIterate(List<List<Annotation>> allAnnotations) {
        Set<List<Annotation>> allSets = new LinkedHashSet<>();
        List<List<Annotation>> annotationsAtIndex = new ArrayList<>();
        for (int sysIndex = 0; sysIndex < allAnnotations.size(); sysIndex++) {
            for (Annotation annotation : allAnnotations.get(sysIndex)) {
                // not adding all set memberships yet--just singletons
                while (annotationsAtIndex.size() < annotation.getEnd()) {
                    annotationsAtIndex.add(getNullList(allAnnotations.size()));
                }
                for (int i = annotation.getBegin(); i < annotation.getEnd(); i++) {
                    annotationsAtIndex.get(i).set(sysIndex, annotation);
                }
            }
        }
        allSets.addAll(annotationsAtIndex);
        allSets.remove(getNullList(allAnnotations.size()));
        return allSets.iterator();
    }

    static List<Annotation> getNullList(int n) {
        List<Annotation> list = new ArrayList<>();
        for (int i=0; i<n; i++) {
            list.add(null);
        }
        return list;
    }

}
