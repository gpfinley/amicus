package edu.umn.amicus.distillers;

import edu.umn.amicus.ANA;
import edu.umn.amicus.AlignedTuple;
import edu.umn.amicus.config.ClassConfigurationLoader;
import edu.umn.amicus.Counter;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Distiller that will take the annotation with the greatest post-transformation agreement among systems.
 * Disagreements in begin/end are ignored.
 * Break ties by priority order.
 * Set threshold on the number of votes needed (to prevent low precision, e.g.) in the class config file.
 * todo: test
 * todo: can we remove the minimum count configuration parameter from this, and replicate its behavior with a NumberFilter?
 *
 * Created by gpfinley on 12/8/16.
 */
public class VotingDistiller implements Distiller<Object> {

    private static final int minVotesToAnnotate;

    /**
     *
     * Contract: each annotation is a different type, in the same order listed in configuration.
     *              Higher-priority annotations listed first.
     *              Answer with the most votes will be annotated,
     *                  with priority order breaking ties (and determining begin/end).
     * @param annotations
     */
    @Override
    public ANA<Object> distill(AlignedTuple annotations) {
        List<Object> values = new ArrayList<>();
        for (ANA pa : annotations) {
            values.add(pa == null ? null : pa.getValue());
        }
        Counter<Object> annotationsCounter = new Counter<>(values);
        int maxCount = 0;
        Object highestCount = "no object";
        for (Map.Entry<Object, Integer> entry : annotationsCounter.entrySet()) {
            if (entry.getValue() > maxCount && entry.getKey() != null) {
                maxCount = entry.getValue();
                highestCount = entry.getKey();
            }
        }
        if (maxCount < minVotesToAnnotate) return null;
        for (ANA annot : annotations) {
            if (annot != null && highestCount.equals(annot.getValue())) {
                return annot;
            }
        }
        // problem if it gets this far (objects didn't properly implement hashCode or equals)
        return null;
    }

    private static class Config {
        public int minVotesToAnnotate;
    }

    // todo: test!
    static {
        Config config;
        try {
            config = (Config) ClassConfigurationLoader.load(VotingDistiller.Config.class);
        } catch (FileNotFoundException e) {
            config = null;
        }
        minVotesToAnnotate = config == null ? 1 : config.minVotesToAnnotate;
    }

}
