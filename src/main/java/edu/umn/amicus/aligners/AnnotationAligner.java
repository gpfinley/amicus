package edu.umn.amicus.aligners;

import edu.umn.amicus.Amicus;
import edu.umn.amicus.AmicusException;
import edu.umn.amicus.Piece;
import edu.umn.amicus.pullers.MultiGetterPuller;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.Iterator;
import java.util.List;

/**
 * Created by gpfinley on 10/21/16.
 */
public abstract class AnnotationAligner extends Piece {

    public static final String DEFAULT_ALIGNER = PerfectOverlapAligner.class.getName();

    protected AnnotationAligner() {}

    public abstract Iterator<List<Annotation>> alignAndIterate(List<List<Annotation>> allAnnotations);

    public static AnnotationAligner create(String alignerClassName) {
        return Amicus.getPieceInstance(AnnotationAligner.class,
                alignerClassName == null ? DEFAULT_ALIGNER : alignerClassName);
    }

}
