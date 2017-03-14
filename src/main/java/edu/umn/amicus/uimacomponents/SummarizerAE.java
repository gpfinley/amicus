//package edu.umn.amicus.uimacomponents;
//
//import edu.umn.amicus.AmicusException;
//import edu.umn.amicus.AnalysisPieceFactory;
//import edu.umn.amicus.summary.DataListener;
//import edu.umn.amicus.summary.CollectionSummarizer;
//import edu.umn.amicus.pullers.Puller;
//import org.apache.uima.UimaContext;
//import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
//import org.apache.uima.cas.CAS;
//import org.apache.uima.cas.CASException;
//import org.apache.uima.fit.component.CasAnnotator_ImplBase;
//import org.apache.uima.fit.descriptor.ConfigurationParameter;
//import org.apache.uima.jcas.JCas;
//import org.apache.uima.jcas.tcas.Annotation;
//import org.apache.uima.resource.ResourceInitializationException;
//
//import java.io.*;
//import java.util.logging.Logger;
//
///**
// * Simple CAS consumer to print basic statistics for evaluation.
// *
// * Created by gpfinley on 2/3/17.
// */
//@Deprecated
//public class SummarizerAE extends CasAnnotator_ImplBase {
//
//    private static final Logger LOGGER = Logger.getLogger(SummarizerAE.class.getName());
//
//    public static final String MY_NAME = "name";
//    public static final String READ_VIEW = "readView";
//    public static final String INPUT_TYPE = "typeClass";
//    public static final String INPUT_FIELD = "fieldName";
//    public static final String PULLER_CLASS = "puller";
//    public static final String LISTENER_NAME = "listenerName";
//    public static final String COLLECTION_SUMMARIZER_CLASS = "summaryWriterClassName";
//    public static final String OUTPUT_PATH = "outputPath";
//
//    @ConfigurationParameter(name = MY_NAME, defaultValue = "Unnamed CollectionSummarizer")
//    private String myName;
//
//    @ConfigurationParameter(name = READ_VIEW)
//    private String readViewName;
//    @ConfigurationParameter(name = INPUT_TYPE, mandatory = false)
//    private String typeClassName;
//    @ConfigurationParameter(name = INPUT_FIELD, mandatory = false)
//    private String fieldName;
//    @ConfigurationParameter(name = PULLER_CLASS, mandatory = false)
//    private String pullerClassName;
//    @ConfigurationParameter(name = LISTENER_NAME)
//    private String listenerName;
//    @ConfigurationParameter(name = COLLECTION_SUMMARIZER_CLASS)
//    private String summaryWriterClassName;
//    @ConfigurationParameter(name = OUTPUT_PATH, mandatory = false)
//    private String outputPath;
//
//    private Class<? extends Annotation> typeClass;
//    private Puller puller;
//    private DataListener listener;
//
//    @Override
//    public void initialize(UimaContext context) throws ResourceInitializationException {
//        super.initialize(context);
//        try {
//            typeClass = Util.getTypeClass(typeClassName);
//        } catch (AmicusException e) {
//            LOGGER.severe(String.format("Could not find input type \"%s\" for CollectionSummarizer \"%s\". Confirm that types are" +
//                    "correct and that classes have been generated from a UIMA type system (easiest way is to build" +
//                    "via maven).", typeClassName, myName));
//            throw new ResourceInitializationException(e);
//        }
//        listener = DataListener.getDataListener(listenerName);
//        try {
//            puller = AnalysisPieceFactory.puller(pullerClassName, fieldName);
//        } catch(AmicusException e) {
//            LOGGER.severe(String.format("Could not instantiate puller %s for CollectionSummarizer \"%s\"", pullerClassName, myName));
//            throw new ResourceInitializationException(e);
//        }
//    }
//
//    @Override
//    public void process(CAS cas) throws AnalysisEngineProcessException {
//        JCas readView;
//        try {
//            readView = cas.getView(readViewName).getJCas();
//        } catch (CASException e) {
//            throw new AnalysisEngineProcessException(e);
//        }
//
//        // Get all annotations of this class and toss them to the DataListener
//        for (Annotation a : readView.getAnnotationIndex(typeClass)) {
//            try {
//                listener.listen(puller.pull(a));
//            } catch (AmicusException e) {
//                LOGGER.severe(String.format("Problem pulling annotation in CollectionSummarizer \"%s\"", myName));
//                throw new AnalysisEngineProcessException(e);
//            }
//        }
//    }
//
//    @Override
//    public void collectionProcessComplete() throws AnalysisEngineProcessException {
//        CollectionSummarizer collectionSummarizer;
//        try {
//            collectionSummarizer = AnalysisPieceFactory.collectionSummarizer(summaryWriterClassName);
//        } catch (AmicusException e) {
//            LOGGER.severe(String.format("Problem instantiating CollectionSummarizer \"%s\" in CollectionSummarizer \"%s\"",
//                    summaryWriterClassName, myName));
//            throw new AnalysisEngineProcessException(e);
//        }
//        try {
//            OutputStream outputStream;
//            if (outputPath == null) {
//                System.out.println("***** Summary for Collector " + listenerName + ":\n");
//                outputStream = System.out;
//                outputPath = "standard out";
//            } else {
//                outputStream = new FileOutputStream(outputPath);
//            }
//            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
//            try {
//                writer.write(collectionSummarizer.summarizeCollection(DataListener.getDataListener(listenerName).regurgitate(), null).toString());
//            } catch (AmicusException e) {
//                throw  new AnalysisEngineProcessException(e);
//            }
//            writer.flush();
//            writer.close();
//            LOGGER.info("Saved summary to " + outputPath);
//        } catch (IOException e) {
//            throw new AnalysisEngineProcessException(e);
//        }
//    }
//
//}