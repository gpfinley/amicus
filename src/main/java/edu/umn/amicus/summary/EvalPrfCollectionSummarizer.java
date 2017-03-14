//package edu.umn.amicus.summary;
//
//import edu.umn.amicus.AmicusException;
//import edu.umn.amicus.ANA;
//import edu.umn.amicus.EvalMatch;
//
//import java.util.*;
//
///**
// * Create summary statistics (precision, recall, F-score) for EvalMatch objects as created by EvalMatchPusher.
// *
// * Created by greg on 2/10/17.
// */
//@Deprecated
//public class EvalPrfCollectionSummarizer implements CollectionSummarizer {
//
//    @Override
//    public String summarize(List<List<ANA>> list, List<String> docIds) throws AmicusException {
//
//        List<EvalMatch> evalMatches = new ArrayList<>();
//        for (List<ANA> preAnnotations : list) {
//            evalMatches.addAll(EvalPrfSummarizer.getEvalMatches(preAnnotations));
//        }
//
//        return EvalPrfSummarizer.getReport(evalMatches);
//    }
//
//}
