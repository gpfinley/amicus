package edu.umn.amicus.internal.coreference;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.umn.amicus.AmicusException;
import edu.umn.amicus.uimacomponents.Util;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

/**
 * Runs most of the Stanford CoreNLP pipeline, then patches in new annotations based on concepts in the jCas.
 * (These new annotations are not UIMA.)
 * Then runs the CoreNLP coreference module using the new annotation set.
 * Outputs pairs and chains (todo: also implement Craft format?)
 *
 * Created by gpfinley on 10/18/16.
 */
public class StanfordNerInterceptor extends JCasAnnotator_ImplBase {

    private static final Logger LOGGER = Logger.getLogger(StanfordNerInterceptor.class.getName());

    public static final String CONFIG_VIEW_NAME = "viewName";
    public static final String CONFIG_ANNOTATION_CLASS = "inputAnnotationClass";
    public static final String CONFIG_ANNOTATION_FIELD = "inputAnnotationField";
    public static final String TEXT_OUTPUT_PATH = "textOutputPath";
    public static final String IGNORE_NER = "ignoreNer";

    @ConfigurationParameter(name = CONFIG_VIEW_NAME, defaultValue = "_InitialView")
    private String viewName;
    @ConfigurationParameter(name = CONFIG_ANNOTATION_CLASS, defaultValue = "edu.umn.amicus.SingleFieldAnnotation")
    private String className;
    @ConfigurationParameter(name = CONFIG_ANNOTATION_FIELD, defaultValue = "field")
    private String fieldName;
    @ConfigurationParameter(name = TEXT_OUTPUT_PATH, defaultValue = "///")
    private String outPath;
    @ConfigurationParameter(name = IGNORE_NER, defaultValue = "false")
    private boolean ignoreNer;

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
     * Copied from Amicus utility class. Wanted this annotator to be able to stand alone.
     * @param field
     * @return
     */
    private static String getGetterFor(String field) {
        return "get" + field.substring(0, 1).toUpperCase() + field.substring(1, field.length());
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        JCas relevantView;
        try {
            relevantView = jCas.getView(viewName);
        } catch (CASException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        String docText = relevantView.getSofaDataString();

        // run CoreNLP on the SofaString (original text of the document)
        Annotation stanfordDocument = new Annotation(docText);

        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
        StanfordCoreNLP frontPipeline = new StanfordCoreNLP(props);
        frontPipeline.annotate(stanfordDocument);

        // intercept named entities from another source, if we haven't been told not to
        if (!ignoreNer) {
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

            // loop through CoreNLP words. Add/override named entity annotations where they exist in the CAS
            List<CoreMap> sentences = stanfordDocument.get(CoreAnnotations.SentencesAnnotation.class);
            for (CoreMap sentence : sentences) {
                for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                    int begin = token.beginPosition();
                    int end = token.endPosition();
                    for (int i = begin; i < end; i++) {
                        if (namedEntityAnnotations[i] != null) {
                            token.set(CoreAnnotations.NamedEntityTagAnnotation.class, namedEntityAnnotations[i]);
                            break;
                        }
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
//        System.out.println(graph);

        if (!"///".equals(outPath)) {

            // build arrays to look up line and word offsets (as used by i2b2) from character offsets
            // also build lists to map CoreNLP sentences and words to character offsets
            // to convert between CoreNLP and i2b2, look up in latter, then in former

            int[] lineAtThisChar = new int[docText.length()];
            int[] wordAtThisChar = new int[docText.length()];
            {
                int curLine = 1;
                int curWord = 0;
                for (int i=0; i<docText.length(); i++) {
                    lineAtThisChar[i] = curLine;
                    wordAtThisChar[i] = curWord;
                    char c = docText.charAt(i);
                    if (c == ' ') {
                        curWord++;
                    } else if (c == '\n') {
                        curLine++;
                        curWord = 0;
                    }
                }
            }

            // store the character offsets for given words of given sentences (all indexed from zero here)
            List<List<Integer>> sentenceWordOffsets = new ArrayList<>();
            List<CoreMap> sentences = stanfordDocument.get(CoreAnnotations.SentencesAnnotation.class);
            for(CoreMap sentence: sentences) {
                List<Integer> wordOffsets = new ArrayList<>();
                sentenceWordOffsets.add(wordOffsets);
                for (CoreLabel c : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                    wordOffsets.add(c.beginPosition());
                }
                // java 8
//                sentence.get(CoreAnnotations.TokensAnnotation.class).forEach(t -> wordOffsets.add(t.beginPosition()));
            }

            try {
                Files.createDirectories(Paths.get(outPath));
            } catch (IOException e) {
                throw new AnalysisEngineProcessException(e);
            }
            StringBuilder toWrite = new StringBuilder();
            for (CorefChain chain : graph.values()) {
                for (Set<CorefChain.CorefMention> corefMentions : chain.getMentionMap().values()) {
                    if (corefMentions.size() > 1) {
                        System.out.println("~~~~~");
                        for (CorefChain.CorefMention cm : corefMentions) {
                            String coveredText = cm.mentionSpan;

                            // subtract one from sentNum and startIndex b/c these indices start at 1
                            int startIndex = sentenceWordOffsets.get(cm.sentNum-1).get(cm.startIndex-1);
                            // subtract one from end index so as not to hit the next word (i2b2 indices are inclusive)
                            int endIndex = sentenceWordOffsets.get(cm.sentNum-1).get(cm.endIndex-2);
                            int startLine = lineAtThisChar[startIndex];
                            int endLine = lineAtThisChar[endIndex];
                            int startWord = wordAtThisChar[startIndex];
                            int endWord = wordAtThisChar[endIndex];

                            // todo: debug only
                            System.out.println(Util.getDocumentID(jCas.getCas()));
                            System.out.println(cm.mentionSpan);
                            System.out.println(startLine);
                            System.out.println(startWord);
                            System.out.println(endLine);
                            System.out.println(endWord);

                            toWrite.append("c=\"");
                            toWrite.append(coveredText);
                            toWrite.append("\" ");
                            toWrite.append(startLine);
                            toWrite.append(":");
                            toWrite.append(startWord);
                            toWrite.append(" ");
                            toWrite.append(endLine);
                            toWrite.append(":");
                            toWrite.append(endWord);
                            toWrite.append("||");
                        }
                        // Not bothering to differentiate coref chains by type (i2b2 eval better not use -i flag!).
                        toWrite.append("t=\"coref pronoun\"\n");
                    }

                }

            }
            try {
                String docId = Util.getDocumentID(jCas.getCas());
                Path outFilePath = Paths.get(outPath).resolve(docId);
                Files.write(outFilePath, toWrite.toString().getBytes());
            } catch (IOException e) {
                throw new AnalysisEngineProcessException(e);
            }

        } else {
            LOGGER.warning("No text output path specified; not saving coreference chains to text.");
        }


    }

}
