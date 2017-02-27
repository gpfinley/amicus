package edu.umn.amicus.pushers;

import edu.umn.amicus.PreAnnotation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * todo: doc
 * Created by greg on 2/24/17.
 */
public class PassthroughPusher extends AnnotationPusher<Annotation> {

    public void push(JCas jCas, PreAnnotation<Annotation> annotation) {
        // todo: test!!! does this actually copy to another jCas, or do we need to do something else??
        annotation.getValue().addToIndexes(jCas);
    }

}
