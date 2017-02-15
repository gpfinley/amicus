package edu.umn.ensembles;

import edu.umn.ensembles.config.*;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Generates a dummy yaml configuration for the acronym system. Can be further modified with a text editor.
 *
 * Created by gpfinley on 10/24/16.
 *
 */
public class CreateExampleConfiguration {

    // todo: fix this

    public static void main(String[] args) throws IOException {

        String outYml = "example_pipeline_config.yml";

        AmicusPipelineConfiguration config = new AmicusPipelineConfiguration();

        config._pipelineName = "Example Ensembles pipeline";

        config.allSystemsUsed = new SourceSystemConfig[] {
                new SourceSystemConfig().useSystemName("biomedicus")
                        .useDataPath("data/biomedicus")
                        .useReadFromView("SystemView")
                        .useSaveIntoView("BiomedicusView"),
                new SourceSystemConfig().useSystemName("ctakes")
                        .useDataPath("data/biomedicus")
                        .useReadFromView("_InitialView")
                        .useSaveIntoView("CtakesView"),
                new SourceSystemConfig().useSystemName("clamp")
                        .useDataPath("data/biomedicus")
                        .useReadFromView("_InitialView")
                        .useSaveIntoView("ClampView")
        };

        MergerConfig ctakesPreprocessorBean = new MergerConfig();

        ctakesPreprocessorBean._mergerName = "cTAKES preprocessor";
        ctakesPreprocessorBean.inputs = new AnnotationInputConfig[] {
                new AnnotationInputConfig().annotationType("org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation")
                        .annotationField("ontologyConceptArr")
                        .transformerClass("edu.umn.ensembles.pullers.CtakesCuiPuller")
                        .fromView("ctakes")
        };

        ctakesPreprocessorBean.outputs = new AnnotationOutputConfig[] {
                new AnnotationOutputConfig().annotationType("edu.umn.ensembles.SingleFieldAnnotation")
                        .annotationField("field")
                        .writeView("CtakesView")
        };

        MergerConfig acronymMergerBean = new MergerConfig();
        acronymMergerBean._mergerName = "Acronym Merger";
        acronymMergerBean.alignerClass = "edu.umn.ensembles.aligners.PerfectOverlapAligner";
        acronymMergerBean.inputs = new AnnotationInputConfig[]{
                new AnnotationInputConfig().fromView("BiomedicusView")
                                        .annotationType("edu.umn.biomedicus.uima.type1_6.Acronym")
                                        .annotationField("text")
                                        .transformerClass("edu.umn.ensembles.pullers.GetterPuller"),
                new AnnotationInputConfig().fromView("CtakesView")
                        .annotationType("org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation")
                        .annotationField("ontologyConceptArr")
                        .transformerClass("edu.umn.ensembles.pullers.CuiConceptPuller"),
                new AnnotationInputConfig().fromView("ClampView")
                        .annotationType("edu.uth.clamp.nlp.typesystem.ClampNameEntityUIMA")
                        .annotationField("cui")
                        .transformerClass("edu.umn.ensembles.pullers.CuiConceptPuller"),
        };
        acronymMergerBean.outputs = new AnnotationOutputConfig[]{
                new AnnotationOutputConfig().annotationType("edu.umn.biomedicus.uima.type1_6.Acronym")
                        .annotationField("text")
                        .distillerClass("edu.umn.ensembles.distillers.PriorityDistiller")
                        .creatorClass("edu.umn.ensembles.pushers.SimplePusher")
                        .writeView(Ensembles.DEFAULT_MERGED_VIEW)
        };


        PipelineComponentConfig[] beans = {ctakesPreprocessorBean, acronymMergerBean};

        config.pipelineComponents = beans;

        config.xmiOutPath = "data/xmi_out";

        config.verify();
        Yaml yaml = new Yaml();
        yaml.dump(config, new FileWriter(outYml));
        AmicusPipelineConfiguration pipeTest = (AmicusPipelineConfiguration) yaml.load(new FileInputStream(outYml));

    }
}
