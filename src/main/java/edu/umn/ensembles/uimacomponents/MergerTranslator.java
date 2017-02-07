package edu.umn.ensembles.uimacomponents;

import edu.umn.ensembles.Ensembles;
import edu.umn.ensembles.EnsemblesException;
import edu.umn.ensembles.PreAnnotation;
import edu.umn.ensembles.Util;
import edu.umn.ensembles.aligners.AnnotationAligner;
import edu.umn.ensembles.creators.MultiCreator;
import edu.umn.ensembles.distillers.AnnotationDistiller;
import edu.umn.ensembles.creators.AnnotationCreator;
import edu.umn.ensembles.transformers.AnnotationTransformer;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import javax.management.ReflectionException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by gpfinley on 1/18/17.
 */
public class MergerTranslator extends JCasAnnotator_ImplBase {

    private static final Logger LOGGER = Logger.getLogger(MergerTranslator.class.getName());

    public static final String SYSTEM_NAMES = "systemName";
    public static final String TYPE_CLASSES = "typeClasses";
    public static final String FIELD_NAMES = "fieldNames";
    public static final String TRANSFORMER_CLASSES = "transformerClasses";

    public static final String ALIGNER_CLASS = "alignerClass";

    public static final String DISTILLER_CLASSES = "distillerClasses";
    public static final String CREATOR_CLASSES = "creatorClasses";
    public static final String OUTPUT_VIEW_NAMES = "outputViewNames";
    public static final String OUTPUT_ANNOTATION_TYPES = "outputAnnotationTypes";
    public static final String OUTPUT_ANNOTATION_FIELDS = "outputAnnotationFields";

    // Some things are not mandatory because there are defaults available (anything with CLASS).
    // Others are not mandatory because specialized transformers/creators might not use them (but the defaults do!).

    @ConfigurationParameter(name = SYSTEM_NAMES)
    private String[] systemNames;
    @ConfigurationParameter(name = TYPE_CLASSES)
    private String[] typeClassNames;
    @ConfigurationParameter(name = FIELD_NAMES, mandatory = false)
    private String[] fieldNames;
    @ConfigurationParameter(name = TRANSFORMER_CLASSES, mandatory = false)
    private String[] transformerClassNames;

    @ConfigurationParameter(name = ALIGNER_CLASS, mandatory = false)
    private String alignerClassName;

    @ConfigurationParameter(name = DISTILLER_CLASSES, mandatory = false)
    private String[] distillerClassNames;
    @ConfigurationParameter(name = CREATOR_CLASSES, mandatory = false)
    private String[] creatorClassNames;
    @ConfigurationParameter(name = OUTPUT_VIEW_NAMES)
    private String[] outputViewNames;
    @ConfigurationParameter(name = OUTPUT_ANNOTATION_TYPES, mandatory = false)
    private String[] outputAnnotationTypes;
    // fields should be separated by semicolons if there are more than one (and a multi-setter Creator should be used)
    @ConfigurationParameter(name = OUTPUT_ANNOTATION_FIELDS, mandatory = false)
    private String[] outputAnnotationFields;

    private List<Class> typeClasses;
    private List<AnnotationDistiller> distillers;
    private List<AnnotationCreator> creators;
    private List<AnnotationTransformer> transformers;
    private AnnotationAligner aligner;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        LOGGER.info("Initializing MergedViewAnnotator.");

        int numInputs = systemNames.length;
        int numOutputs = outputViewNames.length;

        // check lengths of input and output lists to hopefully detect user-caused misalignment in config file

        try {
            assert numInputs == typeClassNames.length;
            assert transformerClassNames == null || numInputs == transformerClassNames.length;
            assert fieldNames == null || numInputs == fieldNames.length;
        } catch (AssertionError e) {
            throw new EnsemblesException("Configuration parameters for inputs do not line up! Check parameter lists.");
        }

        try {
            assert distillerClassNames == null || numOutputs == distillerClassNames.length;
            assert creatorClassNames == null || numOutputs == creatorClassNames.length;
            assert outputAnnotationTypes == null || numOutputs == outputAnnotationTypes.length;
            assert outputAnnotationFields == null || numOutputs == outputAnnotationFields.length;
        } catch (AssertionError e) {
            throw new EnsemblesException("Configuration parameters for outputs do not line up! Check parameter lists.");
        }

