package edu.umn.amicus.distillers;

import edu.umn.amicus.ANA;
import edu.umn.amicus.AlignedTuple;
import edu.umn.amicus.Voter;
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
     *                  with priority order breaking ties.
     *              Voting occurs independently for begin/end values as well.
     * @param annotations
     */
    @Override
    public ANA<Object> distill(AlignedTuple annotations) {
        List<Object> values = new ArrayList<>();
        List<Integer> begins = new ArrayList<>();
        List<Integer> ends = new ArrayList<>();
        for (ANA pa : annotations) {
            if (pa == null) {
                values.add(null);
            } else {
                values.add(pa.getValue());
                begins.add(pa.getBegin());
                ends.add(pa.getEnd());
            }
            values.add(pa == null ? null : pa.getValue());
        }
        Voter<Object> voter = new Voter<>(values);
        if (voter.getWinner() == null) return null;
        if (voter.getHighCount() < minVotesToAnnotate) return null;

        // todo: should we be voting on begin/end of all aligned annotations, or just those w/ winning content?

        int bestBegin = new Voter<>(begins).getWinner();
        int bestEnd = new Voter<>(ends).getWinner();

        for (ANA annot : annotations) {
            if (annot != null && voter.getWinner().equals(annot.getValue())) {
                return new ANA<>(annot.getValue(), bestBegin, bestEnd);
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
