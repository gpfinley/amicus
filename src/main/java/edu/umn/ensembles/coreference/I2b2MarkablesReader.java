package edu.umn.ensembles.coreference;

import edu.umn.ensembles.EnsemblesException;
import edu.umn.ensembles.SingleFieldAnnotation;
import edu.umn.ensembles.uimacomponents.Util;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.CasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import java.io.*;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

/**
 * Load gold standard markables as defined for the 2011 i2b2 coreference resolution challenge.
 * Will store these in simple UIMA annotations as defined in this app's TypeSystem.
 * If no markables directory is given, will just load the text files into CASes with no other annotations.
 *
 * Created by gpfinley on 1/27/17.
 */
public class I2b2MarkablesReader extends CasCollectionReader_ImplBase {

    private static final Logger LOGGER = Logger.getLogger(I2b2MarkablesReader.class.getName());

    private int index;
    private final List<String> textFilePaths = new ArrayList<>();
    private final List<String> markableFilePaths = new ArrayList<>();

    public static final String TEXT_DIRECTORY = "textDirectory";
    public static final String MARKABLES_DIRECTORY = "markablesDirectory";

    @ConfigurationParameter(name = TEXT_DIRECTORY)
    private String textDirectory;
    @ConfigurationParameter(name = MARKABLES_DIRECTORY, mandatory = false)
    private String markablesDirectory;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        Path textPath = Paths.get(textDirectory);
        Path markablesPath = Paths.get(markablesDirectory);

        for (File file : new File(textDirectory).listFiles()) {
            String fileName = file.getName();
            if (fileName.startsWith(".")) continue;
            textFilePaths.add(textPath.resolve(fileName).toString());
            if (markablesDirectory != null) {
                markableFilePaths.add(markablesPath.resolve(fileName + ".con").toString());
            }
        }

        index = 0;
    }

    /**
     * Add a view and annotation to this CAS containing paths to the XMI outputs of both systems
     * @param cas
     */
    @Override
    public void getNext(CAS cas) {
        JCas jCas;
        try {
            jCas = cas.getJCas();
        } catch (CASException e) {
            throw new EnsemblesException(e);
        }
        String text;
        try {
            text = new Scanner(new File(textFilePaths.get(index))).useDelimiter("\\Z").next();
        } catch(FileNotFoundException e) {
            throw new EnsemblesException(e);
        }
        cas.setSofaDataString(text, "text");

        if (markablesDirectory != null) {
            File markablesFile = new File(markableFilePaths.get(index));

            String[] lines = text.split("\\n");
            // get the number of chars before each line to facilitate character index lookup later
            int[] charsBeforeThisLine = new int[lines.length];
            for (int i = 1; i < lines.length; i++) {
                charsBeforeThisLine[i] = charsBeforeThisLine[i - 1] + lines[i - 1].length() + 1;
            }
            // starts of each word in the line (may or may not be filled for each line)
            List[] wordStarts = new List[lines.length];

            try {
                BufferedReader reader = new BufferedReader(new FileReader(markablesFile));
                String nextLine;
                while ((nextLine = reader.readLine()) != null) {

                    // split the line on pipes, then on space or colon
                    String[] fields = nextLine.split("\\|")[0].split("[\\s:]+");
                    // entity type will be the last thing bounded by quotes
                    String entityType = nextLine.substring(nextLine.substring(nextLine.length() - 1).lastIndexOf('"'), nextLine.length() - 1);
                    int beginLine = Integer.parseInt(fields[fields.length - 4]) - 1;
                    int beginWord = Integer.parseInt(fields[fields.length - 3]);
                    int endLine = Integer.parseInt(fields[fields.length - 2]) - 1;
                    int endWord = Integer.parseInt(fields[fields.length - 1]);

                    // if the begin and/or end lines haven't had their words indexed yet, do that
                    for (int lineNum : new int[]{beginLine, endLine}) {
                        if (wordStarts[lineNum] == null) {
                            wordStarts[lineNum] = new ArrayList<Integer>();
                            wordStarts[lineNum].add(0);
                            for (int c = 0; c < lines[lineNum].length(); c++) {
                                if (lines[lineNum].charAt(c) == ' ') wordStarts[lineNum].add(c + 1);
                            }
                            wordStarts[lineNum].add(lines[lineNum].length());
                        }
                    }
                    int begin = charsBeforeThisLine[beginLine] + (Integer) wordStarts[beginLine].get(beginWord);
                    int end = charsBeforeThisLine[endLine] + (Integer) wordStarts[endLine].get(endWord + 1) - 1;
                    SingleFieldAnnotation annotation = new SingleFieldAnnotation(jCas);
                    annotation.setBegin(begin);
                    annotation.setEnd(end);
                    annotation.setField(entityType);
                    annotation.addToIndexes();
                }
            } catch (IOException e) {
                throw new EnsemblesException(e);
            }
        }
        Util.setDocumentID(cas, textFilePaths.get(index).substring(1+textFilePaths.get(index).lastIndexOf('/')));
        index++;
    }

    @Override
    public Progress[] getProgress() {
        return new Progress[] { new ProgressImpl(index, textFilePaths.size(), Progress.ENTITIES) };
    }

    @Override
    public boolean hasNext() {
        return index < textFilePaths.size();
    }

    @Override
    public void close() { }

}
