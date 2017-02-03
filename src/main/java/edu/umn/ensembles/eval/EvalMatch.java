package edu.umn.ensembles.eval;

import edu.umn.ensembles.EnsemblesException;
import edu.umn.ensembles.EvalAnnotation;
import org.apache.uima.jcas.JCas;

/**
 * Object to use in a PreAnnotation for evaluation.
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
            throw new EnsemblesException("Trying to create an EvalMatch outside of specified types;" +
                    "check calling class implementation. Best to use static Strings of EvalMatch");
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
}
