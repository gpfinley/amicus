package edu.umn.amicus.filters;

import org.apache.uima.jcas.tcas.Annotation;

import java.util.List;

/**
 * Created by greg on 2/16/17.
 */
public abstract class AnnotationFilter {

    public abstract List<Boolean> filter(List<Annotation> annotations);

}