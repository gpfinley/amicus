package edu.umn.amicus.aligners;

import edu.umn.amicus.ANA;
import edu.umn.amicus.AlignedTuple;

import java.util.*;

/**
 * Will perform partial overlap alignments with an emphasis on producing good alignments to the first system.
 * Better for evaluation than the standard PartialOverlapAligner.
 *
 * Created by greg on 3/2/17.
 * TODO: TEST
 */
public class EvalPartialOverlapAligner implements Aligner {

    @Override
    public Iterator<AlignedTuple> alignAndIterate(List<List<ANA>> allAnnotations) {

        // true positives and false negatives list. These will be returned.
        Map<ANA, AlignedTuple> truePositiveTuples = new TreeMap<>(new AnnotationBeginComparator());
        List<AlignedTuple> falsePositiveTuples = new ArrayList<>();

        int n = allAnnotations.size();

        for (int i=1; i<n; i++) {
            // todo: document method (goes pairwise with a PartialOverlapAligner between gold and every system)
            List<List<ANA>> goldAndOneHypList = new ArrayList<>();
            goldAndOneHypList.add(allAnnotations.get(0));
            goldAndOneHypList.add(allAnnotations.get(i));
            Iterator<AlignedTuple> iter = new PartialOverlapAligner().alignAndIterate(goldAndOneHypList);
            while (iter.hasNext()) {
                AlignedTuple twoItemList = iter.next();
                ANA goldAnnotation = twoItemList.get(0);
                ANA hypAnnotation = twoItemList.get(1);
                if (goldAnnotation == null) {
                    // yield a false positive
                    AlignedTuple thisFalsePositive = new AlignedTuple(n);
                    for (int j=0; j<n; j++) {
                        thisFalsePositive.set(j, j == i ? hypAnnotation : null);
                    }
                    falsePositiveTuples.add(thisFalsePositive);
                } else {
                    // yield a true positive or false negative
                    AlignedTuple truePositiveTuple = truePositiveTuples.get(goldAnnotation);
                    if (truePositiveTuple == null) {
                        truePositiveTuple = new AlignedTuple(n);
                        truePositiveTuple.set(0, goldAnnotation);
                        for (int j=1; j<n; j++) {
                            truePositiveTuple.set(j, null);
                        }
                        truePositiveTuples.put(goldAnnotation, truePositiveTuple);
                    }
                    truePositiveTuple.set(i, hypAnnotation);
                }
            }
        }

        List<AlignedTuple> finalList = new ArrayList<>();
        finalList.addAll(truePositiveTuples.values());
        finalList.addAll(falsePositiveTuples);
        return finalList.iterator();
    }

    private static class AnnotationBeginComparator implements Comparator<ANA> {
        @Override
        public int compare(ANA a1, ANA a2) {
            int comp = ((Integer) a1.getBegin()).compareTo(a2.getBegin());
            if (comp != 0) return comp;
            return ((Comparable) a1.getValue()).compareTo(a2.getValue());
        }
    }

}
