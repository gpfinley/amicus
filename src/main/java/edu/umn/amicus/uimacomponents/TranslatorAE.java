package edu.umn.amicus.uimacomponents;

import edu.umn.amicus.*;
import edu.umn.amicus.filters.Filter;
import edu.umn.amicus.mappers.Mapper;
import edu.umn.amicus.pullers.Puller;
import edu.umn.amicus.pushers.Pusher;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by gpfinley on 1/18/17.
 */
public class TranslatorAE extends JCasAnnotator_ImplBase {

    private static final Logger LOGGER = Logger.getLogger(TranslatorAE.class.getName());

    public static final String MY_NAME = "name";

    public static final String READ_VIEW = "readView";
    public static final String INPUT_TYPE = "typeClass";
    public static final String INPUT_FIELD = "inputField";
    public static final String PULLER_CLASS = "pullerClass";

    public static final String FILTER_PATTERN = "filterPattern";
    public static final String FILTER_CLASS = "filterClass";

    public static final String MAPPER_CONFIG_PATHS = "mappers";

    public static final String PUSHER_CLASSES = "pusherClasses";
    public static final String WRITE_VIEWS = "outputViewNames";
    public static final String OUTPUT_TYPES = "outputAnnotationTypes";
    public static final String OUTPUT_FIELDS = "outputAnnotationFields";

    // Some things are not mandatory because there are defaults available (anything with CLASS).
    // Others are not mandatory because specialized pullers/pushers might not use them (but the defaults do!).

    @ConfigurationParameter(name = MY_NAME, defaultValue = "Unnamed Translator")
    private String myName;

    @ConfigurationParameter(name = READ_VIEW)
    private String readView;
    @ConfigurationParameter(name = INPUT_TYPE, mandatory = false)
    private String typeClassName;
    @ConfigurationParameter(name = INPUT_FIELD, mandatory = false)
    private String inputField;
    @ConfigurationParameter(name = PULLER_CLASS, mandatory = false)
    private String pullerClassName;

    @ConfigurationParameter(name = FILTER_CLASS, mandatory = false)
    private String filterClassName;
    @ConfigurationParameter(name = FILTER_PATTERN, mandatory = false)
    private String filterPattern;

    @ConfigurationParameter(name = MAPPER_CONFIG_PATHS, mandatory = false)
    private String[] mapperConfigPaths;

    @ConfigurationParameter(name = PUSHER_CLASSES, mandatory = false)
    private String[] pusherClassNames;
    @ConfigurationParameter(name = WRITE_VIEWS)
    private String[] outputViewNames;
    @ConfigurationParameter(name = OUTPUT_TYPES, mandatory = false)
    private String[] outputAnnotationTypes;
    // fields should be separated by semicolons if there are more than one (and a multi-setter Creator should be used)
    @ConfigurationParameter(name = OUTPUT_FIELDS, mandatory = false)
    private String[] outputAnnotationFields;

    private Class typeClass;
    private Puller puller;

    private List<Pusher> pushers;

    private Filter filter;
    private List<Mapper> mappers;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        LOGGER.info("Initializing Translator analysis engine.");

        int numOutputs = outputViewNames.length;

        // check lengths of output lists to hopefully detect user-caused misalignment in config file
        // These won't be in effect when running AmicusPipeline, but might catch something if just using uimaFIT.
        try {
            assert pusherClassNames == null || numOutputs == pusherClassNames.length;
            assert outputAnnotationTypes == null || numOutputs == outputAnnotationTypes.length;
            assert outputAnnotationFields == null || numOutputs == outputAnnotationFields.length;
        } catch (AssertionError e) {
            LOGGER.severe(String.format("Configuration problem for Translator \"%s\"", myName));
            throw new AmicusConfigurationException("Configuration parameters for outputs do not line up! Check parameter lists.");
        }