        try {
            if (alignerClassName != null) {
                aligner = (AnnotationAligner) Class.forName(alignerClassName).newInstance();
            } else {
                aligner = (AnnotationAligner) Ensembles.DEFAULT_ALIGNER_CLASS.newInstance();
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            LOGGER.severe("Could not initialize annotation aligner.");
            throw new EnsemblesException(e);
        }

        try {
            distillers = new ArrayList<>();
            if (distillerClassNames != null) {
                for (String distillerName : distillerClassNames) {
                    distillers.add((AnnotationDistiller) Class.forName(distillerName).newInstance());
                }
            } else {
                for (int i = 0; i < numOutputs; i++) {
                    distillers.add((AnnotationDistiller) Ensembles.DEFAULT_DISTILLER_CLASS.newInstance());
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            LOGGER.severe("Could not initialize annotation distillers.");
            throw new EnsemblesException(e);
        }

        try {
            creators = new ArrayList<>();
            if (creatorClassNames == null) {
                if (outputAnnotationFields == null | outputAnnotationTypes == null) {
                    throw new EnsemblesException("Need to provide output annnotation fields and types UNLESS using" +
                            " a custom AnnotationCreator implementation that can ignore them.");
                }
                for (int i=0; i < numOutputs; i++) {
                    Constructor<? extends AnnotationCreator> creatorConstructor;
                    String[] outFields = outputAnnotationFields[i].split(MultiCreator.DELIMITER);
                    if (outFields.length <= 1) {
                        creatorConstructor = Ensembles.DEFAULT_CREATOR_CLASS.getConstructor(String.class, String.class);
                    } else {
                        creatorConstructor = Ensembles.DEFAULT_MULTI_CREATOR_CLASS.getConstructor(String.class, String.class);
                    }
                    creators.add(creatorConstructor.newInstance(outputAnnotationTypes[i], outputAnnotationFields[i]));
                }
            } else {
                for (int i=0; i < numOutputs; i++) {
                    // if output annotation types or fields are null, the custom creator class better be able to handle that
                    String outputAnnotationType = outputAnnotationTypes != null ? outputAnnotationTypes[i] : null;
                    String outputAnnotationField = outputAnnotationFields != null ? outputAnnotationFields[i] : null;
                    Class<? extends AnnotationCreator> creatorClass =
                            (Class<? extends AnnotationCreator>) Class.forName(creatorClassNames[i]);
                    Constructor creatorConstructor = creatorClass.getConstructor(String.class, String.class);
                    creators.add((AnnotationCreator) creatorConstructor
                            .newInstance(outputAnnotationType, outputAnnotationField));
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException
                | InvocationTargetException e) {
            LOGGER.severe("Could not initialize annotation creators.");
            throw new EnsemblesException(e);
        }

        transformers = new ArrayList<>();
        try {
            if (transformerClassNames == null) {
                if (fieldNames == null) {
                    throw new EnsemblesException("Need to specify field names" +
                            " unless using a custom AnnotationTransformer implementation.");
                }
                for (int i=0; i<numInputs; i++) {
                    transformers.add((AnnotationTransformer) Ensembles.DEFAULT_TRANSFORMER_CLASS
                            .getConstructor(String.class).newInstance(fieldNames[i]));
                }
            } else {
                for (int i = 0; i < numInputs; i++) {
                    String fieldName = fieldNames != null ? fieldNames[i] : null;
                    Constructor constructor = Class.forName(transformerClassNames[i]).getConstructor(String.class);
                    transformers.add((AnnotationTransformer) constructor.newInstance(fieldName));
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException
                | InvocationTargetException e) {
            LOGGER.severe("Could not initialize annotation transformers.");
            throw new EnsemblesException(e);
        }

        try {
            typeClasses = new ArrayList<>();
            for (int i = 0; i < numInputs; i++) {
                typeClasses.add(Class.forName(typeClassNames[i]));
            }
        } catch (ClassNotFoundException e) {
            LOGGER.severe("Could not add input types. Confirm that types are correct and that classes have been " +
                    "generated from a UIMA type system (easiest way is to build via maven).");
            throw new EnsemblesException(e);
        }
    }

    @Override
    public void process(JCas jCas) {

        // todo: modify this so that we can get strings or arrays (for audio, etc.)
        String sofaData;
        try {
            Iterator<JCas> viewIter = jCas.getViewIterator();
            viewIter.next();
            JCas firstView = viewIter.next();
            sofaData = firstView.getSofaDataString();
        } catch (CASException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

//        final JCas outputViewFinal = outputView;

        aligner.alignAndIterate(getAnnotations(jCas))
                .forEachRemaining(annotations -> {
                            List<PreAnnotation> preannotations = new ArrayList<>();
                            for (int i = 0; i < annotations.size(); i++) {
                                preannotations.add(transformers.get(i).transform(annotations.get(i)));
                            }
                            for (int i = 0; i < annotations.size(); i++) {
                                JCas outputView;
                                try {
                                    // todo: does this return null when there is no view of this name, or does it throw an exception??
                                    outputView = jCas.getView(outputViewNames[i]);
                                } catch (CASException e) {
                                    try {
                                        outputView = jCas.createView(outputViewNames[i]);
                                        outputView.setSofaDataString(sofaData, "text");
                                    } catch (CASException e2) {
                                        e2.printStackTrace();
                                        throw new EnsemblesException();
                                    }
                                }
                                PreAnnotation distilled = distillers.get(i).distill(preannotations);
                                creators.get(i).set(outputView, distilled);
                            }
                        });
    }

    private List<List<Annotation>> getAnnotations(JCas jCas) {
        List<List<Annotation>> allAnnotations = new ArrayList<>();
        for (int i=0; i<systemNames.length; i++) {
            JCas readView;
            try {
                readView = jCas.getView(Util.systemToViewName(systemNames[i]));
            } catch (CASException e) {
                e.printStackTrace();
                throw new EnsemblesException("Couldn't find view for system named %s", systemNames[i]);
            }
            List<Annotation> theseAnnotations = new ArrayList<>();
            allAnnotations.add(theseAnnotations);
            // Get all annotations of this class and add them to the index
            readView.getAnnotationIndex(typeClasses.get(i)).forEach(a -> theseAnnotations.add((Annotation) a));
        }

        return allAnnotations;
    }

}
