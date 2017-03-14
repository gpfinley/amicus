package edu.umn.amicus.config;

/**
 * todo: doc
 * Serializable by YAML.
 * Created by greg on 2/10/17.
 */
public class ExporterConfig extends PipelineComponentConfig {

    public String name = "untitled exporter";
    public AnnotationInputConfig[] inputs;

    public String aligner;

    public String documentSummarizer;
    public String documentSummaryOutDir;

    public String collectionSummarizer;
    public String collectionSummaryOutFile;

    @Override
    public void verify() {
        // todo

    }

}
