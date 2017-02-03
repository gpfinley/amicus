package edu.umn.ensembles.internal;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.umn.ensembles.EnsemblesException;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

/**
 * Runs most of the Stanford CoreNLP pipeline, then patches in new annotations based on concepts in the jCas.
 * (These new annotations are not UIMA.)
 * Then runs the CoreNLP coreference module using the new annotation set.
 * Outputs pairs and chains (todo: in what format??)
 *
 * Created by gpfinley on 10/18/16.
 */
public class StanfordNerInterceptor extends JCasAnnotator_ImplBase {

    private static final Logger LOGGER = Logger.getLogger(StanfordNerInterceptor.class.getName());

    public static final String CONFIG_VIEW_NAME = "viewName";
    public static final String CONFIG_ANNOTATION_CLASS = "inputAnnotationClass";
    public static final String CONFIG_ANNOTATION_FIELD = "inputAnnotationField";
    public static final String TEXT_OUTPUT_PATH = "textOutputPath";

    @ConfigurationParameter(name = CONFIG_VIEW_NAME)
    private String viewName;
    @ConfigurationParameter(name = CONFIG_ANNOTATION_CLASS)
    private String className;
    @ConfigurationParameter(name = CONFIG_ANNOTATION_FIELD)
    private String fieldName;
    @ConfigurationParameter(name = TEXT_OUTPUT_PATH)
    private String outPath;

    private Class<? extends org.apache.uima.jcas.tcas.Annotation> inputNeAnnotation;
    private Method getter;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);

        try {
            inputNeAnnotation = (Class<? extends org.apache.uima.jcas.tcas.Annotation>) Class.forName(className);
        } catch(ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch(ClassCastException e) {
            LOGGER.severe(String.format("Class type %s provided in StanfordNerInterceptor config is not an Annotation",
                    className));
            throw new RuntimeException();
        }
        String getterName = getGetterFor(fieldName);
        try {
            getter = inputNeAnnotation.getMethod(getterName);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    /**
     * Copied from Ensembles utility class. Wanted this annotator to be able to stand alone.
     * @param field
     * @return
     */
    private static String getGetterFor(String field) {
        return "get" + field.substring(0, 1).toUpperCase() + field.substring(1, field.length());
    }

    @Override
    public void process(JCas jCas) {
        JCas relevantView;
        try {
            relevantView = jCas.getView(viewName);
        } catch (CASException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        String docText = relevantView.getSofaDataString();

        // loop through UIMA annotations and set character indices to the type of annotation
        // (could also do this with a map)
        String[] namedEntityAnnotations = new String[docText.length()];
        FSIterator iter = relevantView.getAnnotationIndex(inputNeAnnotation).iterator();
        while (iter.hasNext()) {
            org.apache.uima.jcas.tcas.Annotation annot = inputNeAnnotation.cast(iter.next());
            String neType;
            try {
                neType = getter.invoke(annot).toString();
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
            for (int i = annot.getBegin(); i < annot.getEnd(); i++) {
                namedEntityAnnotations[i] = neType;
            }
        }

        // run CoreNLP on the SofaString (original text of the document)
        Annotation stanfordDocument = new Annotation(docText);

        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
        StanfordCoreNLP frontPipeline = new StanfordCoreNLP(props);
        frontPipeline.annotate(stanfordDocument);


        // loop through CoreNLP words. Add/override named entity annotations where they exist in the UIMA document
        List<CoreMap> sentences = stanfordDocument.get(CoreAnnotations.SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                int begin = token.beginPosition();
                int end = token.endPosition();
                for (int i=begin; i<end; i++) {
                    if (namedEntityAnnotations[i] != null) {
                        token.set(CoreAnnotations.NamedEntityTagAnnotation.class, namedEntityAnnotations[i]);
                        break;
                    }
                }
            }
        }

        // set up a pipeline consisting just of the coreference resolver
        Properties corefProps = new Properties();
        corefProps.setProperty("annotators", "dcoref");
        StanfordCoreNLP corefPipeline = new StanfordCoreNLP(corefProps, false);     // false = ignore annotation dependencies
        corefPipeline.annotate(stanfordDocument);

        // This is the coreference link graph
        // Each chain stores a set of mentions that link to each other,
        // along with a method for getting the most representative mention
        // Both sentence and token offsets start at 1!
        Map<Integer, CorefChain> graph =
                stanfordDocument.get(CorefCoreAnnotations.CorefChainAnnotation.class);
        System.out.println(graph);

        // todo: export graph in i2b2 format (line numbers starting at 1, word offsets starting at 0)

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outPath));

            for (CorefChain chain : graph.values()) {



            }

        } catch (IOException e) {
            throw new EnsemblesException(e);
        }



    }

}





// todo: Delete graveyard

//
//        // todo: delete the whole following chunk
//        // these are all the sentences in this document
//        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
//        List<CoreMap> sentences = stanfordDocument.get(CoreAnnotations.SentencesAnnotation.class);
//        for(CoreMap sentence: sentences) {
//            System.out.println(sentence);
//            // traversing the words in the current sentence
//            // a CoreLabel is a CoreMap with additional token-specific methods
//            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
//                // this is the text of the token
//                String word = token.get(CoreAnnotations.TextAnnotation.class);
//                // this is the POS tag of the token
//                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
//                // this is the NER label of the token
////                token.set(CoreAnnotations.NamedEntityTagAnnotation.class, "dummy");
//                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
//                System.out.println(ne);
//            }
//            // this is the parse tree of the current sentence
//            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
//            // this is the Stanford dependency graph of the current sentence
//            SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
//        }
//
//        // todo: delete below
//        // todo: is this actually what we want...or the reverse of what we want??
//        // map sentence/word indices to character offsets
//        // list of sentences, each with a list of words
//        List<List<Integer>> wordBegins = new ArrayList<>();
//        List<List<Integer>> wordEnds = new ArrayList<>();
//        // pad with nulls (sentence numberings start from 1)
//        wordBegins.add(null);
//        wordEnds.add(null);
//        List<CoreMap> sentences = stanfordDocument.get(CoreAnnotations.SentencesAnnotation.class);
//        for(CoreMap sentence: sentences) {
//            List<Integer> theseBegins = new ArrayList<>();
//            List<Integer> theseEnds = new ArrayList<>();
//            // pad with nulls (word numberings start from 1)
//            theseBegins.add(null);
//            theseEnds.add(null);
//            wordBegins.add(theseBegins);
//            wordEnds.add(theseEnds);
//            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
//                theseBegins.add(token.beginPosition());
//                theseEnds.add(token.beginPosition());
//            }
//        }
//
//        // Find all annotations in the jCas and write a new Stanford NER annotation for each one
//        // todo: figure out word/sentence-to-character mapping (system annotations will be char offsets; Stanford wants whole words)
//        jCas.getAnnotationIndex().iterator().forEachRemaining(a -> {
//            // todo: use Biomed concept annotation or something else?
//            ConceptAnnotation concept;
//            try {
//                concept = (ConceptAnnotation) a;
//            } catch(ClassCastException e) {
//                return;
//            }
//
//            Now--find the words and sentences that these offsets map to and set new stanford NER annotations.
//
//            // todo: add NER annotations to the document from the jCas
//        });