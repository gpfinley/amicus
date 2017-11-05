package edu.umn.amicus.summary;

import edu.umn.amicus.util.AnalysisPiece;

/**
 * Simple base class for keeping Exporter data on hand for any class that does summary.
 *
 * Created by gpfinley on 3/13/17.
 */
public abstract class Summarizer implements AnalysisPiece {

    protected String[] viewNames;
    protected String[] types;
    protected String[] fields;

    protected Summarizer() {}

    protected Summarizer(String[] viewNames, String[] types, String[] fields) {
        this.viewNames = viewNames;
        this.types = types;
        this.fields = fields;
    }

}
