package edu.umn.amicus.aligners;

import edu.umn.amicus.AlignedTuple;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * This is a non-aligner; it will "align" annotations by representing each annotation exactly once, by itself.
 *
 * // todo: test (devil in the details on this one; should we just put everything into another list and iterate on that??)
 * Created by greg on 2/11/17.
 */
public class EachSoloAligner implements Aligner {

    public Iterator<AlignedTuple<Annotation>> alignAndIterate(List<List<Annotation>> annotations) {
        return new AnnotIter(annotations);
    }

    private static class AnnotIter implements Iterator<AlignedTuple<Annotation>> {

        private int systemCounter;
        private int withinSystemCounter;

        private int nSystems;
        private int nAnnotationsThisSystem;

        private final List<List<Annotation>> annotations;

        AnnotIter(List<List<Annotation>> annotations) {
            this.annotations = annotations;
            systemCounter = 0;
            withinSystemCounter = 0;
            nSystems = annotations.size();
            for (List<Annotation> thisSystemList : annotations) {
                if (thisSystemList.size() > 0) {
                    nAnnotationsThisSystem = thisSystemList.size();
                    break;
                }
                systemCounter++;
            }
        }

        @Override
        public AlignedTuple<Annotation> next() {
            AlignedTuple<Annotation> theseAnnots = new AlignedTuple<>(nSystems);
            theseAnnots.set(systemCounter, annotations.get(systemCounter).get(withinSystemCounter));

            withinSystemCounter++;
            while (withinSystemCounter >= nAnnotationsThisSystem) {
                withinSystemCounter = 0;
                systemCounter++;
                if (systemCounter >= nSystems) break;           // and will return false for next hasNext
                nAnnotationsThisSystem = annotations.get(systemCounter).size();
            }

            return theseAnnots;
        }

        @Override
        public boolean hasNext() {
            return systemCounter < nSystems;
        }

        @Override
        public void remove() { }
    }

}
