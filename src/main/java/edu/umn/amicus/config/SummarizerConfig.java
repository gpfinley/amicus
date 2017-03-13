package edu.umn.amicus.config;

import edu.umn.amicus.AmicusConfigurationException;

import java.util.logging.Logger;

/**
 * Created by greg on 2/10/17.
 */
@Deprecated
public class SummarizerConfig extends PipelineComponentConfig {

    private static final Logger LOGGER = Logger.getLogger(SummarizerConfig.class.getName());

    public String name;
    public AnnotationInputConfig input;
    public String summaryWriter;
    // if null, print to standard out
    public String outPath;

    public void verify() {
        try {
            input.verify();
        } catch (AmicusConfigurationException e) {
            LOGGER.severe("Input configuration incomplete for CollectionSummarizer " + name);
            throw e;
        }
        // todo. Has to have a name. macroSummarizer should be a real class
    }

}
