package edu.umn.ensembles.uimacomponents;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.component.CasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Reads the names of files common between NLP system output directories.
 *
 * Created by gpfinley on 10/14/16.
 * Converted to use UIMA config parameters (for uimaFIT use) rather than application params on 1/18/17.
 */
public class CommonFilenameCR extends CasCollectionReader_ImplBase {

    private static final Logger LOGGER = Logger.getLogger(CommonFilenameCR.class.getName());

    public static final String SYSTEM_DATA_DIRS = "systemDataDirectories";

    @ConfigurationParameter(name = SYSTEM_DATA_DIRS)
    private String[] dirnameStrings;

    private List<String> fileIDs;
    private int index;

    @Override
    /**
     * Loads all filenames from the two specified input directories and finds those with common names
     */
    public void initialize(UimaContext context) {
        LOGGER.info("Initializing filename reader.");

        Function<String, String> chopExt = new Function<String, String>() {
            @Override
            public String apply(String s) {
                return s.substring(0, s.lastIndexOf('.'));
            }
        };
        // java 8
//        Function<String, String> chopExt = s -> s.substring(0, s.lastIndexOf('.'));

        List<File> directories = new ArrayList<>();
        for (String d : dirnameStrings) {
            directories.add(new File(d));
        }
        // java 8
//        List<File> directories = Arrays.asList(dirnameStrings)
//                .stream()
//                .map(File::new)
//                .collect(Collectors.toList());

        Set<String> commonNames = null;
        for (File dir : directories) {
            if (!dir.exists()) {
                LOGGER.severe(String.format("Directory %s does not exist; check config file!", dir.getAbsolutePath()));
                throw new RuntimeException();
            }
            Set<String> filesThisDir = new HashSet<>();
            for (File f : dir.listFiles()) {
                filesThisDir.add(chopExt.apply(f.getName()));
            }
            // java 8
//            Set<String> filesThisDir = Arrays.asList(dir.listFiles())
//                    .stream()
//                    .map(File::getName)
//                    .map(chopExt)
//                    .collect(Collectors.toSet());
            if (commonNames == null) {
                commonNames = filesThisDir;
            } else {
                commonNames.retainAll(filesThisDir);
            }
        }
        commonNames.remove("TypeSystem");
        commonNames.remove("");
        fileIDs = new ArrayList<>(commonNames);
    }

    /**
     * Add a view and annotation to this CAS containing paths to the serialized CASes of all systems
     * @param cas
     */
    @Override
    public void getNext(CAS cas) {
        Util.setDocumentID(cas, fileIDs.get(index));
        index++;
    }

    @Override
    public Progress[] getProgress() {
        return new Progress[] { new ProgressImpl(index, fileIDs.size(), Progress.ENTITIES) };
    }

    @Override
    public boolean hasNext() {
        return index < fileIDs.size();
    }

    @Override
    public void close() { }

    /**
     * List subdirectories of this directory. Also used, for consistency, by MultiCasReader.
     * @param directory
     * @return
     */
    static List<File> getSubdirectories(File directory) {
        if (!directory.isDirectory()) {
            LOGGER.severe(directory + " is not a directory; check configuration parameters");
            throw new RuntimeException();
        }
        List<File> subDirs = new ArrayList<>();
        for (File f : directory.listFiles()) {
            if (f.isDirectory()) {
                subDirs.add(f);
            }
        }
        return subDirs;
        // java 8
//        return Arrays.asList(directory.listFiles()).stream().filter(x -> x.isDirectory()).collect(Collectors.toList());
    }
}
