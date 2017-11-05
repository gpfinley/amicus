package edu.umn.amicus.pushers;

import edu.umn.amicus.util.ANA;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Set an Annotation as pulled from another CAS. Effectively a copier when using PassthroughPuller.
 *
 * Created by greg on 2/24/17.
 */
public class PassthroughPusher extends Pusher {

    public void push(JCas jCas, ANA<Object> ana) {
        // todo: test!!! does this actually copy to another jCas, or do we need to do something else??
        ((Annotation) ana.getValue()).addToIndexes(jCas);
    }

}
