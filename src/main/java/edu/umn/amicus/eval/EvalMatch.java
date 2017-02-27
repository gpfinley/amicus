package edu.umn.amicus.eval;

import edu.umn.amicus.AmicusException;
import edu.umn.amicus.EvalAnnotation;
import org.apache.uima.jcas.JCas;

/**
 * Object to use in a PreAnnotation for a precision/recall/F-score type evaluation.
 *
 * Created by gpfinley on 2/3/17.
 */
public class EvalMatch {

    public static final String TRUE_POSITIVE = "TruePositive";
    public static final String FALSE_POSITIVE = "FalsePositive";
    public static final String FALSE_NEGATIVE = "FalseNegative";

    private final int systemIndex;
    private final String status;
    private final Double score;

    public EvalMatch(int systemIndex, String status, Double score) {
        if (!TRUE_POSITIVE.equals(status) && !FALSE_POSITIVE.equals(status) && !FALSE_NEGATIVE.equals(status)) {
            throw new AmicusException("Trying to create an EvalMatch outside of specified types;" +
                    "check calling class implementation. Best to use the static Strings declared in EvalMatch");
        }
        this.systemIndex = systemIndex;
        this.status = status;
        this.score = score;
    }

    public EvalMatch(int systemIndex, String status) {
        this(systemIndex, status, null);
    }

    public EvalAnnotation createAnnotationFrom(JCas jCas, int begin, int end) {
        EvalAnnotation annotation = new EvalAnnotation(jCas, begin, end);
        annotation.setSystemIndex(systemIndex);
        annotation.setStatus(status);
        if (score != null) {
            annotation.setScore(score);
        }
        return annotation;
    }

    public int getSystemIndex() {
        return systemIndex;
    }

    public String getStatus() {
        return status;
    }

    public Double getScore() {
        return score;
    }
}
