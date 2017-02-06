package edu.umn.ensembles.config;

/**
 * All necessary configuration information for a single type to be merged.
 * Saved as part of the yaml serialization of the AppConfiguration class.
 *
 * Created by gpfinley on 10/24/16.
 */
public class SingleOutputConfig {
    public String annotationType;
    public String annotationField;
    public String distillerClass;
    public String creatorClass;
    public String writeView;

    /**
     * Verify that these mergers have enough config info
     * For outputs, there are always defaults available
     */
    public void verify() { }

}