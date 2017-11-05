package edu.umn.amicus.distillers;

import edu.umn.amicus.util.ANA;
import edu.umn.amicus.util.AlignedTuple;
import edu.umn.amicus.AmicusException;
import edu.umn.amicus.util.AnalysisPiece;

/**
 * Interface for classes that take Annotations from all types/systems and distill them to a single Annotation of any Type.
 *
 * Created by gpfinley on 10/20/16.
 */
public interface Distiller<T> extends AnalysisPiece {

    ANA<T> distill(AlignedTuple annotations) throws AmicusException;

}