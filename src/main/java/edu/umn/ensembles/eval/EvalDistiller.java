package edu.umn.ensembles.eval;

import edu.umn.ensembles.EnsemblesException;
import edu.umn.ensembles.PreAnnotation;
import edu.umn.ensembles.distillers.AnnotationDistiller;

import java.util.ArrayList;
import java.util.List;

/**
 * todo: doc
 *
 * Created by gpfinley on 10/20/16.
 */
public class EvalDistiller extends AnnotationDistiller<List<EvalMatch>> {

    /**
     * todo: doc
     * <p/>
     * Contract: if first PreAnnotation is null, then exactly one other is non-null and is a false positive.
     * If first PreAnnotation is not null, then all other nulls are false negatives and all non-nulls true positives.
     *
     * @param annotations
     */
    @Override
    public PreAnnotation<List<EvalMatch>> distill(List<PreAnnotation> annotations) {
        List<EvalMatch> evalMatches = new ArrayList<>();
        int begin = 0;
        int end = 0;
        // if first (gold) is null, we have false positives. Otherwise, a mix of true positives and false negatives
        if (annotations.get(0) == null) {
            for (int i = 1; i < annotations.size(); i++) {
                if (annotations.get(i) == null) {
                    evalMatches.add(new EvalMatch(i, EvalMatch.FALSE_POSITIVE));
                    begin = annotations.get(i).getBegin();
                    end = annotations.get(i).getEnd();
                    break;
                }
                if (i == annotations.size()) {
                    throw new EnsemblesException("No annotations found; check this eval aligner implementation");
                }
            }
        } else {
            begin = annotations.get(0).getBegin();
            end = annotations.get(0).getEnd();
            for (int i = 1; i < annotations.size(); i++) {
                if (annotations.get(i) == null) {
                    evalMatches.add(new EvalMatch(i, EvalMatch.FALSE_NEGATIVE));
                } else {
                    double matchScore = getScore(annotations.get(0), annotations.get(i));
                    evalMatches.add(new EvalMatch(i, EvalMatch.TRUE_POSITIVE, matchScore));
                }
            }
        }

        return new PreAnnotation<>(evalMatches, begin, end);
    }

    /**
     * Simple all-or-nothing match score for true (detection) positives
     * Subclasses can override this and implement more detailed scoring schemes, if desired
     * @param gold
     * @param hyp
     * @return
     */
    protected Double getScore(PreAnnotation gold, PreAnnotation hyp) {
//     other eval distillers might have partial matches; this one is all or nothing based on equality
        return gold.getValue().equals(hyp.getValue()) ? 1. : 0.;
    }

}
