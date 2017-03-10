package edu.umn.amicus.uimacomponents;

import edu.umn.amicus.*;
import edu.umn.amicus.aligners.Aligner;
import edu.umn.amicus.summary.MicroSummarizer;
import edu.umn.amicus.pullers.Puller;
import edu.umn.amicus.summary.DataListener;
import edu.umn.amicus.summary.MacroSummarizer;
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
    public static final String EXPORT_WRITER_CLASS = "exportWriterClassName";
    public static final String OUTPUT_DIRECTORY = "microSummaryOutDirectory";

    public static final String SUMMARY_WRITER_CLASS = "summaryWriterClassName";
    public static final String SUMMARY_OUTPUT_PATH = "macroSummaryOutPath";

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
    @ConfigurationParameter(name = EXPORT_WRITER_CLASS, mandatory = false)
    private String exportWriterClassName;
    @ConfigurationParameter(name = OUTPUT_DIRECTORY, mandatory = false)
    private String outputDirectory;

    @ConfigurationParameter(name = SUMMARY_WRITER_CLASS, mandatory = false)
    private String summaryWriterClassName;
    @ConfigurationParameter(name = SUMMARY_OUTPUT_PATH, mandatory = false)
    private String summaryOutputPath;

    private List<Puller> pullers;
    private List<Class<? extends Annotation>> typeClasses;

    private Aligner aligner;
    private MicroSummarizer microSummarizer;
    private MacroSummarizer macroSummarizer;
    private DataListener<List<PreAnnotation>> listener;

    private boolean micro = false;
    private boolean macro = false;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);

        if (summaryWriterClassName == null && exportWriterClassName == null) {
            LOGGER.warning("Exporter should specify either an MicroSummarizer or a MacroSummarizer; " +
                    "doing nothing for Exporter " + myName);
            return;
        }

        LOGGER.info("Initializing Exporter analysis engine.");

        if (exportWriterClassName != null) {
            try {
                Files.createDirectories(Paths.get(outputDirectory));
            } catch (IOException e) {
                throw new ResourceInitializationException(e);
            }
            try {
                microSummarizer = AnalysisPieceFactory.microSummarizer(exportWriterClassName);
            } catch (AmicusException e) {
                LOGGER.severe(String.format("Could not initialize MicroSummarizer for Exporter \"%s\"", myName));
                throw new ResourceInitializationException(e);
            }
            microSummarizer.setViewNames(readViews);
            micro = true;
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

        // for summary
        if (summaryWriterClassName != null) {
            listener = DataListener.getDataListener(myName);
            macro = true;

            try {
                macroSummarizer = AnalysisPieceFactory.macroSummarizer(summaryWriterClassName);
            } catch (AmicusException e) {
                LOGGER.severe(String.format("Problem instantiating MacroSummarizer \"%s\" in Exporter \"%s\"",
                        summaryWriterClassName, myName));
                throw new ResourceInitializationException(e);
            }
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
        List<List<PreAnnotation>> allPreAnnotations = new ArrayList<>();
        try {
            Iterator<List<Annotation>> listIter = aligner.alignAndIterate(getAnnotations(jCas));
            while (listIter.hasNext()) {
                List<Annotation> annotations = listIter.next();
                List<PreAnnotation> preannotations = new ArrayList<>();
                for (int i = 0; i < annotations.size(); i++) {
                    preannotations.add(annotations.get(i) == null ? null :
                            new PreAnnotation(pullers.get(i).pull(annotations.get(i)), annotations.get(i)));
                }
                if (macro) {
                    listener.listen(preannotations, docId);
                }
                if (micro) {
                    allPreAnnotations.add(preannotations);
                }
            }

        } catch (AmicusException e) {
            LOGGER.severe(String.format("Processing problem for Merger \"%s\"", myName));
            throw new AnalysisEngineProcessException(e);
        }

//
//
//        try {
//            // Set up a shell iterator that will call Pullers and pass along transformed values to the microSummarizer
//            final Iterator<List<Annotation>> annotationsIterator = aligner.alignAndIterate(getAnnotations(jCas));
//            text = microSummarizer.exportContents(new Iterator<List<PreAnnotation>>() {
//                @Override
//                public boolean hasNext() {
//                    return annotationsIterator.hasNext();
//                }
//
//                @Override
//                public List<PreAnnotation> next() {
//                    List<Annotation> annotations = annotationsIterator.next();
//                    List<PreAnnotation> preannotations = new ArrayList<>();
//                    for (int i = 0; i < annotations.size(); i++) {
//                        try {
//                            preannotations.add(
//                                    new PreAnnotation<>(pullers.get(i).pull(annotations.get(i)), annotations.get(i)));
//                        } catch (AmicusException e) {
//                            LOGGER.warning(String.format("Could not pull annotation! Exporter \"%s\"", myName));
//                        }
//                    }
//                    return preannotations;
//                }
//
//                @Override
//                public void remove() {
//                    annotationsIterator.remove();
//                }
//            });
//        } catch (AmicusException e) {
//            LOGGER.severe(String.format("Processing exception for Exporter \"%s\"", myName));
//            throw new AnalysisEngineProcessException(e);
//        }

        if (micro) {
            text = microSummarizer.exportContents(allPreAnnotations.iterator());
            Path filepath = Paths.get(outputDirectory).resolve(docId + "." + microSummarizer.getFileExtension());
            Writer writer;
            try {
                writer = new FileWriter(filepath.toFile());
                writer.write(text);
                writer.close();
            } catch (IOException e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
    }

    private List<List<Annotation>> getAnnotations(JCas jCas) throws AnalysisEngineProcessException {
        List<List<Annotation>> allAnnotations = new ArrayList<>();
        for (int i=0; i< readViews.length; i++) {
            JCas readView;
            try {
                readView = jCas.getView(readViews[i]);
            } catch (CASException e) {
                LOGGER.severe(String.format("Couldn't access view \"%s\" in Exporter \"%s\"", readViews[i], myName));
                throw new AnalysisEngineProcessException(e);
            }
            List<Annotation> theseAnnotations = new ArrayList<>();
            allAnnotations.add(theseAnnotations);
            // Get all annotations of this class and add them to the index
            for (Annotation a : readView.getAnnotationIndex(typeClasses.get(i))) {
                theseAnnotations.add(a);
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
                writer.write(macroSummarizer.summarize(listener.regurgitate(), listener.regurgitateIds()).toString());
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


}
