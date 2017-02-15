package edu.umn.ensembles.eval;

import edu.umn.ensembles.EvalAnnotation;
import edu.umn.ensembles.PreAnnotation;
import edu.umn.ensembles.pullers.AnnotationPuller;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.List;

/**
 * Used with a Collector to generate summary data for eval matches.
 *
 * Created by greg on 2/10/17.
 */
public class EvalMatchPuller extends AnnotationPuller<EvalMatch> {

    public EvalMatchPuller() {}

    @Override
    public EvalMatch transform(Annotation annotation) {
        EvalAnnotation ea = (EvalAnnotation) annotation;
        return new EvalMatch(ea.getSystemIndex(), ea.getStatus(), ea.getScore());
    }

}
