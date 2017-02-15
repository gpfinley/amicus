package edu.umn.ensembles.uimacomponents;

import edu.umn.ensembles.Ensembles;
import edu.umn.ensembles.EnsemblesException;
import edu.umn.ensembles.eval.DataListener;
import edu.umn.ensembles.summarizers.Summarizer;
import edu.umn.ensembles.pullers.AnnotationPuller;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.CasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.*;
import java.lang.reflect.Constructor;

/**
 * Simple CAS consumer to print basic statistics for evaluation.
 *
 * Created by gpfinley on 2/3/17.
 */
public class CollectorAE extends CasAnnotator_ImplBase {

    public static final String READ_VIEW = "readView";
    public static final String TYPE_CLASS = "typeClass";
    public static final String FIELD_NAME = "fieldName";
    public static final String PULLER_CLASS = "pullerClass";
    public static final String LISTENER_NAME = "listenerName";
    public static final String SUMMARIZER_CLASS = "summarizerClass";
    public static final String OUTPUT_PATH = "outputPath";

    @ConfigurationParameter(name = READ_VIEW)
    private String readViewName;
    @ConfigurationParameter(name = TYPE_CLASS, mandatory = false)
    private String typeClassName;
    @ConfigurationParameter(name = FIELD_NAME, mandatory = false)
    private String fieldName;
    @ConfigurationParameter(name = PULLER_CLASS, mandatory = false)
    private String pullerClassName;
    @ConfigurationParameter(name = LISTENER_NAME)
    private String listenerName;
    @ConfigurationParameter(name = SUMMARIZER_CLASS)
    private String summarizerClass;
    @ConfigurationParameter(name = OUTPUT_PATH, mandatory = false)
    private String outputPath;

    private Class<? extends Annotation> typeClass;
    private AnnotationPuller puller;
    private DataListener listener;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        try {
            typeClass = (Class<? extends Annotation>) Class.forName(typeClassName);
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new ResourceInitializationException(e);
        }
        listener = DataListener.getDataListener(listenerName);

        try {
            if (pullerClassName == null) {
                if (fieldName == null) {
                    throw new EnsemblesException("Need to specify field names when using " +
                            "the default AnnotationPuller implementation.");
                }
                puller = Ensembles.DEFAULT_PULLER_CLASS.getConstructor(String.class).newInstance(fieldName);
            } else {
                Constructor<? extends AnnotationPuller> constructor =
                        ((Class<? extends AnnotationPuller>) Class.forName(pullerClassName))
                                .getConstructor(String.class);
                puller = constructor.newInstance(fieldName);
            }
        } catch (ReflectiveOperationException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(CAS cas) throws AnalysisEngineProcessException {
        JCas readView;
        try {
            readView = cas.getView(readViewName).getJCas();
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }

        // Get all annotations of this class and toss them to the DataListener
        for (Annotation a : readView.getAnnotationIndex(typeClass)) {
            listener.listen(puller.transform(a));
        }
    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
        Summarizer summarizer;
        try {
            summarizer = ((Class<? extends Summarizer>) Class.forName(summarizerClass)).getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new AnalysisEngineProcessException(e);
        }
        try {
            OutputStream outputStream;
            if (outputPath == null) {
                outputStream = System.out;
            } else {
                outputStream = new FileOutputStream(outputPath);
            }
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write("Summary for " + listenerName + ":\n");
            writer.write(summarizer.summarize(DataListener.getDataListener(listenerName).regurgitate()).toString());
        } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

}