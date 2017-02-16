package edu.umn.amicus.uimacomponents;

import edu.umn.amicus.Amicus;
import edu.umn.amicus.AmicusException;
import edu.umn.amicus.PreAnnotation;
import edu.umn.amicus.aligners.AnnotationAligner;
import edu.umn.amicus.pushers.AnnotationPusher;
import edu.umn.amicus.pushers.MultiPusher;
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by gpfinley on 1/18/17.
 */
public class MergerAE extends JCasAnnotator_ImplBase {

    private static final Logger LOGGER = Logger.getLogger(MergerAE.class.getName());

    public static final String READ_VIEWS = "readViews";
    public static final String TYPE_CLASSES = "typeClasses";
    public static final String FIELD_NAMES = "fieldNames";
    public static final String PULLER_CLASSES = "pullerClasses";

    public static final String ALIGNER_CLASS = "alignerClass";

    public static final String DISTILLER_CLASSES = "distillerClasses";
    public static final String PUSHER_CLASSES = "pusherClasses";
    public static final String OUTPUT_VIEW_NAMES = "outputViewNames";
    public static final String OUTPUT_ANNOTATION_TYPES = "outputAnnotationTypes";
    public static final String OUTPUT_ANNOTATION_FIELDS = "outputAnnotationFields";

    // Some things are not mandatory because there are defaults available (anything with CLASS).
    // Others are not mandatory because specialized pullers/pushers might not use them (but the defaults do!).

    @ConfigurationParameter(name = READ_VIEWS)
    private String[] readViews;
    @ConfigurationParameter(name = TYPE_CLASSES, mandatory = false)
    private String[] typeClassNames;
    @ConfigurationParameter(name = FIELD_NAMES, mandatory = false)
    private String[] fieldNames;
    @ConfigurationParameter(name = PULLER_CLASSES, mandatory = false)
    private String[] pullerClassNames;

    @ConfigurationParameter(name = ALIGNER_CLASS, mandatory = false)
    private String alignerClassName;

    @ConfigurationParameter(name = DISTILLER_CLASSES, mandatory = false)
    private String[] distillerClassNames;
    @ConfigurationParameter(name = PUSHER_CLASSES, mandatory = false)
    private String[] pusherClassNames;
    @ConfigurationParameter(name = OUTPUT_VIEW_NAMES)
    private String[] outputViewNames;
    @ConfigurationParameter(name = OUTPUT_ANNOTATION_TYPES, mandatory = false)
    private String[] outputAnnotationTypes;
    // fields should be separated by semicolons if there are more than one (and a multi-setter Creator should be used)
    @ConfigurationParameter(name = OUTPUT_ANNOTATION_FIELDS, mandatory = false)
    private String[] outputAnnotationFields;

    private List<Class> typeClasses;
    private List<AnnotationDistiller> distillers;
    private List<AnnotationPusher> pushers;
    private List<AnnotationPuller> pullers;
    private AnnotationAligner aligner;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        LOGGER.info("Initializing MergedViewAnnotator.");

        int numInputs = readViews.length;
        int numOutputs = outputViewNames.length;

