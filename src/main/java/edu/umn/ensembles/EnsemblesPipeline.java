package edu.umn.ensembles;

import edu.umn.ensembles.config.EnsemblesPipelineConfiguration;
import edu.umn.ensembles.config.SingleMergerConfiguration;
import edu.umn.ensembles.config.SingleSystemConfig;
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
        reader = CollectionReaderFactory.createReader(CommonFilenameCR.class,
//                ensemblesTypeSystem,
                TypeSystemDescriptionFactory.createTypeSystemDescription(),
                CommonFilenameCR.SYSTEM_DATA_DIRS, pipelineConfig.aggregateInputDirectories());

        for (SingleSystemConfig systemConfig : pipelineConfig.allSystemsUsed) {
            engines.add(AnalysisEngineFactory.createEngine(CasAdderAE.class,
                    CasAdderAE.DATA_DIR, systemConfig.dataPath,
                    CasAdderAE.READ_FROM_VIEW, systemConfig.readFromView,
                    CasAdderAE.COPY_INTO_VIEW, systemConfig.saveIntoView
                    ));
        }

        for (SingleMergerConfiguration mergerConfig : pipelineConfig.mergerConfigurations) {
            engines.add(
                    AnalysisEngineFactory.createEngine(MergerAE.class,
                            MergerAE.READ_VIEWS, mergerConfig.aggregateInputSystemNames(),
                            MergerAE.TYPE_CLASSES, mergerConfig.aggregateInputTypes(),
                            MergerAE.FIELD_NAMES, mergerConfig.aggregateInputFields(),
                            MergerAE.PULLER_CLASSES, mergerConfig.aggregateInputTransformers(),
                            MergerAE.ALIGNER_CLASS, mergerConfig.alignerClass,
                            MergerAE.DISTILLER_CLASSES, mergerConfig.aggregateDistillerClasses(),
                            MergerAE.OUTPUT_ANNOTATION_TYPES, mergerConfig.aggregateOutputAnnotationClasses(),
                            MergerAE.OUTPUT_ANNOTATION_FIELDS, mergerConfig.aggregateOutputAnnotationFields(),
                            MergerAE.PUSHER_CLASSES, mergerConfig.aggregateCreatorClasses(),
                            MergerAE.OUTPUT_VIEW_NAMES, mergerConfig.aggregateOutputViewNames()
            ));
        }
//        AnalysisEngine engine = AnalysisEngineFactory.createEngine();
//        engine.typeSystemInit(ensemblesTypeSystem);

        engines.add(AnalysisEngineFactory.createEngine(XmiWriterAE.class,
                XmiWriterAE.CONFIG_OUTPUT_DIR, pipelineConfig.xmiOutPath));

        if (runEvaluationAE) {
            engines.add(AnalysisEngineFactory.createEngine(EvalSummarizerAE.class));
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
