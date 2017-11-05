package edu.umn.amicus.aligners;

import edu.umn.amicus.util.AlignedTuple;
import edu.umn.amicus.util.ANA;

import java.util.*;

/**
 * Aligner that will generate tuples of annotations only if they overlap in both begin and end
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
    public Iterator<AlignedTuple> alignAndIterate(List<List<ANA>> allAnnotations) {
        Map<BeginEnd, AlignedTuple> beMap = new HashMap<>();
        for (int i=0; i < allAnnotations.size(); i++) {
            for (ANA annotation : allAnnotations.get(i)) {
                BeginEnd beginEnd = new BeginEnd(annotation.getBegin(), annotation.getEnd());
                if (!beMap.containsKey(beginEnd)) {
                    AlignedTuple newList = new AlignedTuple(allAnnotations.size());
                    beMap.put(beginEnd, newList);
                }
                beMap.get(beginEnd).set(i, annotation);
            }
        }
        return beMap.values().iterator();
    }

}
