package edu.umn.ensembles;

import edu.umn.ensembles.config.SingleInputConfig;
import edu.umn.ensembles.config.MergerConfigBean;
import edu.umn.ensembles.config.PipelineConfigBean;
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
        PipelineConfigBean config = new PipelineConfigBean();

        config.setOutPath("data/example_out");
        config.setInputNames(Arrays.asList(
                "biomedicus",
                "clamp",
                "ctakes"
        ));
        config.setInputViews(Arrays.asList(
                "SystemView",
                "_InitialView",
                "_InitialView"
        ));
        config.setInputDirectories(Arrays.asList(
                "data/systemsData/biomedicus",
                "data/systemsData/clamp",
                "data/systemsData/ctakes"
        ));

        MergerConfigBean bean = new MergerConfigBean();

        bean.set_name("Acronym Merger");
        bean.setAlignerClass("edu.umn.ensembles.aligners.PerfectOverlapAligner");
        bean.setDistillerClass("edu.umn.ensembles.distillers.PriorityDistiller");
        bean.setCreatorClass("edu.umn.ensembles.creators.SimpleCreator");
        bean.setOutputViewName(Ensembles.DEFAULT_MERGED_VIEW);
        bean.setOutputAnnotationClass("edu.umn.biomedicus.uima.type1_6.Acronym");
        bean.setOutputAnnotationFields("text");

        SingleInputConfig biomedicusAcronymInput = new SingleInputConfig();
        SingleInputConfig ctakesAcronymInput = new SingleInputConfig();
        SingleInputConfig clampAcronymInput = new SingleInputConfig();

        biomedicusAcronymInput.setFromSystem("biomedicus");
        biomedicusAcronymInput.setType("edu.umn.biomedicus.uima.type1_6.Acronym");
        biomedicusAcronymInput.setField("text");
        biomedicusAcronymInput.setTransformerClass("edu.umn.ensembles.transformers.GetterTransformer");

        ctakesAcronymInput.setFromSystem("ctakes");
        ctakesAcronymInput.setType("org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation");
        ctakesAcronymInput.setField("ontologyConceptArr");
        ctakesAcronymInput.setTransformerClass("edu.umn.ensembles.transformers.CtakesConceptTransformer");

        clampAcronymInput.setFromSystem("clamp");
        clampAcronymInput.setType("edu.uth.clamp.nlp.typesystem.ClampNameEntityUIMA");
        clampAcronymInput.setField("cui");
        clampAcronymInput.setTransformerClass("edu.umn.ensembles.transformers.CuiConceptTransformer");

        SingleInputConfig[] inputs = {biomedicusAcronymInput, ctakesAcronymInput, clampAcronymInput};
        bean.setInputs(inputs);
        bean.setOutputViewName("MergedView");
        List<MergerConfigBean> beans = new ArrayList<>();
        beans.add(bean);

        config.setMergerConfigurations(beans);

        config.verify();
        Yaml yaml = new Yaml();
        String outYml = "example_pipeline_config.yml";
        yaml.dump(config, new FileWriter(outYml));

        PipelineConfigBean pipeTest = (PipelineConfigBean) yaml.load(new FileInputStream(outYml));

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
//        MergerConfigBean bean = new MergerConfigBean();
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
//        List<MergerConfigBean> beans = new ArrayList<>();
//        bean.setOutputViewName("MergedView");
//        beans.add(bean);
//
//        eval.setMergerConfigurations(beans);
//
//        eval.save(new FileOutputStream("evalConfiguration.yml"));
//    }
}
