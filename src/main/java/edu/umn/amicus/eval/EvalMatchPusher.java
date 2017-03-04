package edu.umn.amicus.eval;

import edu.umn.amicus.AmicusException;
import edu.umn.amicus.PreAnnotation;
import edu.umn.amicus.pushers.Pusher;
import org.apache.uima.jcas.JCas;

import java.util.List;

/**
 * Creates annotations based on a specific scheme for evaluating multiple systems at once
 * Created by gpfinley on 2/3/17.
 */
public class EvalMatchPusher extends Pusher {

    public EvalMatchPusher(String typeName, String fieldName) throws AmicusException {
        super(typeName, fieldName);
    }

    /**
     * todo: doc
     * @param jCas
     * @param annot
     */
    @Override
//    public void push(JCas jCas, PreAnnotation<List<EvalMatch>> annot) {
    public void push(JCas jCas, PreAnnotation<Object> evalMatches) {
        for (EvalMatch em : (List<EvalMatch>) evalMatches.getValue()) {
            em.createAnnotationFrom(jCas, evalMatches.getBegin(), evalMatches.getEnd()).addToIndexes();
        }
    }
}