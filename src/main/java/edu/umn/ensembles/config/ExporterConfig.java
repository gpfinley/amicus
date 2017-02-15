package edu.umn.ensembles.config;

import edu.umn.ensembles.Ensembles;

/**
 * todo: doc
 * Serializable by YAML.
 * Created by greg on 2/10/17.
 */
public class ExporterConfig extends PipelineComponentConfig {

    public String _exporterName = "untitled exporter";
    public AnnotationInputConfig[] inputs;
    public String alignerClass = Ensembles.DEFAULT_ALIGNER_CLASS_FOR_EXPORTER.getName();
    public String exporterClass = Ensembles.DEFAULT_EXPORTER_CLASS.getName();
    public String outputDirectory;

    @Override
    public void verify() {

    }

}
