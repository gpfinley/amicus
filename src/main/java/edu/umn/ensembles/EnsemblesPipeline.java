package edu.umn.ensembles;

import edu.umn.ensembles.config.EnsemblesPipelineConfiguration;
import edu.umn.ensembles.config.SingleMergerConfiguration;
import edu.umn.ensembles.uimacomponents.*;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Standard pipeline, as configurable by yml file.
 * todo: more doc
 *
 * Created by gpfinley on 1/20/17.
 */
public class EnsemblesPipeline {

    public EnsemblesPipeline(String configFilePath, boolean runEvaluationAE) throws IOException, UIMAException {

        EnsemblesPipelineConfiguration pipelineConfig;

        Yaml yaml = new Yaml();
        pipelineConfig = (EnsemblesPipelineConfiguration) yaml.load(new FileInputStream(configFilePath));

        // needed for collection reader
        TypeSystemDescription ensemblesTypeSystem =
                TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath(Ensembles.MY_TYPE_SYSTEM.toString());

        CollectionReader reader;
        List<AnalysisEngine> engines = new ArrayList<>();
//        try {
        reader = CollectionReaderFactory.createReader(CommonFilenameReader.class,
//                ensemblesTypeSystem,
                TypeSystemDescriptionFactory.createTypeSystemDescription(),
                CommonFilenameReader.SYSTEM_DATA_DIRS, pipelineConfig.getInputDirectories());

        for (int i=0; i<pipelineConfig.getInputNames().size(); i++) {
            engines.add(AnalysisEngineFactory.createEngine(CasAdder.class,
                    CasAdder.DATA_DIR, pipelineConfig.getInputDirectories().get(i),
                    CasAdder.SYSTEM_NAME, pipelineConfig.getInputNames().get(i),
                    CasAdder.VIEW_NAME, pipelineConfig.getInputViews().get(i)
                    ));
        }

        for (SingleMergerConfiguration mergerConfig : pipelineConfig.getMergerConfigurations()) {
            engines.add(
                    AnalysisEngineFactory.createEngine(MergerTranslator.class,
                            MergerTranslator.SYSTEM_NAMES, mergerConfig.aggregateInputSystemNames(),
                            MergerTranslator.TYPE_CLASSES, mergerConfig.aggregateInputTypes(),
                            MergerTranslator.FIELD_NAMES, mergerConfig.aggregateInputFields(),
                            MergerTranslator.TRANSFORMER_CLASSES, mergerConfig.aggregateInputTransformers(),
                            MergerTranslator.ALIGNER_CLASS, mergerConfig.alignerClass,
                            MergerTranslator.DISTILLER_CLASSES, mergerConfig.aggregateDistillerClasses(),
                            MergerTranslator.OUTPUT_ANNOTATION_TYPES, mergerConfig.aggregateOutputAnnotationClasses(),
                            MergerTranslator.OUTPUT_ANNOTATION_FIELDS, mergerConfig.aggregateOutputAnnotationFields(),
                            MergerTranslator.CREATOR_CLASSES, mergerConfig.aggregateCreatorClasses(),
                            MergerTranslator.OUTPUT_VIEW_NAMES, mergerConfig.aggregateOutputViewNames()
            ));
        }
//        AnalysisEngine engine = AnalysisEngineFactory.createEngine();
//        engine.typeSystemInit(ensemblesTypeSystem);

        engines.add(AnalysisEngineFactory.createEngine(XmiWriter.class,
                XmiWriter.CONFIG_OUTPUT_DIR, pipelineConfig.getOutPath()));

        if (runEvaluationAE) {
            engines.add(AnalysisEngineFactory.createEngine(EvalSummarizer.class));
        }

//        } catch (ResourceInitializationException e) {
//            throw new EnsemblesException(e);
//        }
//        try {
        // have to wrap the analysis engines in an array
        SimplePipeline.runPipeline(reader, engines.toArray(new AnalysisEngine[engines.size()]));
//        } catch (IOException | UIMAException e) {
//            throw new EnsemblesException(e);
//        }

    }

    public static void main(String[] args) throws Exception {
        args = new String[]{"simple_test_config.yml"};
        boolean eval = "-e".equals(args[0]);
        String configFilePath = args[eval ? 1 : 0];
        new EnsemblesPipeline(configFilePath, eval);
    }

}
