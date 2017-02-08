package edu.umn.ensembles;

import edu.umn.ensembles.config.*;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Generates a dummy yaml configuration for the acronym system. Can be further modified with a text editor.
 *
 * Created by gpfinley on 10/24/16.
 *
 */
public class CreateExampleConfiguration {

    public static void main(String[] args) throws IOException {
        EnsemblesPipelineConfiguration config = new EnsemblesPipelineConfiguration();

        config.systemsUsed = new SingleSystemConfig[] {
                new SingleSystemConfig().useSystemName("biomedicus")
                        .useDataPath("data/biomedicus")
                        .useReadFromView("SystemView")
                        .useCopyIntoView("BiomedicusView"),
                new SingleSystemConfig().useSystemName("ctakes")
                        .useDataPath("data/biomedicus")
                        .useReadFromView("_InitialView")
                        .useCopyIntoView("CtakesView"),
                new SingleSystemConfig().useSystemName("clamp")
                        .useDataPath("data/biomedicus")
                        .useReadFromView("_InitialView")
                        .useCopyIntoView("ClampView")
        };

        SingleMergerConfiguration ctakesPreprocessorBean = new SingleMergerConfiguration();

        ctakesPreprocessorBean._name = "cTAKES preprocessor";
        ctakesPreprocessorBean.inputs = new SingleInputConfig[] {
                new SingleInputConfig().annotationType("org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation")
                        .annotationField("ontologyConceptArr")
                        .transformerClass("edu.umn.ensembles.transformers.CtakesCuiTransformer")
                        .fromSystem("ctakes")
        };

        ctakesPreprocessorBean.outputs = new SingleOutputConfig[] {
                new SingleOutputConfig().annotationType("edu.umn.ensembles.SingleFieldAnnotation")
                        .annotationField("field")
                        .writeView("CtakesView")
        };

        SingleMergerConfiguration acronymMergerBean = new SingleMergerConfiguration();
        acronymMergerBean._name = "Acronym Merger";
        acronymMergerBean.alignerClass = "edu.umn.ensembles.aligners.PerfectOverlapAligner";
        acronymMergerBean.inputs = new SingleInputConfig[]{
                new SingleInputConfig().fromSystem("biomedicus")
                                        .annotationType("edu.umn.biomedicus.uima.type1_6.Acronym")
                                        .annotationField("text")
                                        .transformerClass("edu.umn.ensembles.transformers.GetterTransformer"),
                new SingleInputConfig().fromSystem("ctakes")
                        .annotationType("org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation")
                        .annotationField("ontologyConceptArr")
                        .transformerClass("edu.umn.ensembles.transformers.CuiConceptTransformer"),
                new SingleInputConfig().fromSystem("clamp")
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

        config.verify();
        Yaml yaml = new Yaml();
        String outYml = "example_pipeline_config.yml";
        yaml.dump(config, new FileWriter(outYml));

        EnsemblesPipelineConfiguration pipeTest = (EnsemblesPipelineConfiguration) yaml.load(new FileInputStream(outYml));

//        config.save(new FileOutputStream("configuration.yml"));

//        evalConfig();

    }
//
//    // todo: update
//    private static void evalConfig() throws IOException {
//        AppConfiguration eval = new AppConfiguration();
//
//        Map<String, String> evalViews = new HashMap<>();
//        evalViews.put("merged", Ensembles.DEFAULT_MERGED_VIEW);
////        evalViews.put("manual", "OriginalDocumentView");
//        evalViews.put("manual", "SystemView");
//        eval.setViewNames(evalViews);
//
//        SingleMergerConfiguration bean = new SingleMergerConfiguration();
//        bean.set_name("Acronym Evaluator");
//        bean.setAlignerClass("edu.umn.ensembles.aligners.PerfectOverlapAligner");
//        SingleInputConfig mergedInput = new SingleInputConfig();
//        SingleInputConfig goldInput = new SingleInputConfig();
//
//        mergedInput.setFromSystem("merged");
//        mergedInput.setType("edu.umn.biomedicus.uima.type1_6.Acronym");
//        mergedInput.setField("text");
//        mergedInput.setTransformerClass("edu.umn.ensembles.transformers.GetterTransformer");
//
//        goldInput.setType("edu.umn.biomedicus.type.TokenAnnotation");
//        goldInput.setField("acronymAbbrevExpansion");
//        goldInput.setFromSystem("manual");
//        goldInput.setTransformerClass("edu.umn.ensembles.transformers.GetterTransformer");
//
//        SingleInputConfig[] inputs = {goldInput, mergedInput};
//        bean.setInputs(inputs);
//        List<SingleMergerConfiguration> beans = new ArrayList<>();
//        bean.setOutputViewName("MergedView");
//        beans.add(bean);
//
//        eval.setMergerConfigurations(beans);
//
//        eval.save(new FileOutputStream("evalConfiguration.yml"));
//    }
}
