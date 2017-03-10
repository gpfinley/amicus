package edu.umn.amicus.summary;

import edu.umn.amicus.AmicusException;
import edu.umn.amicus.AnalysisPiece;
import edu.umn.amicus.PreAnnotation;

import java.util.List;

/**
 * Created by gpfinley on 2/8/17.
 */
public interface MacroSummarizer extends AnalysisPiece {

    Object summarize(List<List<PreAnnotation>> allData, List<String> docIds) throws AmicusException;

}