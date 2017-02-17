package edu.umn.amicus.summarizers;

import edu.umn.amicus.Amicus;
import edu.umn.amicus.Piece;

import java.util.List;

/**
 * Created by gpfinley on 2/8/17.
 */
public abstract class Summarizer<D> extends Piece {

    public static final String DEFAULT_SUMMARIZER = CounterSummarizer.class.getName();

    public abstract Object summarize(List<D> allData);

    public static Summarizer create(String distillerClassName) {
        return Amicus.getPieceInstance(Summarizer.class,
                distillerClassName == null ? DEFAULT_SUMMARIZER : distillerClassName);
    }
}