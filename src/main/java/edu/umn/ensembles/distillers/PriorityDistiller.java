package edu.umn.ensembles.distillers;

import edu.umn.ensembles.PreAnnotation;

import java.util.List;

/**
 * Distiller that will take the first Annotation it comes across and ignore the others.
 * Be sure to list Types in order of priority (highest first) in the configuration.
 *
 * Created by gpfinley on 10/20/16.
 */
public class PriorityDistiller extends AnnotationDistiller<Object> {

    /**
     *
     * Contract: each annotation is a different type, in the same order listed in configuration.
     *              Higher-priority annotations listed first.
     * @param annotations
     */
    @Override
    public PreAnnotation distill(List<PreAnnotation> annotations) {
        try {
            return annotations.get(0);
        } catch (Exception e) {
            return null;
        }
    }

}
