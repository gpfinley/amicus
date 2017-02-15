package edu.umn.ensembles.config;

/**
 * Created by gpfinley on 1/20/17.
 */
public class AmicusPipelineConfiguration {

    public String _pipelineName;
    public SourceSystemConfig[] allSystemsUsed;
    public PipelineComponentConfig[] pipelineComponents;
    public String xmiOutPath;

    /**
     * Throw an exception if not enough information was provided
     */
    public void verify() {
//        if (inputNames == null || inputDirectories == null || inputViews == null
//                || inputNames.size() != inputDirectories.size() || inputNames.size() != inputViews.size()) {
//            throw new EnsemblesException("Pipeline configuration incomplete");
//        }
        for (PipelineComponentConfig c : pipelineComponents) c.verify();
        for (SourceSystemConfig c : allSystemsUsed) c.verify();

        // todo: confirm that there are no two Collectors with the same name
    }

    public String[] aggregateInputDirectories() {
        String[] inputs = new String[allSystemsUsed.length];
        for (int i=0; i<inputs.length; i++) {
            inputs[i] = allSystemsUsed[i].dataPath;
        }
        return inputs;
    }
}
