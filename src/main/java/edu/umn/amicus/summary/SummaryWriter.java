package edu.umn.amicus.summary;

import edu.umn.amicus.AnalysisPiece;

import java.util.List;

/**
 * Created by gpfinley on 2/8/17.
 */
public interface SummaryWriter<D> extends AnalysisPiece {

    Object summarize(List<D> allData);

}