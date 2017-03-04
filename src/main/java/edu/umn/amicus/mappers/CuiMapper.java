package edu.umn.amicus.mappers;

import edu.umn.amicus.AmicusException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Maps CUIs to their preferred string forms in the UMLS.
 *
 * Created by gpfinley on 10/13/16.
 */
public class CuiMapper extends Mapper {

    private static Logger LOGGER = Logger.getLogger(CuiMapper.class.getName());

    private String mrconsoPath;
    private String language = "ENG";
    private Set<String> cuisToUse;

    @Override
    protected Object mappingFunction(Object cuiString) throws AmicusException{
        if (cuiString == null) return null;
        if (internalMap == null) {
            initialize();
        }
        String cui = cuiString.toString().toUpperCase();
        if (internalMap.containsKey(cui)) return internalMap.get(cui);
        return cuiString;
    }

    /**
     * todo: doc
     * todo: should this just be put in the constructor? the concurrent map should already ensure only a single instance of this
     */
    public void initialize() throws AmicusException {
        if (mrconsoPath == null) {
            throw new AmicusException("Need to provide path to UMLS MRCONSO.RRF file in CuiMapper config.");
        }
        LOGGER.info("Loading CUIs and their preferred terms");
        internalMap = new HashMap<>();
        Pattern bar = Pattern.compile("\\|");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(mrconsoPath));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = bar.split(line);
                if (cuisToUse == null || cuisToUse.contains(fields[0])) {
                    // Some logic required to determine the exact most preferred form of the CUI
                    if ((language == null || fields[1].equals(language)) && fields[2].equals("P") && fields[4].equals("PF") && fields[6].equals("Y")) {
                        internalMap.put(fields[0], fields[14]);
                    }
                }
            }
        } catch(IOException e) {
            throw new AmicusException(e);
        }
    }

    /*
     * Getters and setters for YAML
     */

    public String getMrconsoPath() {
        return mrconsoPath;
    }

    public void setMrconsoPath(String mrconsoPath) {
        this.mrconsoPath = mrconsoPath;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Set<String> getCuisToUse() {
        return cuisToUse;
    }

    public void setCuisToUse(Set<String> cuisToUse) {
        this.cuisToUse = cuisToUse;
    }

}
