package edu.umn.amicus.aligners;

import org.apache.uima.jcas.tcas.Annotation;

import java.util.Iterator;
import java.util.List;

/**
 * Created by gpfinley on 10/21/16.
 */
public abstract class AnnotationAligner {

    protected AnnotationAligner() {}

    public abstract Iterator<List<Annotation>> alignAndIterate(List<List<Annotation>> allAnnotations);

}
