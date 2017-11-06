package edu.umn.amicus.uimacomponents;

import edu.umn.amicus.*;
import edu.umn.amicus.aligners.Aligner;
import edu.umn.amicus.summary.DocumentSummarizer;
import edu.umn.amicus.pullers.Puller;
import edu.umn.amicus.summary.CollectionSummarizer;
import edu.umn.amicus.util.ANA;
import edu.umn.amicus.util.AlignedTuple;
import edu.umn.amicus.util.AnalysisPieceFactory;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * todo: doc, test
 * todo: test exportWriting, specifically
 * Created by greg on 2/11/17.
 */
public class ExporterAE extends JCasAnnotator_ImplBase {

    private static final Logger LOGGER = Logger.getLogger(ExporterAE.class.getName());

    public static final String MY_NAME = "name";
    public static final String READ_VIEWS = "readViews";
    public static final String INPUT_TYPES = "typeClasses";
    public static final String INPUT_FIELDS = "fieldNames";
    public static final String PULLER_CLASSES = "pullerClasses";

    public static final String ALIGNER_CLASS = "alignerClass";
    public static final String DOC_SUMMARIZER_CLASS = "documentSummarizerClassName";
    public static final String OUTPUT_DIRECTORY = "documentSummaryOutDir";

    public static final String COLLECTION_SUMMARIZER_CLASS = "collectionSummarizerClassName";
    public static final String SUMMARY_OUTPUT_PATH = "collectionSummaryOutFile";

    @ConfigurationParameter(name = MY_NAME, defaultValue = "Unnamed Exporter")
    private String myName;

    @ConfigurationParameter(name = READ_VIEWS)
    private String[] readViews;
    @ConfigurationParameter(name = INPUT_TYPES, mandatory = false)
    private String[] typeClassNames;
    @ConfigurationParameter(name = INPUT_FIELDS, mandatory = false)
    private String[] fieldNames;
    @ConfigurationParameter(name = PULLER_CLASSES, mandatory = false)
    private String[] pullerClassNames;
    @ConfigurationParameter(name = ALIGNER_CLASS, mandatory = false)
    private String alignerClassName;
    @ConfigurationParameter(name = DOC_SUMMARIZER_CLASS, mandatory = false)
    private String documentSummarizerClassName;
    @ConfigurationParameter(name = OUTPUT_DIRECTORY, mandatory = false)
    private String outputDirectory;

    @ConfigurationParameter(name = COLLECTION_SUMMARIZER_CLASS, mandatory = false)
    private String collectionSummarizerClassName;
    @ConfigurationParameter(name = SUMMARY_OUTPUT_PATH, mandatory = false)
    private String summaryOutputPath;

    private List<Puller> pullers;
    private List<Class<? extends Annotation>> typeClasses;

    private Aligner aligner;
    private DocumentSummarizer documentSummarizer;
    private CollectionSummarizer collectionSummarizer;

    private boolean micro = false;
    private boolean macro = false;

