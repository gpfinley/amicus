package edu.umn.amicus.createconfig;

import edu.umn.amicus.Amicus;
import edu.umn.amicus.config.*;
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

        config.pipelineName = "Example Amicus pipeline";

        config.allSystemsUsed = new SourceSystemConfig[] {
                new SourceSystemConfig().useSystemName("biomedicus")
                        .useDataPath("data/biomedicus")
                        .useReadFromView("SystemView")
                        .useSaveIntoView("BiomedicusView"),
                new SourceSystemConfig().useSystemName("ctakes")
                        .useDataPath("data/ctakes")
                        .useReadFromView("_InitialView")
                        .useSaveIntoView("CtakesView"),
                new SourceSystemConfig().useSystemName("clamp")
                        .useDataPath("data/clamp")
                        .useReadFromView("_InitialView")
                        .useSaveIntoView("ClampView")
        };

        MergerConfig ctakesPreprocessorBean = new MergerConfig();

        ctakesPreprocessorBean.name = "cTAKES preprocessor";
        ctakesPreprocessorBean.inputs = new AnnotationInputConfig[] {
                new AnnotationInputConfig().annotationType("org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation")
                        .annotationField("ontologyConceptArr")
                        .pullerClass("edu.umn.amicus.pullers.CtakesCuiPuller")
                        .fromView("ctakes")
        };

        ctakesPreprocessorBean.outputs = new AnnotationOutputConfig[] {
                new AnnotationOutputConfig().annotationType("edu.umn.amicus.SingleFieldAnnotation")
                        .annotationField("field")
                        .writeView("CtakesView")
        };

        MergerConfig acronymMergerBean = new MergerConfig();
        acronymMergerBean.name = "Acronym Merger";
        acronymMergerBean.alignerClass = "edu.umn.amicus.aligners.PerfectOverlapAligner";
        acronymMergerBean.inputs = new AnnotationInputConfig[]{
                new AnnotationInputConfig().fromView("BiomedicusView")
                        .annotationType("edu.umn.biomedicus.uima.type1_6.Acronym")
                        .annotationField("text")
                        .pullerClass("edu.umn.amicus.pullers.GetterPuller"),
                new AnnotationInputConfig().fromView("CtakesView")
                        .annotationType("org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation")
                        .annotationField("ontologyConceptArr")
                        .pullerClass("edu.umn.amicus.pullers.CuiConceptPuller"),
                new AnnotationInputConfig().fromView("ClampView")
                        .annotationType("edu.uth.clamp.nlp.typesystem.ClampNameEntityUIMA")
                        .annotationField("cui")
                        .pullerClass("edu.umn.amicus.pullers.CuiConceptPuller"),
        };
        acronymMergerBean.outputs = new AnnotationOutputConfig[]{
                new AnnotationOutputConfig().annotationType("edu.umn.biomedicus.uima.type1_6.Acronym")
                        .annotationField("text")
                        .distillerClass("edu.umn.amicus.distillers.PriorityDistiller")
                        .pusherClass("edu.umn.amicus.pushers.SetterPusher")
                        .writeView(Amicus.DEFAULT_MERGED_VIEW)
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
