package edu.umn.ensembles.eval;

import edu.umn.ensembles.PreAnnotation;
import edu.umn.ensembles.creators.AnnotationCreator;
import org.apache.uima.jcas.JCas;

import java.util.List;

/**
 * Creates annotations based on a specific scheme for evaluating multiple systems at once
 * Created by gpfinley on 2/3/17.
 */
public class EvalCreator extends AnnotationCreator<List<EvalMatch>> {

    /**
     * todo: doc
     * @param jCas
     * @param annot
     */
    public void set(JCas jCas, PreAnnotation<List<EvalMatch>> annot) {
        for (EvalMatch em : annot.getValue()) {
            em.createAnnotationFrom(jCas, annot.getBegin(), annot.getEnd()).addToIndexes();
        }
    }
}