    private List<AlignedTuple> collectionSummaryTuples;
    private List<String> collectionSummaryDocIds;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);

        if (collectionSummarizerClassName == null && documentSummarizerClassName == null) {
            LOGGER.warning("Exporter should specify either an DocumentSummarizer or a CollectionSummarizer; " +
                    "doing nothing for Exporter " + myName);
            return;
        }

        LOGGER.info("Initializing Exporter analysis engine.");

        if (documentSummarizerClassName != null) {
            if (outputDirectory == null) {
                outputDirectory = "amicusExporterDefaultOutput";
            }
            try {
                Files.createDirectories(Paths.get(outputDirectory));
            } catch (IOException e) {
                throw new ResourceInitializationException(e);
            }
            try {
                documentSummarizer = AnalysisPieceFactory.microSummarizer(documentSummarizerClassName, readViews, typeClassNames, fieldNames);
            } catch (AmicusException e) {
                LOGGER.severe(String.format("Could not initialize DocumentSummarizer for Exporter \"%s\"", myName));
                throw new ResourceInitializationException(e);
            }
            micro = true;
        }

        // for summary
        if (collectionSummarizerClassName != null) {
            macro = true;
            collectionSummaryTuples = new ArrayList<>();
            collectionSummaryDocIds = new ArrayList<>();
            try {
                collectionSummarizer = AnalysisPieceFactory.macroSummarizer(collectionSummarizerClassName, readViews, typeClassNames, fieldNames);
            } catch (AmicusException e) {
                LOGGER.severe(String.format("Problem instantiating CollectionSummarizer \"%s\" in Exporter \"%s\"",
                        collectionSummarizerClassName, myName));
                throw new ResourceInitializationException(e);
            }
        }

        int numInputs = readViews.length;

        // check lengths of input and output lists to hopefully detect user-caused misalignment in config file
        // These won't be in effect when running AmicusPipeline, but might catch something if just using uimaFIT.
        try {
            assert typeClassNames == null || numInputs == typeClassNames.length;
            assert pullerClassNames == null || numInputs == pullerClassNames.length;
            assert fieldNames == null || numInputs == fieldNames.length;
        } catch (AssertionError e) {
            throw new AmicusConfigurationException("Configuration parameters for inputs do not line up! Check parameter lists.");
        }

        if (typeClassNames == null) typeClassNames = new String[numInputs];
        typeClasses = new ArrayList<>();
        for (int i = 0; i < numInputs; i++) {
            try {
                typeClasses.add(Util.getTypeClass(typeClassNames[i]));
            } catch (AmicusException e) {
                LOGGER.severe(String.format("Could not find input type \"%s\" for Exporter \"%s\". Confirm that types are" +
                        "correct and that classes have been generated from a UIMA type system (easiest way is to build" +
                        "via maven).", typeClassNames[i], myName));
                throw new ResourceInitializationException(e);
            }
        }

        pullers = new ArrayList<>();
        try {
            aligner = AnalysisPieceFactory.aligner(alignerClassName);
            for (int i = 0; i < numInputs; i++) {
                pullers.add(AnalysisPieceFactory.puller(pullerClassNames[i], fieldNames[i]));
            }
        } catch (AmicusException e) {
            LOGGER.severe(String.format("Could not initialize all analysis pieces for Exporter \"%s\"", myName));
            throw new ResourceInitializationException(e);
        }

    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {

        if (!micro && !macro) return;

        String docId;
        try {
            docId = Util.getDocumentID(jCas);
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }

        String text;
        List<AlignedTuple> allPreAnnotations = new ArrayList<>();
        try {
            Iterator<AlignedTuple> listIter = aligner.alignAndIterate(getPreAnnotations(jCas));
            while (listIter.hasNext()) {
                AlignedTuple preAnnotations = listIter.next();
                if (macro) {
                    collectionSummaryTuples.add(preAnnotations);
                    collectionSummaryDocIds.add(docId);
                }
                if (micro) {
                    allPreAnnotations.add(preAnnotations);
                }
            }

        } catch (AmicusException e) {
            LOGGER.severe(String.format("Processing problem for Exporter \"%s\"", myName));
            throw new AnalysisEngineProcessException(e);
        }

        if (micro) {
            try {
                text = documentSummarizer.summarizeDocument(allPreAnnotations.iterator(), docId, jCas.getDocumentText()).toString();
                Path filepath = Paths.get(outputDirectory).resolve(docId + "." + documentSummarizer.getFileExtension());
                Writer writer = new FileWriter(filepath.toFile());
                writer.write(text);
                writer.close();
            } catch (IOException | AmicusException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
    }

    private List<List<ANA>> getPreAnnotations(JCas jCas) throws AnalysisEngineProcessException {
        List<List<ANA>> allAnnotations = new ArrayList<>();
        for (int i=0; i< readViews.length; i++) {
            JCas readView;
            try {
                readView = jCas.getView(readViews[i]);
            } catch (CASException e) {
                LOGGER.severe(String.format("Couldn't access view \"%s\" in Exporter \"%s\"", readViews[i], myName));
                throw new AnalysisEngineProcessException(e);
            }
            List<ANA> theseAnnotations = new ArrayList<>();
            allAnnotations.add(theseAnnotations);
            // Get all annotations of this class and add them to the index
            for (Annotation a : readView.getAnnotationIndex(typeClasses.get(i))) {
                Object pulled;
                try {
                    pulled = pullers.get(i).pull(a);
                } catch (AmicusException e) {
                    throw new AnalysisEngineProcessException();
                }
                if (pulled != null) {
                    theseAnnotations.add(new ANA<>(pulled, a));
                }
            }
        }

        return allAnnotations;
    }


    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {

        if (!macro) return;

        try {
            OutputStream outputStream;
            if (summaryOutputPath == null) {
                System.out.println("***** Summary for Exporter " + myName + ":\n");
                outputStream = System.out;
                summaryOutputPath = "standard out";
            } else {
                Path dirsToCreate = Paths.get(summaryOutputPath).normalize().getParent();
                if (dirsToCreate != null) {
                    Files.createDirectories(Paths.get(summaryOutputPath).normalize().getParent());
                }
                outputStream = new FileOutputStream(summaryOutputPath);
            }
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            try {
//                writer.write(collectionSummarizer.summarizeCollection(listener.regurgitate(), listener.regurgitateIds()).toString());
                writer.write(collectionSummarizer
                        .summarizeCollection(collectionSummaryTuples.iterator(), collectionSummaryDocIds.iterator())
                        .toString());
            } catch (AmicusException e) {
                // todo log
                throw new AnalysisEngineProcessException(e);
            }
            writer.flush();
            writer.close();
            LOGGER.info("Saved summary to " + summaryOutputPath);
        } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    /**
     * Method is synchronized to prevent simultaneous access to the summary lists.
     * Lists will not be ordered by document, but at least they are threadsafe this way.
     * @param tuple
     * @param docId
     */
    private synchronized void addDataSynchronized(AlignedTuple tuple, String docId) {
        collectionSummaryTuples.add(tuple);
        collectionSummaryDocIds.add(docId);
    }


}
