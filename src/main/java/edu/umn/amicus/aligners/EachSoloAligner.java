package edu.umn.amicus.aligners;

import edu.umn.amicus.ANA;
import edu.umn.amicus.AlignedTuple;

import java.util.Iterator;
import java.util.List;

/**
 * This is a non-aligner; it will "align" annotations by representing each annotation exactly once, by itself.
 *
 * // todo: test (devil in the details on this one; should we just put everything into another list and iterate on that??)
 * Created by greg on 2/11/17.
 */
public class EachSoloAligner implements Aligner {

    public Iterator<AlignedTuple> alignAndIterate(List<List<ANA>> annotations) {
        return new AnnotIter(annotations);
    }

    private static class AnnotIter implements Iterator<AlignedTuple> {

        private int systemCounter;
        private int withinSystemCounter;

        private int nSystems;
        private int nAnnotationsThisSystem;

        private final List<List<ANA>> annotations;

        AnnotIter(List<List<ANA>> annotations) {
            this.annotations = annotations;
            systemCounter = 0;
            withinSystemCounter = 0;
            nSystems = annotations.size();
            for (List<ANA> thisSystemList : annotations) {
                if (thisSystemList.size() > 0) {
                    nAnnotationsThisSystem = thisSystemList.size();
                    break;
                }
                systemCounter++;
            }
        }

        @Override
        public AlignedTuple next() {
            AlignedTuple theseAnnots = new AlignedTuple(nSystems);
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
