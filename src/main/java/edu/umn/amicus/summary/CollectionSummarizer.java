package edu.umn.amicus.summary;

import edu.umn.amicus.AlignedTuple;
import edu.umn.amicus.AmicusException;
import edu.umn.amicus.AnalysisPiece;

import java.util.Iterator;

/**
 * Created by gpfinley on 2/8/17.
 */
public interface CollectionSummarizer extends AnalysisPiece {

    Object summarizeCollection(Iterator<AlignedTuple> tuples, Iterator<String> docIds) throws AmicusException;

}