package edu.umn.amicus.aligners;

import edu.umn.amicus.AmicusException;
import org.apache.uima.jcas.tcas.Annotation;

import javax.naming.OperationNotSupportedException;
import java.util.*;

/**
 * Finds all unique overlapping tuples of annotations.
 * Single annotations may be represented in several tuples, depending on how misaligned the annotations really are.
 * Much slower than PerfectOverlapAligner, which can hash beginning/ending values.
 * Created by gpfinley on 12/20/16.
 * // todo: unit test
 */
@Deprecated
public class AllOverlapsAligner implements Aligner {

    /**
     * @param allAnnotations
     * @return
     */
    @Override
    public Iterator<List<Annotation>> alignAndIterate(List<List<Annotation>> allAnnotations) {

        if (true) throw new AmicusException(new OperationNotSupportedException());

        // TODO: IMPLEMENT!

//        List<List<Annotation>> annotationsAtIndex = new ArrayList<>();

        int maxIndex = 0;
        for (List<Annotation> annotations : allAnnotations) {
            for(Annotation annotation : annotations) {
                int end = annotation.getEnd();
                if (end > maxIndex) maxIndex = end;
            }
        }

        Annotation[][] annotationsAtIndex = new Annotation[maxIndex][allAnnotations.size()];

        for (int sysIndex = 0; sysIndex < allAnnotations.size(); sysIndex++) {
            for (Annotation annotation : allAnnotations.get(sysIndex)) {
                for (int i = annotation.getBegin(); i < annotation.getEnd(); i++) {
                    annotationsAtIndex[i][sysIndex] = annotation;
                }
            }
        }



        return null;

        // convert into a set to remove duplicate overlapping annotation lists
//        return new LinkedHashSet<>(annotationsAtIndex).iterator();
    }
}
