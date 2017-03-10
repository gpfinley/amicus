package edu.umn.amicus;

import org.yaml.snakeyaml.Yaml;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration, constants, and simple methods used by this application.
 *
 * Created by gpfinley on 10/17/16.
 */
public final class Amicus {

    public final static Path CLASS_CONFIG_DIR = Paths.get("classConfigurations");

    public final static String ANNOTATION_FIELD_DELIMITER = ";";
    public final static String LIST_AS_STRING_DELIMITER = "|";

    /**
     * View names used by UIMA modules
     */
    // view holding the basename of all XMI files
    public static final String DOCID_VIEW = "_InitialView";

    public final static Yaml yaml = new Yaml();

    // set this once. If a sofa is based on a different string, we have a problem
    private final static Map<String, Object> sofaData = new HashMap<>();

    public static void verifySofaData(String docId, Object sofaData) throws MismatchedSofaDataException {
        Object oldSofaData = Amicus.sofaData.get(docId);
        if (oldSofaData == null) {
            Amicus.sofaData.put(docId, sofaData);
            return;
        }
        if (!oldSofaData.equals(sofaData)) {
            throw new MismatchedSofaDataException();
        }
    }

    public static Object getSofaData(String docId) {
        return sofaData.get(docId);
    }

}
