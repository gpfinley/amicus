package edu.umn.ensembles.uimafit;

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

    // todo: code this up for command-line use

    public EnsemblesPipeline() throws IOException {

        String path = "example_pipeline_config.yml";

        // todo: read configuration details from a properties file? Or some kind of script? YAML?
        // todo: implement pipeline in uimaFIT

        PipelineConfigBean pipelineConfig;

        Yaml yaml = new Yaml();
        pipelineConfig = (PipelineConfigBean) yaml.load(new FileInputStream(path));

        CollectionReader reader;
        List<AnalysisEngine> engines = new ArrayList<>();
        try {
            reader = CollectionReaderFactory.createReader(CommonFilenameReader.class,
                    CommonFilenameReader.SYSTEM_DATA_DIRS, pipelineConfig.getInputDirectories());

            for (MergerConfigBean mergerConfig : pipelineConfig.getTranslatorConfigurations()) {
                engines.add(
                        AnalysisEngineFactory.createEngine(MergerTranslator.class,
                                MergerTranslator.ALIGNER_CLASS, mergerConfig.getAlignerClass(),
                                MergerTranslator.CREATOR_CLASS, mergerConfig.getCreatorClass(),
                                MergerTranslator.DISTILLER_CLASS, mergerConfig.getDistillerClass(),
                                MergerTranslator.OUTPUT_ANNOTATION_TYPE, mergerConfig.getOutputAnnotationClass(),
                                MergerTranslator.OUTPUT_ANNOTATION_FIELDS, mergerConfig.getOutputAnnotationFields(),
                                MergerTranslator.OUTPUT_VIEW_NAME, mergerConfig.getOutputViewName(),
                                MergerTranslator.SYSTEM_NAMES, mergerConfig.getInputSystemNames(),
                                MergerTranslator.TYPE_CLASSES, mergerConfig.getInputTypes(),
                                MergerTranslator.FIELD_NAMES, mergerConfig.getInputFields(),
                                MergerTranslator.TRANSFORMER_CLASSES, mergerConfig.getInputTransformers())
                );
            }

            engines.add(AnalysisEngineFactory.createEngine(XmiWriter.class,
                    XmiWriter.CONFIG_OUTPUT_DIR, pipelineConfig.getOutPath()));

        } catch (ResourceInitializationException e) {
            e.printStackTrace();
            throw new EnsemblesException();
        }
        try {
            SimplePipeline.runPipeline(reader,
                    engines.toArray(new AnalysisEngine[engines.size()]));
        } catch (IOException | UIMAException e) {
            //todo log
            throw new EnsemblesException(e);
        }

        /*
         * CommonFilenameReader
         *
         * CasAdder
         * ...
         * CasAdder (one per system)
         *
         * MergerTranslator
         * ...
         * MergerTranslator (one per annotation to merge or translate)
         *
         * XmiWriter
         */

    }

    public static void main(String[] args) throws IOException {
        new EnsemblesPipeline();
    }

}
