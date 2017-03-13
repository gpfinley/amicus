package edu.umn.amicus.summary;

import edu.umn.amicus.AlignedTuple;
import edu.umn.amicus.AnalysisPiece;
import edu.umn.amicus.PreAnnotation;

import java.util.Iterator;
import java.util.List;

/**
 * Created by greg on 2/11/17.
 */
public interface DocumentSummarizer extends AnalysisPiece {

    Object summarizeDocument(Iterator<AlignedTuple<PreAnnotation>> tuples);

    String getFileExtension();

}
