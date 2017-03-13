package edu.umn.amicus.aligners;

import edu.umn.amicus.AlignedTuple;
import edu.umn.amicus.AmicusException;
import org.apache.uima.jcas.tcas.Annotation;

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
    public Iterator<AlignedTuple<Annotation>> alignAndIterate(List<List<Annotation>> allAnnotations) throws AmicusException {

        List<AlignedTuple<Annotation>> overlappingAnnotationsOnly = new ArrayList<>();
        Iterator<AlignedTuple<Annotation>> iterator = aligner.alignAndIterate(allAnnotations);
        while (iterator.hasNext()) {
            AlignedTuple<Annotation> list = iterator.next();
            if (!list.contains(null)) {
                overlappingAnnotationsOnly.add(list);
            }
        }
        return overlappingAnnotationsOnly.iterator();

//        Map<BeginEnd, List<Annotation>> beMap = new HashMap<>();
//        for (int i=0; i < allAnnotations.size(); i++) {
//            for (Annotation annotation : allAnnotations.get(i)) {
//                BeginEnd beginEnd = new BeginEnd(annotation.getBegin(), annotation.getEnd());
//                if (!beMap.containsKey(beginEnd)) {
//                    List<Annotation> newList = new ArrayList<>();
//                    for (List<Annotation> a : allAnnotations) {
//                        newList.add(null);
//                    }
//                    beMap.put(beginEnd, newList);
//                }
//                beMap.get(beginEnd).set(i, annotation);
//            }
//        }
//        List<List<Annotation>> finalList = new ArrayList<>();
//        for (List<Annotation> aList : beMap.values()) {
//            if (!aList.contains(null)) {
//                finalList.add(aList);
//            }
//        }
//        return finalList.iterator();
    }

}
