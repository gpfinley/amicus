package edu.umn.amicus.summary;

import edu.umn.amicus.AmicusException;
import edu.umn.amicus.util.AlignedTuple;
import edu.umn.amicus.util.AnalysisPiece;

import java.util.Iterator;

/**
 * Per-document summarizer used by the Exporter Analysis Engine.
 * Created by greg on 2/11/17.
 */
public interface DocumentSummarizer extends AnalysisPiece {

    /**
     * Generate a summary, probably a String, from a document's aligned tuples.
     * Each tuple contains one Amicus-Native Annotation (ANA) per input annotation.
     * @param tuples an iterator over all tuples for this document
     * @return a String to write to file
     */
    Object summarizeDocument(Iterator<AlignedTuple> tuples, String docId, String docText) throws AmicusException;

    /**
     * Get the appropriate file extension to use for this Summarizer's output.
     * @return a file extension such as 'txt' or 'csv'
     */
    String getFileExtension();

}
