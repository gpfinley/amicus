package edu.umn.ensembles;

import org.apache.uima.UIMAFramework;
import org.apache.uima.collection.CollectionProcessingEngine;
import org.apache.uima.collection.metadata.CpeDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Run the application. If no argument provided, will assume 'merge'. Command line usage:
 *
 * java -Dconfig=[path-to-config-file] -jar [path-to-jar] [merge/extract/evaluate/path-to-CPE]
 *
 * Created by gpfinley on 12/9/16.
 */
@Deprecated
public class Main {

    private static final String MERGE = "merge";
    private static final String EXTRACT = "extract";
    private static final String EVALUATE = "evaluate";

    private static final String MERGE_CPE = "MergingCPE.xml";
    private static final String EXTRACT_CPE = "AnnotationToTextCPE.xml";
    private static final String EVALUATE_CPE = "EvaluationCPE.xml";

    public static void main(String[] args) {
        String process = args.length > 0 ? args[0] : MERGE;
        Path descriptorsPath = Paths.get("descriptors");
        Path cpeDescPath = null;
        if (process.equalsIgnoreCase(MERGE)) {
            cpeDescPath = descriptorsPath.resolve(MERGE_CPE);
        } else if(process.equalsIgnoreCase(EXTRACT)) {
            cpeDescPath = descriptorsPath.resolve(EXTRACT_CPE);
        } else if(process.equalsIgnoreCase(EVALUATE)) {
            cpeDescPath = descriptorsPath.resolve(EVALUATE_CPE);
        } else {
            File file = new File(process);
            if (file.exists()) {
                cpeDescPath = file.toPath();
            } else {
                System.out.println("Usage: first argument should be 'merge', 'extract', 'evaluate', or the path to a Collection Processing Engine descriptor file.");
                System.exit(1);
            }
        }

        CpeDescription cpeDesc = xmlToCpe(cpeDescPath);
        runCpe(cpeDesc);
    }

    public static CpeDescription xmlToCpe(Path path) {
        try {
            XMLInputSource xmlInputSource = new XMLInputSource(path.toString());
            return UIMAFramework.getXMLParser().parseCpeDescription(xmlInputSource);
        } catch (InvalidXMLException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public static void runCpe(CpeDescription cpeDesc) {

        // todo: put typesystem imports into file (like the Python script)

        CollectionProcessingEngine cpe;
        try {
            cpe = UIMAFramework.produceCollectionProcessingEngine(cpeDesc);
        } catch (ResourceInitializationException e) {
            e.printStackTrace();
            throw new EnsemblesException();
        }

        try {
            cpe.process();
        } catch (ResourceInitializationException e) {
            e.printStackTrace();
            throw new EnsemblesException();
        }
    }
}