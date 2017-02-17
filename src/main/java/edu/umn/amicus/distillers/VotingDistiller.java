package edu.umn.amicus.distillers;

import edu.umn.amicus.PreAnnotation;
import edu.umn.amicus.config.ClassConfigurationLoader;
import edu.umn.amicus.processing.Counter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Distiller that will take the annotation with the greatest post-transformation agreement among systems.
 * Disagreements in begin/end are ignored.
 * Break ties by priority order.
 * Set threshold on the number of votes needed (to prevent low precision, e.g.) in the class config file.
 * todo: test
 *
 * Created by gpfinley on 12/8/16.
 */
public class VotingDistiller extends AnnotationDistiller<Object> {

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
    public PreAnnotation<Object> distill(List<PreAnnotation> annotations) {
        List<Object> values = new ArrayList<>();
        for (PreAnnotation pa : annotations) {
            values.add(pa.getValue());
        }
        Counter<Object> annotationsCounter = new Counter<>(values);
        int maxCount = 0;
        Object highestCount = null;
        for (Map.Entry<Object, Integer> entry : annotationsCounter.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                highestCount = entry.getKey();
            }
        }
        if (maxCount < minVotesToAnnotate) return null;
        for (PreAnnotation annot : annotations) {
            if (annot.getValue().equals(highestCount)) {
                return annot;
            }
        }
        // problem if it gets this far (objects didn't properly implement hashCode or equals)
        return null;
    }

    private static class Config {
        int minVotesToAnnotate;
    }

    static {
        Config config;
        try {
            config = (Config) ClassConfigurationLoader.load(VotingDistiller.class);
        } catch (IOException e) {
            config = null;
        }
        minVotesToAnnotate = config == null ? 1 : config.minVotesToAnnotate;
    }

}
