package edu.umn.amicus.config;

import edu.umn.amicus.AmicusException;

/**
 * A serializable bean for a single merge engine.
 * Contains methods for aggregating options across its inputs and outputs, which is needed for uimaFIT configurations.
 *
 * Created by gpfinley on 10/24/16.
 */
public class TranslatorConfig extends PipelineComponentConfig {

    public String _translatorName = "untitled translator";

    public AnnotationInputConfig input;
    public AnnotationOutputConfig[] outputs;

    public String filterClassName;
    public String filterPattern;

    // todo: revert to single mapper??
    public String[] mapperConfigPaths;

    /**
     * Verify that these mergers have enough config info
     */
    public void verify() {
//        if (inputs == null || inputs.length == 0
//                || outputs == null) {
//            throw new AmicusException("Translator or merger configuration incomplete");
//        }
//        for(AnnotationInputConfig c : inputs) c.verify();
//        for(AnnotationOutputConfig c : outputs) c.verify();
        // todo
    }

}
