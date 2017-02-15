package edu.umn.ensembles;

import edu.umn.ensembles.config.*;
import edu.umn.ensembles.uimacomponents.*;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
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

        AmicusPipelineConfiguration pipelineConfig;

        Yaml yaml = new Yaml();
        pipelineConfig = (AmicusPipelineConfiguration) yaml.load(new FileInputStream(configFilePath));

        // todo: either delete or modify in case we don't want to use type system autodetection
//        // needed for collection reader
//        TypeSystemDescription ensemblesTypeSystem =
//                TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath(Ensembles.MY_TYPE_SYSTEM.toString());

        CollectionReader reader;
        List<AnalysisEngine> engines = new ArrayList<>();
        reader = CollectionReaderFactory.createReader(CommonFilenameCR.class,
//                ensemblesTypeSystem,
                TypeSystemDescriptionFactory.createTypeSystemDescription(),
                CommonFilenameCR.SYSTEM_DATA_DIRS, pipelineConfig.aggregateInputDirectories());

        for (SourceSystemConfig systemConfig : pipelineConfig.allSystemsUsed) {
            engines.add(AnalysisEngineFactory.createEngine(CasAdderAE.class,
                    CasAdderAE.DATA_DIR, systemConfig.dataPath,
                    CasAdderAE.READ_FROM_VIEW, systemConfig.readFromView,
                    CasAdderAE.COPY_INTO_VIEW, systemConfig.saveIntoView
                    ));
        }

        //todo: delete
//        // keep track of summarizers while building pipeline components.
//        // Keep their names to retrieve the relevant DataLoaders.
//        // Traverse in the same order the components were added in.
//        LinkedHashMap<String, Summarizer> summarizersToRun = new LinkedHashMap<>();

        for (PipelineComponentConfig componentConfig : pipelineConfig.pipelineComponents) {
            if (componentConfig.getClass().equals(MergerConfig.class)) {
                MergerConfig mergerConfig = (MergerConfig) componentConfig;
                engines.add(
                        AnalysisEngineFactory.createEngine(MergerAE.class,
                                MergerAE.READ_VIEWS, mergerConfig.aggregateInputSystemNames(),
                                MergerAE.TYPE_CLASSES, mergerConfig.aggregateInputTypes(),
                                MergerAE.FIELD_NAMES, mergerConfig.aggregateInputFields(),
                                MergerAE.PULLER_CLASSES, mergerConfig.aggregateInputPullers(),
                                MergerAE.ALIGNER_CLASS, mergerConfig.alignerClass,
                                MergerAE.DISTILLER_CLASSES, mergerConfig.aggregateOutputDistillers(),
                                MergerAE.OUTPUT_ANNOTATION_TYPES, mergerConfig.aggregateOutputAnnotationClasses(),
                                MergerAE.OUTPUT_ANNOTATION_FIELDS, mergerConfig.aggregateOutputAnnotationFields(),
                                MergerAE.PUSHER_CLASSES, mergerConfig.aggregateOutputPushers(),
                                MergerAE.OUTPUT_VIEW_NAMES, mergerConfig.aggregateOutputViewNames()
                        ));
            } else if(componentConfig.getClass().equals(CollectorConfig.class)) {
                CollectorConfig collectorConfig = (CollectorConfig) componentConfig;
                engines.add(
                        AnalysisEngineFactory.createEngine(CollectorAE.class,
                                CollectorAE.TYPE_CLASS, collectorConfig.input.annotationType,
                                CollectorAE.FIELD_NAME, collectorConfig.input.annotationField,
                                CollectorAE.READ_VIEW, collectorConfig.input.fromView,
                                CollectorAE.PULLER_CLASS, collectorConfig.input.pullerClass,
                                CollectorAE.LISTENER_NAME, collectorConfig.name
                        ));
                // todo: delete
//                try {
//                    Constructor<? extends Summarizer> summarizerConstructor = collectorConfig.summarizer.getConstructor();
//                    summarizersToRun.put(collectorConfig.name, summarizerConstructor.newInstance());
//                } catch (ReflectiveOperationException e) {
//                    throw new EnsemblesException(e);
//                }
            } else if(componentConfig.getClass().equals(ExporterConfig.class)) {
                ExporterConfig exporterConfig = (ExporterConfig) componentConfig;
                // todo: instantiate AE
            }
        }

        engines.add(AnalysisEngineFactory.createEngine(XmiWriterAE.class,
                XmiWriterAE.CONFIG_OUTPUT_DIR, pipelineConfig.xmiOutPath));

        if (runEvaluationAE) {
            engines.add(AnalysisEngineFactory.createEngine(CollectorAE.class));
        }

        SimplePipeline.runPipeline(reader, engines.toArray(new AnalysisEngine[engines.size()]));

        // todo: delete (doing it in the collectionProcessComplete method, not here)
//        // run named summarizers after pipeline completes
//        for (Map.Entry<String, Summarizer> entry : summarizersToRun.entrySet()) {
//            entry.getValue().summarize(DataListener.getDataListener(entry.getKey()).regurgitate());
//        }

    }

    public static void main(String[] args) throws Exception {
        args = new String[]{"simple_test_config.yml"};
        boolean eval = "-e".equals(args[0]);
        String configFilePath = args[eval ? 1 : 0];
        new EnsemblesPipeline(configFilePath, eval);
    }

}