        // check lengths of input and output lists to hopefully detect user-caused misalignment in config file
        // These won't be in effect when running AmicusPipeline, but might catch something if just using uimaFIT.
        try {
            assert numInputs == typeClassNames.length;
            assert pullerClassNames == null || numInputs == pullerClassNames.length;
            assert fieldNames == null || numInputs == fieldNames.length;
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

        // todo: take out null checks here?? Defaults should only be specified in one place and they're also in the config classes
        try {
            if (alignerClassName != null) {
                aligner = (AnnotationAligner) Class.forName(alignerClassName).newInstance();
            } else {
                aligner = Amicus.DEFAULT_ALIGNER_CLASS.newInstance();
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            LOGGER.severe("Could not initialize annotation aligner.");
            throw new AmicusException(e);
        }

        try {
            distillers = new ArrayList<>();
            for (String distillerName : distillerClassNames) {
                if (distillerName == null) {
                    distillers.add(Amicus.DEFAULT_DISTILLER_CLASS.newInstance());
                } else {
                    distillers.add((AnnotationDistiller) Class.forName(distillerName).newInstance());
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            LOGGER.severe("Could not initialize annotation distillers.");
            throw new AmicusException(e);
        }

        try {
            pushers = new ArrayList<>();
            for (int i=0; i < numOutputs; i++) {
                if(pusherClassNames[i] == null) {
                    if (outputAnnotationFields[i] == null | outputAnnotationTypes[i] == null) {
                        throw new AmicusException("Need to provide output annnotation fields and types UNLESS using" +
                                " a custom AnnotationPusher implementation that can ignore them.");
                    }
                    Constructor<? extends AnnotationPusher> creatorConstructor;
                    String[] outFields = outputAnnotationFields[i].split(MultiPusher.DELIMITER);
                    if (outFields.length <= 1) {
                        creatorConstructor = Amicus.DEFAULT_PUSHER_CLASS.getConstructor(String.class, String.class);
                    } else {
                        creatorConstructor = Amicus.DEFAULT_MULTI_PUSHER_CLASS.getConstructor(String.class, String.class);
                    }
                    pushers.add(creatorConstructor.newInstance(outputAnnotationTypes[i], outputAnnotationFields[i]));
                } else {
                    // if output annotation types or fields are null, the custom creator class better be able to handle that
                    String outputAnnotationType = outputAnnotationTypes != null ? outputAnnotationTypes[i] : null;
                    String outputAnnotationField = outputAnnotationFields != null ? outputAnnotationFields[i] : null;
                    Class<? extends AnnotationPusher> creatorClass =
                            (Class<? extends AnnotationPusher>) Class.forName(pusherClassNames[i]);
                    Constructor creatorConstructor = creatorClass.getConstructor(String.class, String.class);
                    pushers.add((AnnotationPusher) creatorConstructor
                            .newInstance(outputAnnotationType, outputAnnotationField));
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException
                | InvocationTargetException e) {
            LOGGER.severe("Could not initialize annotation pushers.");
            throw new AmicusException(e);
        }

        pullers = new ArrayList<>();
        try {
            for (int i=0; i<numInputs; i++) {
                if (pullerClassNames[i] == null) {
                    if (fieldNames == null) {
                        throw new AmicusException("Need to specify field names" +
                                " unless using a custom AnnotationPuller implementation.");
                    }
                    pullers.add(Amicus.DEFAULT_PULLER_CLASS
                            .getConstructor(String.class).newInstance(fieldNames[i]));
                } else {
                    String fieldName = fieldNames != null ? fieldNames[i] : null;
                    Constructor constructor = Class.forName(pullerClassNames[i]).getConstructor(String.class);
                    pullers.add((AnnotationPuller) constructor.newInstance(fieldName));
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException
                | InvocationTargetException e) {
            LOGGER.severe("Could not initialize annotation pullers.");
            throw new AmicusException(e);
        }

        try {
            typeClasses = new ArrayList<>();
            for (int i = 0; i < numInputs; i++) {
                typeClasses.add(Class.forName(typeClassNames[i]));
            }
        } catch (ClassNotFoundException e) {
            LOGGER.severe("Could not add input types. Confirm that types are correct and that classes have been " +
                    "generated from a UIMA type system (easiest way is to build via maven).");
            throw new AmicusException(e);
        }
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {

        // todo: modify this so that we can get strings or arrays (for audio, etc.)
        String sofaData = "";
        try {
            Iterator<JCas> viewIter = jCas.getViewIterator();
            while (viewIter.hasNext() && "".equals(sofaData)) {
                sofaData = viewIter.next().getSofaDataString();
            }
            if ("".equals(sofaData)) {
                LOGGER.warning("No sofaData found in any view!");
            }
        } catch (CASException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

        // add any views that are used by any outputs
        Set<String> viewsToAdd = new HashSet<>();
        for (String outputViewName : outputViewNames) {
            viewsToAdd.add(outputViewName);
        }
        try {
            Iterator<JCas> viewIter = jCas.getViewIterator();
            while (viewIter.hasNext()) {
                viewsToAdd.remove(viewIter.next().getViewName());
            }
            for (String viewToAdd : viewsToAdd) {
                JCas addedView = jCas.createView(viewToAdd);
                addedView.setSofaDataString(sofaData, "text");
            }
        } catch(CASException e) {
            throw new AnalysisEngineProcessException(e);
        }

        for (int i = 0; i < outputViewNames.length; i++) {
            String outputViewName = outputViewNames[i];
            if (outputViewName == null) outputViewName = Amicus.DEFAULT_MERGED_VIEW;
            JCas outputView;
            try {
                Iterator<JCas> viewIter = jCas.getViewIterator();
                createNewView:
                {
                    while (viewIter.hasNext()) {
                        outputView = viewIter.next();
                        if (outputView.getViewName().equals(outputViewName)) {
                            break createNewView;
                        }
                    }
                    outputView = jCas.createView(outputViewName);
                    outputView.setSofaDataString(sofaData, "text");
                }
            } catch (CASException e) {
                throw new AmicusException(e);
            }
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
                pushers.get(i).push(outputView, distilled);
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
