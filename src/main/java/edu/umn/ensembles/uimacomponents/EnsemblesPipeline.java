package edu.umn.ensembles.uimacomponents;

import edu.umn.ensembles.EnsemblesException;
import edu.umn.ensembles.config.MergerConfigBean;
import edu.umn.ensembles.config.PipelineConfigBean;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gpfinley on 1/20/17.
 */
public class EnsemblesPipeline {

    public EnsemblesPipeline(String configFilePath, boolean runEvaluationAE) throws IOException {

        PipelineConfigBean pipelineConfig;

        Yaml yaml = new Yaml();
        pipelineConfig = (PipelineConfigBean) yaml.load(new FileInputStream(configFilePath));

        CollectionReader reader;
        List<AnalysisEngine> engines = new ArrayList<>();
        try {
            reader = CollectionReaderFactory.createReader(CommonFilenameReader.class,
                    CommonFilenameReader.SYSTEM_DATA_DIRS, pipelineConfig.getInputDirectories());

            for (MergerConfigBean mergerConfig : pipelineConfig.getMergerConfigurations()) {
                engines.add(
                        AnalysisEngineFactory.createEngine(MergerTranslator.class,
                                MergerTranslator.SYSTEM_NAMES, mergerConfig.getInputSystemNames(),
                                MergerTranslator.TYPE_CLASSES, mergerConfig.getInputTypes(),
                                MergerTranslator.FIELD_NAMES, mergerConfig.getInputFields(),
                                MergerTranslator.TRANSFORMER_CLASSES, mergerConfig.getInputTransformers(),
                                MergerTranslator.ALIGNER_CLASS, mergerConfig.alignerClass,
                                MergerTranslator.DISTILLER_CLASSES, mergerConfig.getDistillerClasses(),
                                MergerTranslator.OUTPUT_ANNOTATION_TYPES, mergerConfig.getOutputAnnotationClasses(),
                                MergerTranslator.OUTPUT_ANNOTATION_FIELDS, mergerConfig.getOutputAnnotationFields(),
                                MergerTranslator.CREATOR_CLASSES, mergerConfig.getCreatorClasses(),
                                MergerTranslator.OUTPUT_VIEW_NAMES, mergerConfig.getOutputViewNames()
                ));
            }

            engines.add(AnalysisEngineFactory.createEngine(XmiWriter.class,
                    XmiWriter.CONFIG_OUTPUT_DIR, pipelineConfig.getOutPath()));

            if (runEvaluationAE) {
                engines.add(AnalysisEngineFactory.createEngine(EvalSummarizer.class));
            }

        } catch (ResourceInitializationException e) {
            throw new EnsemblesException(e);
        }
        try {
            SimplePipeline.runPipeline(reader,
                    engines.toArray(new AnalysisEngine[engines.size()]));
        } catch (IOException | UIMAException e) {
            throw new EnsemblesException(e);
        }

    }

    public static void main(String[] args) throws IOException {
        boolean eval = "-e".equals(args[0]);
        String configFilePath = args[eval ? 1 : 0];
        new EnsemblesPipeline(configFilePath, eval);
    }

}
