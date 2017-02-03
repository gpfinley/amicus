package edu.umn.ensembles.aligners;

import org.apache.uima.jcas.tcas.Annotation;

import java.util.*;

/**
 * Aligner that will generate tuples of annotations only if they overlap in both begin and end
 *
 * Created by gpfinley on 10/21/16.
 */
public class PerfectOverlapAligner extends AnnotationAligner {

    /**
     * Will always provide zero or one annotations per system.
     * @param allAnnotations a list of lists of annotations as provided by MultiCasReader
     * @return
     */
    @Override
    public Iterator<List<Annotation>> alignAndIterate(List<List<Annotation>> allAnnotations) {
        Map<BeginEnd, List<Annotation>> beMap = new HashMap<>();
        for (int i=0; i < allAnnotations.size(); i++) {
            for (Annotation annotation : allAnnotations.get(i)) {
                BeginEnd beginEnd = new BeginEnd(annotation.getBegin(), annotation.getEnd());
                if (!beMap.containsKey(beginEnd)) {
                    List<Annotation> newList = new ArrayList<>();
                    allAnnotations.forEach(x -> newList.add(null));
                    beMap.put(beginEnd, newList);
                }
                beMap.get(beginEnd).set(i, annotation);
            }
        }
        return beMap.values().iterator();
    }

    private static class BeginEnd {
        int begin;
        int end;

        BeginEnd(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BeginEnd beginEnd = (BeginEnd) o;

            if (begin != beginEnd.begin) return false;
            return end == beginEnd.end;

        }

        @Override
        public int hashCode() {
            int result = begin;
            result = 31 * result + end;
            return result;
        }
    }

}
