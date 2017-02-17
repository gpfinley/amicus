package edu.umn.amicus.uimacomponents;

import edu.umn.amicus.AmicusException;
import edu.umn.amicus.PreAnnotation;
import edu.umn.amicus.aligners.AnnotationAligner;
import edu.umn.amicus.pushers.AnnotationPusher;
import edu.umn.amicus.distillers.AnnotationDistiller;
import edu.umn.amicus.pullers.AnnotationPuller;
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
public class MergerAE extends JCasAnnotator_ImplBase {

    private static final Logger LOGGER = Logger.getLogger(MergerAE.class.getName());

    public static final String READ_VIEWS = "readViews";
    public static final String INPUT_TYPES = "typeClasses";
    public static final String INPUT_FIELDS = "inputFields";
    public static final String PULLER_CLASSES = "pullerClasses";

    public static final String ALIGNER_CLASS = "alignerClass";

    public static final String DISTILLER_CLASSES = "distillerClasses";
    public static final String PUSHER_CLASSES = "pusherClasses";
    public static final String WRITE_VIEWS = "outputViewNames";
    public static final String OUTPUT_TYPES = "outputAnnotationTypes";
    public static final String OUTPUT_FIELDS = "outputAnnotationFields";

    // Some things are not mandatory because there are defaults available (anything with CLASS).
    // Others are not mandatory because specialized pullers/pushers might not use them (but the defaults do!).

    @ConfigurationParameter(name = READ_VIEWS)
    private String[] readViews;
    @ConfigurationParameter(name = INPUT_TYPES, mandatory = false)
    private String[] typeClassNames;
    @ConfigurationParameter(name = INPUT_FIELDS, mandatory = false)
    private String[] inputFields;
    @ConfigurationParameter(name = PULLER_CLASSES, mandatory = false)
    private String[] pullerClassNames;

    @ConfigurationParameter(name = ALIGNER_CLASS, mandatory = false)
    private String alignerClassName;

    @ConfigurationParameter(name = DISTILLER_CLASSES, mandatory = false)
    private String[] distillerClassNames;
    @ConfigurationParameter(name = PUSHER_CLASSES, mandatory = false)
    private String[] pusherClassNames;
    @ConfigurationParameter(name = WRITE_VIEWS)
    private String[] outputViewNames;
    @ConfigurationParameter(name = OUTPUT_TYPES, mandatory = false)
    private String[] outputAnnotationTypes;
    // fields should be separated by semicolons if there are more than one (and a multi-setter Creator should be used)
    @ConfigurationParameter(name = OUTPUT_FIELDS, mandatory = false)
    private String[] outputAnnotationFields;

    private List<Class> typeClasses;
    private List<AnnotationDistiller> distillers;
    private List<AnnotationPusher> pushers;
    private List<AnnotationPuller> pullers;
    private AnnotationAligner aligner;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        LOGGER.info("Initializing Merger analysis engine.");

        int numInputs = readViews.length;
        int numOutputs = outputViewNames.length;

        // check lengths of input and output lists to hopefully detect user-caused misalignment in config file
        // These won't be in effect when running AmicusPipeline, but might catch something if just using uimaFIT.
        try {
            assert numInputs == typeClassNames.length;
            assert pullerClassNames == null || numInputs == pullerClassNames.length;
            assert inputFields == null || numInputs == inputFields.length;
        } catch (AssertionError e) {
            throw new AmicusException("Configuration parameters for inputs do not line up! Check parameter lists.");
        }
        try {
            assert distillerClassNames == null || numOutputs == distillerClassNames.length;
            assert pusherClassNames == null || numOutputs == pusherClassNames.length;
            assert outputAnnotationTypes == null || numOutputs == outputAnnotationTypes.length;
            assert outputAnnotationFields == null || numOutputs == outputAnnotationFields.length;
        } catch (AssertionError e) {
            throw new AmicusException("Configuration parameters for outputs do not line up! Check parameter lists.");
        }

        distillers = new ArrayList<>();
        pushers = new ArrayList<>();
        pullers = new ArrayList<>();
        aligner = AnnotationAligner.create(alignerClassName);
        for (String distillerClassName : distillerClassNames) {
            distillers.add(AnnotationDistiller.create(distillerClassName));
        }
        for (int i=0; i<numOutputs; i++) {
            pushers.add(AnnotationPusher.create(pusherClassNames[i], outputAnnotationTypes[i], outputAnnotationFields[i]));
        }
        for (int i=0; i<numInputs; i++) {
            pullers.add(AnnotationPuller.create(pullerClassNames[i], inputFields[i]));
        }

        typeClasses = new ArrayList<>();
        try {
            for (int i = 0; i < numInputs; i++) {
                typeClasses.add(Class.forName(typeClassNames[i]));
            }
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

        Iterator<List<Annotation>> listIter = aligner.alignAndIterate(getAnnotations(jCas));
        while (listIter.hasNext()) {
            List<Annotation> annotations = listIter.next();
            List<PreAnnotation> preannotations = new ArrayList<>();
            for (int i = 0; i < annotations.size(); i++) {
                preannotations.add(annotations.get(i) == null ? null :
                        new PreAnnotation(pullers.get(i).transform(annotations.get(i)), annotations.get(i)));
            }
            for (int i = 0; i < outputViewNames.length; i++) {
                JCas outputView;
                try {
                    outputView = jCas.getView(outputViewNames[i]);
                } catch (CASException e) {
                    throw new AmicusException(e);
                }
                PreAnnotation distilled = distillers.get(i).distill(preannotations);
                if (distilled != null) {
                    pushers.get(i).push(outputView, distilled);
                }
            }
        }
    }



    private List<List<Annotation>> getAnnotations(JCas jCas) {
        List<List<Annotation>> allAnnotations = new ArrayList<>();
        for (int i=0; i< readViews.length; i++) {
            JCas readView;
            try {
                readView = jCas.getView(readViews[i]);
            } catch (CASException e) {
                e.printStackTrace();
                throw new AmicusException("Couldn't find view %s", readViews[i]);
            }
            List<Annotation> theseAnnotations = new ArrayList<>();
            allAnnotations.add(theseAnnotations);
            // Get all annotations of this class and add them to the index
            for (Annotation a : (FSIndex<Annotation>) readView.getAnnotationIndex(typeClasses.get(i))) {
                theseAnnotations.add(a);
            }
        }

        return allAnnotations;
    }

}
