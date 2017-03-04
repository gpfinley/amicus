package edu.umn.amicus;

import edu.umn.amicus.aligners.EachSoloAligner;
import edu.umn.amicus.export.EachSoloTsvExportWriter;
import org.yaml.snakeyaml.Yaml;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Configuration, constants, and simple methods used by this application.
 *
 * Created by gpfinley on 10/17/16.
 */
public final class Amicus {

    public final static Path CLASS_CONFIG_DIR = Paths.get("classConfigurations");

    public final static Class<EachSoloTsvExportWriter> DEFAULT_EXPORTER_CLASS = EachSoloTsvExportWriter.class;
    public final static Class<EachSoloAligner> DEFAULT_ALIGNER_CLASS_FOR_EXPORTER = EachSoloAligner.class;

    public final static String CONCATENATED_STRING_DELIMITER = "|";

    /**
     * View names used by UIMA modules
     */
    // view holding the basename of all XMI files
    public static final String DOCID_VIEW = "_InitialView";

    // todo: get rid of this?
    // view holding the contents of the merging analysis
    public static final String DEFAULT_MERGED_VIEW = "MergedView";

    public final static Yaml yaml = new Yaml();

    // set this once. If a sofa is based on a different string, we have a problem
    private static String sofaData;

    public static void verifySofaData(String sofaData) throws AmicusException {
        if (Amicus.sofaData == null) {
            Amicus.sofaData = sofaData;
        } else {
            if (!Amicus.sofaData.equals(sofaData)) {
                throw new AmicusException("Data does not match across sofas!");
            }
        }
    }

}
