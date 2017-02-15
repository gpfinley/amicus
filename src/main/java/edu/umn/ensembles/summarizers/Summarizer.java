package edu.umn.ensembles.summarizers;

import java.util.List;

/**
 * Created by gpfinley on 2/8/17.
 */
public interface Summarizer<D> {
    Object summarize(List<D> allData);
}