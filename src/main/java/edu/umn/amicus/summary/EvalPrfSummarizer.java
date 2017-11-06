package edu.umn.amicus.summary;

import edu.umn.amicus.util.ANA;
import edu.umn.amicus.util.AlignedTuple;
import edu.umn.amicus.util.Counter;
import edu.umn.amicus.util.EvalMatch;

import java.util.*;

/**
 * Created by gpfinley on 3/10/17.
 */
public class EvalPrfSummarizer extends Summarizer implements DocumentSummarizer, CollectionSummarizer {

    @Override
    public String summarizeDocument(Iterator<AlignedTuple> tuples, String docId, String docText) {
        List<EvalMatch> evalMatches = new ArrayList<>();
        while(tuples.hasNext()) {
            evalMatches.addAll(getEvalMatches(tuples.next()));
        }
        return getReport(evalMatches);
    }

    @Override
    public String summarizeCollection(Iterator<AlignedTuple> tuples, Iterator<String> docIds) {
        return summarizeDocument(tuples, null, null);
    }

    @Override
    public String getFileExtension() {
        return "txt";
    }

    public static List<EvalMatch> getEvalMatches(AlignedTuple annotations) {
        List<EvalMatch> evalMatches = new ArrayList<>();
        // if first (gold) is null, we have false positives. Otherwise, a mix of true positives and false negatives
        if (annotations.get(0) == null) {
            for (int i = 1; i < annotations.size(); i++) {
                if (annotations.get(i) != null) {
                    evalMatches.add(new EvalMatch(i, EvalMatch.FALSE_POSITIVE));
                }
            }
        } else {
            for (int i = 1; i < annotations.size(); i++) {
                if (annotations.get(i) == null) {
                    evalMatches.add(new EvalMatch(i, EvalMatch.FALSE_NEGATIVE));
                } else {
                    double matchScore = getScore(annotations.get(0), annotations.get(i));
                    String valueToStore = matchScore == 1 ? "" + annotations.get(0).getValue()
                            : annotations.get(0).getValue() + " : " + annotations.get(i).getValue();
                    evalMatches.add(new EvalMatch(i, EvalMatch.TRUE_POSITIVE, matchScore, valueToStore));
                }
            }
        }
        return evalMatches;
    }

    public static String getReport(List<EvalMatch> matches) {
        StringBuilder builder = new StringBuilder();

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

        for (Map.Entry<Integer, Stats> entry : results.entrySet()) {
            builder.append(String.format("SYSTEM %d:\n\n", entry.getKey()))
                    .append(entry.getValue())
                    .append("\n");
        }
        return builder.toString();
    }

    /**
     * Simple all-or-nothing match score for true (detection) positives
     * Subclasses can override this and implement more detailed scoring schemes, if desired
     * @param gold
     * @param hyp
     * @return
     */
    protected static double getScore(ANA gold, ANA hyp) {
        if (gold.getValue() == null) {
            // todo: better logging!!!
            System.out.println("Null object in gold??");
            return 0.;
        }
        // other eval distillers might have partial matches; this one is all or nothing based on equality
        return gold.getValue().equals(hyp.getValue()) ? 1. : 0.;
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
