package edu.umn.ensembles.config;

import edu.umn.ensembles.EnsemblesException;

/**
 * All necessary configuration information for a single type to be merged.
 * Saved as part of the yaml serialization of the AppConfiguration class.
 *
 * Created by gpfinley on 10/24/16.
 */
public class SingleInputConfig {
    public String annotationType;
    public String annotationField;
    public String fromSystem;
    public String transformerClass;

    /**
     * Verify that these mergers have enough config info
     */
    public void verify() {
        if (fromSystem == null) {
            throw new EnsemblesException("Input type configuration incomplete");
        }
    }
}