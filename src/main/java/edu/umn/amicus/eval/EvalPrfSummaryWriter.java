package edu.umn.amicus.eval;

import edu.umn.amicus.Counter;
import edu.umn.amicus.summary.SummaryWriter;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Create summary statistics (precision, recall, F-score) for EvalMatch objects as created by EvalMatchPusher.
 *
 * todo: summarize errors!
 *
 * Created by greg on 2/10/17.
 */
public class EvalPrfSummaryWriter implements SummaryWriter<EvalMatch> {

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

            Counter<String> correct = new Counter<>();
            Counter<String> errors = new Counter<>();
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

        Counter<String> correct = new Counter<>();
        Counter<String> incorrect = new Counter<>();

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
                    if (em.getValue() != null) {
                        if (em.getScore() == 1) {
                            correct.increment(em.getValue());
                        } else if (em.getScore() == 0) {
                            incorrect.increment(em.getValue());
                        }
                    }
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

            builder.append("\n\nCORRECT LABELS:\n\n");
            for (Map.Entry<String, Integer> entry : correct.createSortedMap().entrySet()) {
                builder.append(entry.getValue())
                        .append("\t")
                        .append(entry.getKey())
                        .append("\n");
            }
            builder.append("\n\nINCORRECT LABELS:\n\n");
            for (Map.Entry<String, Integer> entry : incorrect.createSortedMap().entrySet()) {
                builder.append(entry.getValue())
                        .append("\t")
                        .append(entry.getKey())
                        .append("\n");
            }

            return builder.toString();
        }
    }

}
