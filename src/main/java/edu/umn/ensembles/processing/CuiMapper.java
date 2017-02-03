package edu.umn.ensembles.processing;

import edu.umn.ensembles.config.ClassConfigurationLoader;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Maps CUIs to their preferred string forms in the UMLS.
 *
 * Created by gpfinley on 10/13/16.
 */
public class CuiMapper implements Mapper<String, String> {

    private static Logger LOGGER = Logger.getLogger(CuiMapper.class.getName());

    private static CuiMapper cuiMapper;

    protected static class Config {
        String mrconsoPath;
        String language;
        Set<String> cuisToUse;
    }

    protected final Map<String, String> cuiToString;

    /**
     * Load default string forms of CUIs as given in the UMLS MRCONSO.RRF file.
     * Each parameter can be null
     *
     * @param mrconsoPath path to your UMLS distribution's MRCONSO.RRF file (if null: do not map CUIs to strings).
     * @param language the three-letter language code to use (if null: load all languages).
     * @param cuisToUse save memory by only loading these CUIs (leave null to load all CUIs)
     */
    protected CuiMapper(String mrconsoPath, String language, Set<String> cuisToUse) {
        if (mrconsoPath == null) {
            cuiToString = new HashMap<>();
            LOGGER.info("Will not map CUIs to concept names");
            return;
        }
        LOGGER.info("Loading CUIs and their preferred terms");
        cuiToString = new HashMap<>();
        Pattern bar = Pattern.compile("\\|");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(mrconsoPath));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = bar.split(line);
                if (cuisToUse != null && !cuisToUse.contains(fields[0])) continue;
                // Some logic required to determine the exact most preferred form of the CUI
                if ((language == null || fields[1].equals(language)) && fields[2].equals("P") && fields[4].equals("PF") && fields[6].equals("Y")) {
                    cuiToString.put(fields[0], fields[14]);
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public String map(String cui) {
        return cuiToString.getOrDefault(cui, cui);
    }

    public static CuiMapper getInstance() {
        if (cuiMapper == null) {
            try {
                Config config = ((Config) ClassConfigurationLoader.load(CuiMapper.class));
                cuiMapper = new CuiMapper(config.mrconsoPath, config.language, config.cuisToUse);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
        return cuiMapper;
    }

    public static void main(String[] args) {
        getInstance();
    }
}
