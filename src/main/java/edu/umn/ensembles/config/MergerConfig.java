package edu.umn.ensembles.config;

import edu.umn.ensembles.Ensembles;
import edu.umn.ensembles.EnsemblesException;

import java.util.ArrayList;
import java.util.List;

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

    public String alignerClass = Ensembles.DEFAULT_ALIGNER_CLASS.getName();

    /**
     * Verify that these mergers have enough config info
     */
    public void verify() {
        if (inputs == null || inputs.length == 0
                || outputs == null) {
            throw new EnsemblesException("Translator or merger configuration incomplete");
        }
        for(AnnotationInputConfig c : inputs) c.verify();
        for(AnnotationOutputConfig c : outputs) c.verify();
    }

}
