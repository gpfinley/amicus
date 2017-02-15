package edu.umn.ensembles.distillers;

import edu.umn.ensembles.PreAnnotation;

import java.util.List;

/**
 * Distiller that will take the Annotation from the first system--
 * as long as all other systems have an overlapping annotation present.
 *
 * Created by gpfinley on 10/20/16.
 */
public class RequireAnnotationsDistiller extends AnnotationDistiller<Object> {

    /**
     *
     * @param annotations
     */
    @Override
    public PreAnnotation distill(List<PreAnnotation> annotations) {
        for (PreAnnotation pa : annotations) {
            if (pa == null) return null;
        }
        return annotations.size() == 0 ? null : annotations.get(0);
    }

}
