package edu.umn.amicus.config;

/**
 * todo: doc
 * Serializable by YAML.
 * Created by greg on 2/10/17.
 */
public class ExporterConfig extends PipelineComponentConfig {

    public String name = "untitled exporter";
    public AnnotationInputConfig[] inputs;
    public String alignerClass;
    public String microSummarizer;
    public String microSummaryOutDirectory;

    public String macroSummarizer;
    public String macroSummaryOutPath;

    @Override
    public void verify() {
        // todo

    }

}