        try {
            pushers = new ArrayList<>();
            for (int i = 0; i < numOutputs; i++) {
                pushers.add(AnalysisPieceFactory.pusher(
                        pusherClassNames[i], outputAnnotationTypes[i], outputAnnotationFields[i]));
            }
            puller = AnalysisPieceFactory.puller(pullerClassName, inputField);
            filter = AnalysisPieceFactory.filter(filterClassName, filterPattern);

            mappers = new ArrayList<>();
            if (mapperConfigPaths != null) {
                for (String mapperConfigPath : mapperConfigPaths) {
                    mappers.add(AnalysisPieceFactory.mapper(mapperConfigPath));
                }
            }
        } catch (AmicusException e) {
            LOGGER.severe(String.format("Couldn't load analysis pieces for Translator \"%s\"", myName));
            throw new ResourceInitializationException(e);
        }

        try {
            typeClass = Util.getTypeClass(typeClassName);
        } catch (AmicusException e) {
            LOGGER.severe(String.format("Could not find input type \"%s\" for Translator \"%s\". Confirm that types are" +
                    "correct and that classes have been generated from a UIMA type system (easiest way is to build" +
                    "via maven).", typeClassName, myName));
            throw new ResourceInitializationException(e);
        }

    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {

        //        String sofaData;
//        try {
//            sofaData = (String) Util.getSofaData(jCas);
//        } catch (CASException e) {
//            LOGGER.severe(String.format("Could not load sofa data for Translator \"%s\"", myName));
//            throw new AnalysisEngineProcessException(e);
//        } catch (AmicusException e) {
//            LOGGER.severe(String.format("No sofa data found anywhere for document %s for Translator \"%s\"",
//                    Util.getDocumentID(jCas.getCas()), myName));
//            throw new AnalysisEngineProcessException(e);
//        }
        try {
//            Util.createOutputViews(jCas, sofaData, outputViewNames);
            Util.createOutputViews(jCas, outputViewNames);
        } catch (CASException e) {
            LOGGER.severe(String.format("Could not create output views for Translator \"%s\"", myName));
            throw new AnalysisEngineProcessException(e);
//        } catch (MismatchedSofaDataException e) {
//            LOGGER.warning(String.format("Inconsistent sofa data found in view %s for document %s",
//                    jCas.getViewName(), Util.getDocumentID(jCas.getCas())));
        }

        List<Annotation> annotations = getAnnotations(jCas);
        List<PreAnnotation<Object>> preAnnotations = new ArrayList<>();

        try {

            for (Annotation annotation : annotations) {
                Object pulled = puller.pull(annotation);
                if (filter.passes(pulled)) {
                    for (Mapper mapper : mappers) {
                        pulled = mapper.map(pulled);
                    }
                    preAnnotations.add(new PreAnnotation<>(pulled, annotation));
                }
            }

            for (int i = 0; i < outputViewNames.length; i++) {
                JCas outputView;
                try {
                    outputView = jCas.getView(outputViewNames[i]);
                } catch (CASException | CASRuntimeException e) {
                    LOGGER.severe(String.format("Couldn't find output view %s for Translator \"%s\"",
                            outputViewNames[i], myName));
                    throw new AnalysisEngineProcessException(e);
                }
                for (PreAnnotation preAnnotation : preAnnotations) {
                    pushers.get(i).push(outputView, preAnnotation);
                }
            }
        } catch (AmicusException e) {
            LOGGER.severe(String.format("Processing exception for Translator \"%s\"", myName));
            throw new AnalysisEngineProcessException(e);
        }
    }

    private List<Annotation> getAnnotations(JCas jCas) throws AnalysisEngineProcessException {
        JCas viewToRead;
        try {
            viewToRead = jCas.getView(readView);
        } catch (CASException e) {
            LOGGER.severe(String.format("Couldn't find view \"%s\" for Translator \"%s\"", readView, myName));
            throw new AnalysisEngineProcessException(e);
        }
        List<Annotation> theseAnnotations = new ArrayList<>();
        // Get all annotations of this class and add them to the index
        for (Annotation a : (FSIndex<Annotation>) viewToRead.getAnnotationIndex(typeClass)) {
            theseAnnotations.add(a);
        }
        return theseAnnotations;
    }

}
