package edu.umn.amicus.eval;

import edu.umn.amicus.summary.SummaryWriter;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Create summary statistics (precision, recall, F-score) for EvalMatch objects as created by EvalMatchPusher.
 *
 * Created by greg on 2/10/17.
 */
public class EvalPrfSummaryWriter extends SummaryWriter<EvalMatch> {

    @Override
    public String summarize(List<EvalMatch> matches) {

        Map<Integer, Stats> results = new TreeMap<>();

        for (EvalMatch em : matches) {
            Integer system = em.getSystemIndex();
            Stats stats = results.get(system);
            if (stats == null) {
                stats = new Stats();
                results.put(system, stats);
            }
            stats.add(em);
        }

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Integer, Stats> entry : results.entrySet()) {
            builder.append(String.format("SYSTEM %d:\n\n", entry.getKey()));
            builder.append(entry.getValue());
            builder.append("\n");
        }
        return builder.toString();
    }

    private static class Stats {
        int truePos;
        int falseNeg;
        int falsePos;
        double totalHitScore;

        public void add(EvalMatch em) {
            switch (em.getStatus()) {
                case EvalMatch.FALSE_NEGATIVE:
                    falseNeg++;
                    break;
                case EvalMatch.FALSE_POSITIVE:
                    falsePos++;
                    break;
                case EvalMatch.TRUE_POSITIVE:
                    truePos++;
                    totalHitScore += em.getScore();
                    break;
            }
        }

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
            builder.append("\nMean score of true positives: ");
            builder.append(totalHitScore / truePos);
            builder.append("\n");
            return builder.toString();
        }
    }

}
