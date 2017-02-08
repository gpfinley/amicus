package edu.umn.ensembles.config;

/**
 * Created by gpfinley on 1/20/17.
 */
public class EnsemblesPipelineConfiguration {

    public String _pipelineName;
    public SingleSystemConfig[] allSystemsUsed;
    public SingleMergerConfiguration[] mergerConfigurations;
    public String xmiOutPath;

    /**
     * Throw an exception if not enough information was provided
     */
    public void verify() {
//        if (inputNames == null || inputDirectories == null || inputViews == null
//                || inputNames.size() != inputDirectories.size() || inputNames.size() != inputViews.size()) {
//            throw new EnsemblesException("Pipeline configuration incomplete");
//        }
        for (SingleMergerConfiguration c : mergerConfigurations) c.verify();
        for (SingleSystemConfig c : allSystemsUsed) c.verify();
        // todo: java 8 (delete)
//        Arrays.stream(mergerConfigurations).forEach(SingleMergerConfiguration::verify);
//        Arrays.stream(allSystemsUsed).forEach(SingleSystemConfig::verify);
    }

    public String[] aggregateInputDirectories() {
        String[] inputs = new String[allSystemsUsed.length];
        for (int i=0; i<inputs.length; i++) {
            inputs[i] = allSystemsUsed[i].dataPath;
        }
        return inputs;
    }
}
