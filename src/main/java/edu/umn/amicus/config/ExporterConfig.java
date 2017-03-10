package edu.umn.amicus.config;

import edu.umn.amicus.Amicus;

/**
 * todo: doc
 * Serializable by YAML.
 * Created by greg on 2/10/17.
 */
public class ExporterConfig extends PipelineComponentConfig {

    public String name = "untitled exporter";
    public AnnotationInputConfig[] inputs;
    public String alignerClass = Amicus.DEFAULT_ALIGNER_CLASS_FOR_EXPORTER.getName();
    public String exporterClass = Amicus.DEFAULT_EXPORTER_CLASS.getName();
    public String outputDirectory;

    @Override
    public void verify() {

    }

}
