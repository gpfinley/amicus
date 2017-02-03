package edu.umn.ensembles.distillers;

import edu.umn.ensembles.PreAnnotation;

import java.util.List;

/**
 * Interface for classes that take Annotations from all types/systems and distill them to a single Annotation of any Type.
 *
 * Created by gpfinley on 10/20/16.
 */
public abstract class AnnotationDistiller<T> {

    public AnnotationDistiller() {}

    public abstract PreAnnotation<T> distill(List<PreAnnotation> annotations);

}