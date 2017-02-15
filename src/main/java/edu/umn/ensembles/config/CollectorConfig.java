package edu.umn.ensembles.config;

/**
 * Created by greg on 2/10/17.
 */
public class CollectorConfig extends PipelineComponentConfig {

    public String name;
    public AnnotationInputConfig input;
//    public Class<? extends Summarizer> summarizer;
    public String summarizerClass;

    public void verify() {
        // todo
    }

}
