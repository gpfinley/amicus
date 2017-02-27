package edu.umn.amicus.summary;

import edu.umn.amicus.Amicus;
import edu.umn.amicus.Piece;

import java.util.List;

/**
 * Created by gpfinley on 2/8/17.
 */
public abstract class SummaryWriter<D> extends Piece {

    public static final String DEFAULT_SUMMARIZER = CounterSummaryWriter.class.getName();

    public abstract Object summarize(List<D> allData);

    public static SummaryWriter create(String distillerClassName) {
        return Amicus.getPieceInstance(SummaryWriter.class,
                distillerClassName == null ? DEFAULT_SUMMARIZER : distillerClassName);
    }
}