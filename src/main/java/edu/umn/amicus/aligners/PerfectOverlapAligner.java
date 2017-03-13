package edu.umn.amicus.aligners;

import edu.umn.amicus.AlignedTuple;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.*;

/**
 * Aligner that will generate tuples of annotations only if they overlap in both begin and end
 * // todo: will only use the last-iterated annotation from each system if there is perfect overlap in a single input. allow this? warn?
 *
 * Created by gpfinley on 10/21/16.
 */
public class PerfectOverlapAligner implements Aligner {

    /**
     * Will always provide zero or one annotations per system.
     * @param allAnnotations a list of lists of annotations as provided by MultiCasReader
     * @return
     */
    @Override
    public Iterator<AlignedTuple<Annotation>> alignAndIterate(List<List<Annotation>> allAnnotations) {
        Map<BeginEnd, AlignedTuple<Annotation>> beMap = new HashMap<>();
        for (int i=0; i < allAnnotations.size(); i++) {
            for (Annotation annotation : allAnnotations.get(i)) {
                BeginEnd beginEnd = new BeginEnd(annotation.getBegin(), annotation.getEnd());
                if (!beMap.containsKey(beginEnd)) {
                    AlignedTuple<Annotation> newList = new AlignedTuple<>(allAnnotations.size());
                    beMap.put(beginEnd, newList);
                }
                beMap.get(beginEnd).set(i, annotation);
            }
        }
        return beMap.values().iterator();
    }

}
