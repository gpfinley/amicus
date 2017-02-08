package edu.umn.ensembles.uimacomponents;

import edu.umn.ensembles.EnsemblesException;
import edu.umn.ensembles.EvalAnnotation;
import edu.umn.ensembles.eval.EvalMatch;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.CasAnnotator_ImplBase;
import org.apache.uima.fit.component.CasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;

import java.util.Map;
import java.util.TreeMap;

/**
 * Simple CAS consumer to print basic statistics for evaluation.
 *
 * Created by gpfinley on 2/3/17.
 */
public class EvalSummarizerAE extends CasAnnotator_ImplBase {

    public static final String EVAL_VIEW_NAME = "evalViewName";

    @ConfigurationParameter(name = EVAL_VIEW_NAME)
    private String evalViewName;

    public void process(CAS cas) {
        JCas evalView;
        try {
            evalView = cas.getView(evalViewName).getJCas();
        } catch (CASException e) {
            throw new EnsemblesException(e);
        }

        // use a treemap for easy reporting of stats ordered by system
        final Map<Integer, Stats> statsMap = new TreeMap<>();

        evalView.getAnnotationIndex(EvalAnnotation.class).forEach(a -> {
            Stats stats = statsMap.get(a.getSystemIndex());
            if (stats == null) {
                stats = new Stats();
                statsMap.put(a.getSystemIndex(), stats);
            }
            switch (a.getStatus()) {
                case EvalMatch.FALSE_NEGATIVE:
                    stats.falseNeg++;
                    break;
                case EvalMatch.FALSE_POSITIVE:
                    stats.falsePos++;
                    break;
                case EvalMatch.TRUE_POSITIVE:
                    stats.truePos++;
                    stats.totalHitScore += a.getScore();
                    break;
            }
        });

        for (Map.Entry<Integer, Stats> entry : statsMap.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }

    }

    private static class Stats {
        int truePos;
        int falseNeg;
        int falsePos;
        double totalHitScore;

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("True positives:  ");
            builder.append(truePos);
            builder.append("\nFalse positives: ");
            builder.append(falsePos);
            builder.append("\nFalse negatives: ");
            builder.append(falseNeg);
            builder.append("\nPrecision: ");
            double tp = truePos;
            double p = tp / (truePos + falsePos);
            builder.append(p);
            builder.append("\nRecall:    ");
            double r = tp / (truePos + falseNeg);
            builder.append(r);
            builder.append("\nF-score:   ");
            builder.append((p * r) / (p + r) * 2);
            builder.append("\nMean accuracy of true positives: ");
            builder.append(totalHitScore / truePos);
            builder.append("\n");
            return builder.toString();
        }
    }

}
