package edu.umn.amicus.distillers;

import edu.umn.amicus.Amicus;
import edu.umn.amicus.AnalysisPiece;
import edu.umn.amicus.PreAnnotation;

import java.util.List;

/**
 * Interface for classes that take Annotations from all types/systems and distill them to a single Annotation of any Type.
 *
 * Created by gpfinley on 10/20/16.
 */
public abstract class AnnotationDistiller<T> extends AnalysisPiece {

    public static final String DEFAULT_DISTILLER = PriorityDistiller.class.getName();

    public AnnotationDistiller() {}

    public abstract PreAnnotation<T> distill(List<PreAnnotation> annotations);

    public static AnnotationDistiller create(String distillerClassName) {
        return Amicus.getPieceInstance(AnnotationDistiller.class,
                distillerClassName == null ? DEFAULT_DISTILLER : distillerClassName);
    }

}