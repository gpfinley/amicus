package edu.umn.amicus.config;

/**
 * Created by greg on 2/10/17.
 */
public class SummarizerConfig extends PipelineComponentConfig {

    public String name;
    public AnnotationInputConfig input;
    public String summarizerClass;
    // if null, print to standard out
    public String outPath;

    public void verify() {
        // todo. Has to have a name. summarizerClass should be a real class
    }

}
