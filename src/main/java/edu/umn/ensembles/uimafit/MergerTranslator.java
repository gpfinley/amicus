package edu.umn.ensembles.uimafit;

import edu.umn.ensembles.Ensembles;
import edu.umn.ensembles.EnsemblesException;
import edu.umn.ensembles.PreAnnotation;
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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
    public static final String DISTILLER_CLASS = "distillerClass";
    public static final String CREATOR_CLASS = "creatorClass";
    public static final String OUTPUT_VIEW_NAME = "outputViewName";
    public static final String OUTPUT_ANNOTATION_TYPE = "outputAnnotationType";
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
    @ConfigurationParameter(name = DISTILLER_CLASS, mandatory = false)
    private String distillerClassName;
    @ConfigurationParameter(name = CREATOR_CLASS, mandatory = false)
    private String creatorClassName;
    @ConfigurationParameter(name = OUTPUT_VIEW_NAME)
    private String outputViewName;
    @ConfigurationParameter(name = OUTPUT_ANNOTATION_TYPE, mandatory = false)
    private String outputAnnotationType;
    // fields should be separated by semicolons if there are more than one
    @ConfigurationParameter(name = OUTPUT_ANNOTATION_FIELDS, mandatory = false)
    private String outputAnnotationFields;

    private Class[] typeClasses;
    private AnnotationAligner aligner;
    private AnnotationDistiller distiller;
    private AnnotationCreator creator;
    private List<AnnotationTransformer> transformers;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        LOGGER.info("Initializing MergedViewAnnotator.");

        try {
            assert systemNames.length == typeClasses.length;
        } catch(AssertionError e) {
            throw new EnsemblesException("System names and number of types must be same length (to map each type to its origin system).");
        }

        try {
            try {
                if (alignerClassName != null) {
                    aligner = (AnnotationAligner) Class.forName(alignerClassName).newInstance();
                } else {
                    aligner = (AnnotationAligner) Ensembles.DEFAULT_ALIGNER_CLASS.newInstance();
                }
                if (distillerClassName != null) {
                    distiller = (AnnotationDistiller) Class.forName(distillerClassName).newInstance();
                } else {
                    distiller = (AnnotationDistiller) Ensembles.DEFAULT_DISTILLER_CLASS.newInstance();
                }
                Constructor<? extends AnnotationCreator> creatorConstructor;
                String[] outFields = outputAnnotationFields.split(MultiCreator.SPLITTER);
                try {
                    if (creatorClassName != null) {
                        Class<? extends AnnotationCreator> creatorClass = (Class<? extends AnnotationCreator>) Class.forName(creatorClassName);
//                        if (!AnnotationCreator.class.isAssignableFrom(creatorClass)) {
//                            throw new EnsemblesException("Class %s does not extend AnnotationCreator.", creatorClassName);
//                        }
                        creatorConstructor = creatorClass.getConstructor(String.class, String.class);
                    } else {
                        if (outFields.length <= 1) {
                            creatorConstructor = Ensembles.DEFAULT_CREATOR_CLASS.getConstructor(String.class, String.class);
                        } else {
                            creatorConstructor = Ensembles.DEFAULT_MULTI_CREATOR_CLASS.getConstructor(String.class, String.class);
                        }
                    }
                } catch (NoSuchMethodException e) {
                    // todo: log/provide feedback
                    e.printStackTrace();
                    throw new EnsemblesException();
                }
                creator = creatorConstructor.newInstance(outputAnnotationType, outputAnnotationFields);
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                // todo: log/provide feedback
                e.printStackTrace();
                throw new EnsemblesException();
            }

            transformers = new ArrayList<>();
            for (int i=0; i<fieldNames.length; i++) {
                Class clazz = (transformerClassNames == null ||
                        transformerClassNames.length <= i ||
                        transformerClassNames[i] == null) ? Ensembles.DEFAULT_TRANSFORMER_CLASS
                        : Class.forName(transformerClassNames[i]);
                try {
                    transformers.add((AnnotationTransformer) clazz.getConstructor(String.class).newInstance(fieldNames[i]));
                } catch (Exception e) {
                    // todo: log/throw/etc
                    e.printStackTrace();
                    throw new EnsemblesException();
                }
            }

            typeClasses = new Class[typeClassNames.length];
            for (int i=0; i<typeClasses.length; i++) {
                typeClasses[i] = Class.forName(typeClassNames[i]);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            // todo: should these exceptions be ResourceInit??
            throw new EnsemblesException("Couldn't find a class; check names in config.");
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

        JCas outputView;
        try {
            // todo: does this return null when there is no view of this name, or does it throw an exception??
            outputView = jCas.getView(outputViewName);
        } catch (CASException e) {
            try {
                outputView = jCas.createView(outputViewName);
                outputView.setSofaDataString(sofaData, "text");
            } catch (CASException e2) {
                e2.printStackTrace();
                throw new EnsemblesException();
            }
        }
        final JCas outputViewFinal = outputView;

        aligner.alignAndIterate(getAnnotations(jCas))
                .forEachRemaining(annotations -> {
                            List<PreAnnotation> preannotations = new ArrayList<>();
                            for (int i = 0; i < annotations.size(); i++) {
                                preannotations.add(transformers.get(i).transform(annotations.get(i)));
                            }
                            PreAnnotation distilled = distiller.distill(preannotations);
                            creator.set(outputViewFinal, distilled);
                        });
    }

    private List<List<Annotation>> getAnnotations(JCas jCas) {
        List<List<Annotation>> allAnnotations = new ArrayList<>();
        for (int i=0; i<systemNames.length; i++) {
            JCas readView;
            try {
                readView = jCas.getView(Ensembles.systemToViewName(systemNames[i]));
            } catch (CASException e) {
                e.printStackTrace();
                throw new EnsemblesException("Couldn't find view for system named %s", systemNames[i]);
            }
            List<Annotation> theseAnnotations = new ArrayList<>();
            allAnnotations.add(theseAnnotations);
            // Get all annotations of this class and add them to the index
            readView.getAnnotationIndex(typeClasses[i]).forEach(a -> theseAnnotations.add((Annotation) a));
        }

        return allAnnotations;
    }

}
