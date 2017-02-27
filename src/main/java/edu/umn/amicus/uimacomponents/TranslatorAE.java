package edu.umn.amicus.uimacomponents;

import edu.umn.amicus.AmicusException;
import edu.umn.amicus.PreAnnotation;
import edu.umn.amicus.distillers.AnnotationDistiller;
import edu.umn.amicus.filters.AnnotationFilter;
import edu.umn.amicus.mappers.Mapper;
import edu.umn.amicus.pullers.AnnotationPuller;
import edu.umn.amicus.pushers.AnnotationPusher;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
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

    public static final String READ_VIEW = "readView";
    public static final String INPUT_TYPE = "typeClass";
    public static final String INPUT_FIELD = "inputField";
    public static final String PULLER_CLASS = "pullerClass";

    public static final String FILTER_PATTERN = "filterPattern";
    public static final String FILTER_CLASS = "filterClass";

    public static final String MAPPER_CONFIG_PATHS = "mapperConfigPaths";

//    public static final String DISTILLER_CLASSES = "distillerClasses";
    public static final String PUSHER_CLASSES = "pusherClasses";
    public static final String WRITE_VIEWS = "outputViewNames";
    public static final String OUTPUT_TYPES = "outputAnnotationTypes";
    public static final String OUTPUT_FIELDS = "outputAnnotationFields";

    // Some things are not mandatory because there are defaults available (anything with CLASS).
    // Others are not mandatory because specialized pullers/pushers might not use them (but the defaults do!).

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

//    @ConfigurationParameter(name = DISTILLER_CLASSES, mandatory = false)
//    private String[] distillerClassNames;
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
    private AnnotationPuller puller;

//    private List<AnnotationDistiller> distillers;
    private List<AnnotationPusher> pushers;

    private AnnotationFilter filter;
    private List<Mapper> mappers;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        LOGGER.info("Initializing Translator analysis engine.");

        int numOutputs = outputViewNames.length;

        // check lengths of output lists to hopefully detect user-caused misalignment in config file
        // These won't be in effect when running AmicusPipeline, but might catch something if just using uimaFIT.
        try {
//            assert distillerClassNames == null || numOutputs == distillerClassNames.length;
            assert pusherClassNames == null || numOutputs == pusherClassNames.length;
            assert outputAnnotationTypes == null || numOutputs == outputAnnotationTypes.length;
            assert outputAnnotationFields == null || numOutputs == outputAnnotationFields.length;
        } catch (AssertionError e) {
            throw new AmicusException("Configuration parameters for outputs do not line up! Check parameter lists.");
        }

//        distillers = new ArrayList<>();
        pushers = new ArrayList<>();
        for (int i=0; i<numOutputs; i++) {
//            distillers.add(AnnotationDistiller.create(distillerClassNames[i]));
            pushers.add(AnnotationPusher.create(
                    pusherClassNames[i], outputAnnotationTypes[i], outputAnnotationFields[i]));
        }
//        System.out.println(pullerClassName);
//        System.out.println(inputField);
        puller = AnnotationPuller.create(pullerClassName, inputField);
        filter = AnnotationFilter.create(filterClassName, filterPattern);

        mappers = new ArrayList<>();
        for (String mapperConfigPath : mapperConfigPaths) {
            mappers.add(Mapper.create(mapperConfigPath));
        }

        try {
            typeClass = Class.forName(typeClassName);
        } catch (ClassNotFoundException e) {
            LOGGER.severe("Could not add input types. Confirm that full type paths are correct and that classes" +
                    " have been generated from a UIMA type system (easiest way is to build via maven).");
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {

        try {
            String sofaData = (String) Util.getSofaData(jCas);
            Util.createOutputViews(jCas, sofaData, outputViewNames);
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }

        List<Annotation> annotations = getAnnotations(jCas);
        List<PreAnnotation<Object>> preAnnotations = new ArrayList<>();

        for (Annotation annotation : annotations) {
            Object pulled = puller.transform(annotation);
            if (filter.passes(pulled)) {
                for (Mapper mapper : mappers) {
                    pulled = mapper.map(pulled);
                }
                System.out.println(pulled);
                preAnnotations.add(new PreAnnotation<>(pulled, annotation));
            }
        }

        for (int i = 0; i < outputViewNames.length; i++) {
            JCas outputView;
            try {
                outputView = jCas.getView(outputViewNames[i]);
            } catch (CASException e) {
                throw new AmicusException(e);
            }
            for (PreAnnotation preAnnotation : preAnnotations) {
                pushers.get(i).push(outputView, preAnnotation);
            }
        }
    }

    private List<Annotation> getAnnotations(JCas jCas) {
        JCas viewToRead;
        try {
            viewToRead = jCas.getView(readView);
        } catch (CASException e) {
            e.printStackTrace();
            throw new AmicusException("Couldn't find view %s", readView);
        }
        List<Annotation> theseAnnotations = new ArrayList<>();
        // Get all annotations of this class and add them to the index
        for (Annotation a : (FSIndex<Annotation>) viewToRead.getAnnotationIndex(typeClass)) {
            theseAnnotations.add(a);
        }
        return theseAnnotations;
    }

}
