package edu.umn.ensembles.config;

import java.util.Arrays;
import java.util.List;

/**
 * Created by gpfinley on 1/20/17.
 */
public class EnsemblesPipelineConfiguration {

    public String _pipelineName;
    public SingleSystemConfig[] systemsUsed;
    public SingleMergerConfiguration[] mergerConfigurations;

    /**
     * Throw an exception if not enough information was provided
     */
    public void verify() {
//        if (inputNames == null || inputDirectories == null || inputViews == null
//                || inputNames.size() != inputDirectories.size() || inputNames.size() != inputViews.size()) {
//            throw new EnsemblesException("Pipeline configuration incomplete");
//        }
        Arrays.stream(mergerConfigurations).forEach(SingleMergerConfiguration::verify);
        Arrays.stream(systemsUsed).forEach(SingleSystemConfig::verify);
    }

    // todo: write aggregators for systemsUsed (like MergerConfiguration has)
}
