package edu.umn.amicus.aligners;

import edu.umn.amicus.AlignedTuple;
import edu.umn.amicus.AmicusException;
import edu.umn.amicus.AnalysisPiece;
import edu.umn.amicus.ANA;

import java.util.Iterator;
import java.util.List;

/**
 * Created by gpfinley on 10/21/16.
 */
public interface Aligner extends AnalysisPiece {

    Iterator<AlignedTuple> alignAndIterate(List<List<ANA>> allAnnotations) throws AmicusException;

}
