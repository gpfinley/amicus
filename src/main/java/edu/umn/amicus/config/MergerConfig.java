package edu.umn.amicus.config;

import edu.umn.amicus.AmicusConfigurationException;

/**
 * A serializable bean for a single merge engine.
 * Contains methods for aggregating options across its inputs and outputs, which is needed for uimaFIT configurations.
 *
 * Created by gpfinley on 10/24/16.
 */
public class MergerConfig extends PipelineComponentConfig {

    public String _mergerName = "untitled merger";

    public AnnotationInputConfig[] inputs;
    public AnnotationOutputConfig[] outputs;

    public String alignerClass;

    /**
     * Verify that these mergers have enough config info
     */
    public void verify() {
        if (inputs == null || inputs.length == 0
                || outputs == null) {
            throw new AmicusConfigurationException("Merger configuration incomplete");
        }
        for(AnnotationInputConfig c : inputs) c.verify();
        for(AnnotationOutputConfig c : outputs) c.verify();
    }

}
