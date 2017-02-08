package edu.umn.ensembles.eval;

/**
 * Created by gpfinley on 2/8/17.
 */
public class HitOrMissSummarizer implements Summarizer<HitOrMiss, String> {

    public String summarize(Iterable<HitOrMiss> allData) {
        int truePos = 0;
        int falsePos = 0;
        int falseNeg = 0;
        for (HitOrMiss hm : allData) {
            if (hm.positiveOrNegative) {
                if (hm.trueOrFalse) {
                    truePos++;
                } else {
                    falsePos++;
                }
            } else if(!hm.trueOrFalse) {
                falseNeg++;
            }
        }

        StringBuilder builder = new StringBuilder();
        builder.append("True positives: ");

        return builder.toString();
    }
}
