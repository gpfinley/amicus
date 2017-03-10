package edu.umn.amicus.summary;

import edu.umn.amicus.AmicusException;
import edu.umn.amicus.PreAnnotation;
import edu.umn.amicus.EvalMatch;

import java.util.*;

/**
 * Create summary statistics (precision, recall, F-score) for EvalMatch objects as created by EvalMatchPusher.
 *
 * Created by greg on 2/10/17.
 */
public class EvalPrfMacroSummarizer implements MacroSummarizer {

    @Override
    public String summarize(List<List<PreAnnotation>> list, List<String> docIds) throws AmicusException {

        List<EvalMatch> evalMatches = new ArrayList<>();
        for (List<PreAnnotation> preAnnotations : list) {
            evalMatches.addAll(EvalPrfMicroSummarizer.getEvalMatches(preAnnotations));
        }

        return EvalPrfMicroSummarizer.getReport(evalMatches);
    }

}
