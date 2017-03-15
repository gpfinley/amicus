package edu.umn.amicus.filters;

import edu.umn.amicus.AnalysisPiece;

/**
 * Created by greg on 2/16/17.
 */
public interface Filter extends AnalysisPiece {

    boolean passes(Object value);

}