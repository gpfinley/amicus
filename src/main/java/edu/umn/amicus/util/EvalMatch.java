package edu.umn.amicus;

import edu.umn.amicus.EvalAnnotation;
import org.apache.uima.jcas.JCas;

/**
 * Object to use in a ANA for a precision/recall/F-score type evaluation.
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
    private final String value;

    public EvalMatch(int systemIndex, String status, Double score, String value) {
        if (!TRUE_POSITIVE.equals(status) && !FALSE_POSITIVE.equals(status) && !FALSE_NEGATIVE.equals(status)) {
            throw new RuntimeException("Trying to create an EvalMatch outside of specified types;" +
                    "check calling class implementation. Best to use the static Strings declared in EvalMatch");
        }
        this.systemIndex = systemIndex;
        this.status = status;
        this.score = score;
        this.value = value;
    }

    public EvalMatch(int systemIndex, String status) {
        this(systemIndex, status, null, null);
    }

    public EvalAnnotation createAnnotationFrom(JCas jCas, int begin, int end) {
        EvalAnnotation annotation = new EvalAnnotation(jCas, begin, end);
        annotation.setSystemIndex(systemIndex);
        annotation.setStatus(status);
        if (score != null) {
            annotation.setScore(score);
        }
        if (value != null) {
            annotation.setValue(value);
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

    public String getValue() {
        return value;
    }
}
