package edu.umn.ensembles;

import edu.umn.ensembles.internal.I2b2MarkablesReader;
import edu.umn.ensembles.internal.StanfordNerInterceptor;
import edu.umn.ensembles.uimafit.XmiWriter;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gpfinley on 1/20/17.
 */
public class Test {
    public static void main(String[] args) {


        String textDir = "/Users/gpfinley/i2b2_past/2011/Beth_Train/docs";
        String markablesDir = "/Users/gpfinley/i2b2_past/2011/Beth_Train/concepts";
//        String xmiOut = "data/xmiOutTest";

        CollectionReader reader;
        List<AnalysisEngine> engines = new ArrayList<>();
        try {
            TypeSystemDescription typeSystem = TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath("typeSystems/EnsemblesTypeSystem.xml");
            reader = CollectionReaderFactory.createReader(I2b2MarkablesReader.class,
                    typeSystem,
                    I2b2MarkablesReader.TEXT_DIRECTORY, textDir,
                    I2b2MarkablesReader.MARKABLES_DIRECTORY, markablesDir);

            engines.add(AnalysisEngineFactory.createEngine(StanfordNerInterceptor.class));

//            engines.add(AnalysisEngineFactory.createEngine(XmiWriter.class,
//                    XmiWriter.CONFIG_OUTPUT_DIR, xmiOut));

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


    }
}
