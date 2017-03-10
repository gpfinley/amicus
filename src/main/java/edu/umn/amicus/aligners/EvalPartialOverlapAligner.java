package edu.umn.amicus.aligners;

import edu.umn.amicus.aligners.Aligner;
import edu.umn.amicus.aligners.PartialOverlapAligner;
import org.apache.uima.jcas.tcas.Annotation;

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
    public Iterator<List<Annotation>> alignAndIterate(List<List<Annotation>> allAnnotations) {

        // true positives and false negatives list. These will be returned.
        Map<Annotation, List<Annotation>> truePositiveLists = new TreeMap<>(new AnnotationBeginComparator());
        List<List<Annotation>> falsePositiveLists = new ArrayList<>();

        int n = allAnnotations.size();

        for (int i=1; i<n; i++) {
            List<List<Annotation>> goldAndOneHypList = new ArrayList<>();
            goldAndOneHypList.add(allAnnotations.get(0));
            goldAndOneHypList.add(allAnnotations.get(i));
            Iterator<List<Annotation>> iter = new PartialOverlapAligner().alignAndIterate(goldAndOneHypList);
            while (iter.hasNext()) {
                List<Annotation> twoItemList = iter.next();
                Annotation goldAnnotation = twoItemList.get(0);
                Annotation hypAnnotation = twoItemList.get(1);
                if (goldAnnotation == null) {
                    // yield a false positive
                    List<Annotation> thisFalsePositive = new ArrayList<>();
                    for (int j=0; j<n; j++) {
                        thisFalsePositive.add(j == i ? hypAnnotation : null);
                    }
                    falsePositiveLists.add(thisFalsePositive);
                } else {
                    // yield a true positive or false negative
                    List<Annotation> truePositiveList = truePositiveLists.get(goldAnnotation);
                    if (truePositiveList == null) {
                        truePositiveList = new ArrayList<>();
                        truePositiveList.add(goldAnnotation);
                        for (int j=1; j<n; j++) {
                            truePositiveList.add(null);
                        }
                        truePositiveLists.put(goldAnnotation, truePositiveList);
                    }
                    truePositiveList.set(i, hypAnnotation);
                }
            }
        }

        List<List<Annotation>> finalList = new ArrayList<>();
        finalList.addAll(truePositiveLists.values());
        finalList.addAll(falsePositiveLists);
        return finalList.iterator();
    }

    private static class AnnotationBeginComparator implements Comparator<Annotation> {
        @Override
        public int compare(Annotation a1, Annotation a2) {
            return ((Integer) a1.getBegin()).compareTo(a2.getBegin());
        }
    }

}
