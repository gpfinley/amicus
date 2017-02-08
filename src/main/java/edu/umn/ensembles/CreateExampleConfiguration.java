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


    public static void main(String[] args) throws IOException {

        String outYml = "example_pipeline_config.yml";

        EnsemblesPipelineConfiguration config = new EnsemblesPipelineConfiguration();

        config._pipelineName = "Example Ensembles pipeline";

        config.allSystemsUsed = new SingleSystemConfig[] {
                new SingleSystemConfig().useSystemName("biomedicus")
                        .useDataPath("data/biomedicus")
                        .useReadFromView("SystemView")
                        .useSaveIntoView("BiomedicusView"),
                new SingleSystemConfig().useSystemName("ctakes")
                        .useDataPath("data/biomedicus")
                        .useReadFromView("_InitialView")
                        .useSaveIntoView("CtakesView"),
                new SingleSystemConfig().useSystemName("clamp")
                        .useDataPath("data/biomedicus")
                        .useReadFromView("_InitialView")
                        .useSaveIntoView("ClampView")
        };

        SingleMergerConfiguration ctakesPreprocessorBean = new SingleMergerConfiguration();

        ctakesPreprocessorBean._mergerName = "cTAKES preprocessor";
        ctakesPreprocessorBean.inputs = new SingleInputConfig[] {
                new SingleInputConfig().annotationType("org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation")
                        .annotationField("ontologyConceptArr")
                        .transformerClass("edu.umn.ensembles.transformers.CtakesCuiTransformer")
                        .fromView("ctakes")
        };

        ctakesPreprocessorBean.outputs = new SingleOutputConfig[] {
                new SingleOutputConfig().annotationType("edu.umn.ensembles.SingleFieldAnnotation")
                        .annotationField("field")
                        .writeView("CtakesView")
        };

        SingleMergerConfiguration acronymMergerBean = new SingleMergerConfiguration();
        acronymMergerBean._mergerName = "Acronym Merger";
        acronymMergerBean.alignerClass = "edu.umn.ensembles.aligners.PerfectOverlapAligner";
        acronymMergerBean.inputs = new SingleInputConfig[]{
                new SingleInputConfig().fromView("BiomedicusView")
                                        .annotationType("edu.umn.biomedicus.uima.type1_6.Acronym")
                                        .annotationField("text")
                                        .transformerClass("edu.umn.ensembles.transformers.GetterTransformer"),
                new SingleInputConfig().fromView("CtakesView")
                        .annotationType("org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation")
                        .annotationField("ontologyConceptArr")
                        .transformerClass("edu.umn.ensembles.transformers.CuiConceptTransformer"),
                new SingleInputConfig().fromView("ClampView")
                        .annotationType("edu.uth.clamp.nlp.typesystem.ClampNameEntityUIMA")
                        .annotationField("cui")
                        .transformerClass("edu.umn.ensembles.transformers.CuiConceptTransformer"),
        };
        acronymMergerBean.outputs = new SingleOutputConfig[]{
                new SingleOutputConfig().annotationType("edu.umn.biomedicus.uima.type1_6.Acronym")
                        .annotationField("text")
                        .distillerClass("edu.umn.ensembles.distillers.PriorityDistiller")
                        .creatorClass("edu.umn.ensembles.creators.SimpleCreator")
                        .writeView(Ensembles.DEFAULT_MERGED_VIEW)
        };


        SingleMergerConfiguration[] beans = {ctakesPreprocessorBean, acronymMergerBean};

        config.mergerConfigurations = beans;

        config.xmiOutPath = "data/xmi_out";

        config.verify();
        Yaml yaml = new Yaml();
        yaml.dump(config, new FileWriter(outYml));
        EnsemblesPipelineConfiguration pipeTest = (EnsemblesPipelineConfiguration) yaml.load(new FileInputStream(outYml));

    }
}
