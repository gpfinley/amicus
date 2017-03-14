package edu.umn.amicus.summary;

import edu.umn.amicus.AlignedTuple;
import edu.umn.amicus.AnalysisPiece;

import java.util.Iterator;

/**
 * Created by greg on 2/11/17.
 */
public interface DocumentSummarizer extends AnalysisPiece {

    Object summarizeDocument(Iterator<AlignedTuple> tuples);

    String getFileExtension();

}
