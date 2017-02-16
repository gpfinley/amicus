package edu.umn.amicus.coreference;

import edu.umn.amicus.Amicus;
import edu.umn.amicus.AmicusException;
import edu.umn.amicus.uimacomponents.XmiWriterAE;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
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
 * Created by gpfinley on 1/27/17.
 */
public class NerMain {

    public static void main(String[] args) {
        // todo: parse args
//        String textDir = "/Users/gpfinley/i2b2_past/2011/Beth_Train/docs";
//        String markablesDir = "/Users/gpfinley/i2b2_past/2011/Beth_Train/concepts";
//        String xmiOut = "data/xmiOutTest";
//        String textOut = "data/stanfordTextOut";

        String textDir = "/Users/gpfinley/corefEvalTest/docs";
        String markablesDir = "/Users/gpfinley/corefEvalTest/mentions";
        String xmiOut = "/Users/gpfinley/corefEvalTest/xmi";
        String textOut = "/Users/gpfinley/corefEvalTest/hyp_no_ner";
//        boolean ignoreNer = false;
        boolean ignoreNer = true;

        CollectionReader reader;
        List<AnalysisEngine> engines = new ArrayList<>();
        try {
            TypeSystemDescription typeSystem = TypeSystemDescriptionFactory.createTypeSystemDescriptionFromPath(Amicus.MY_TYPE_SYSTEM.toString());
            reader = CollectionReaderFactory.createReader(I2b2MarkablesReader.class,
                    typeSystem,
                    I2b2MarkablesReader.TEXT_DIRECTORY, textDir,
                    I2b2MarkablesReader.MARKABLES_DIRECTORY, markablesDir);

            engines.add(AnalysisEngineFactory.createEngine(StanfordNerInterceptor.class,
//                    StanfordNerInterceptor.CONFIG_VIEW_NAME, "_InitialView",
//                    StanfordNerInterceptor.CONFIG_ANNOTATION_CLASS, "edu.umn.amicus.SingleFieldAnnotation",
//                    StanfordNerInterceptor.CONFIG_ANNOTATION_FIELD, "field",
                    StanfordNerInterceptor.TEXT_OUTPUT_PATH, textOut,
                    StanfordNerInterceptor.IGNORE_NER, ignoreNer));

            engines.add(AnalysisEngineFactory.createEngine(XmiWriterAE.class,
                    XmiWriterAE.CONFIG_OUTPUT_DIR, xmiOut,
                    XmiWriterAE.TYPE_SYSTEM_VIEW, "_InitialView"));

        } catch (ResourceInitializationException e) {
            e.printStackTrace();
            throw new AmicusException();
        }
        try {
            SimplePipeline.runPipeline(reader,
                    engines.toArray(new AnalysisEngine[engines.size()]));
        } catch (IOException | UIMAException e) {
            throw new AmicusException(e);
        }
    }
}
