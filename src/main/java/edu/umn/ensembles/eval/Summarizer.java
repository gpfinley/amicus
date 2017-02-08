package edu.umn.ensembles.eval;

/**
 * Created by gpfinley on 2/8/17.
 */
public interface Summarizer<D, S> {
    S summarize(Iterable<D> allData);
}
