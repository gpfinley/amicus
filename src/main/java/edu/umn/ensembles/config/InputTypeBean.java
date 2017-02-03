package edu.umn.ensembles.config;

import edu.umn.ensembles.EnsemblesException;

/**
 * All necessary configuration information for a single type to be merged.
 * Saved as part of the yaml serialization of the AppConfiguration class.
 *
 * Created by gpfinley on 10/24/16.
 */
public class InputTypeBean {
    private String type;
    private String fromSystem;
    private String field;
    private String transformerClass;

    public void verify() {
        /**
         * Verify that these mergers have enough config info
         */
        if (type == null || fromSystem == null) {
            throw new EnsemblesException("Input type configuration incomplete");
        }
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFromSystem() {
        return fromSystem;
    }

    public void setFromSystem(String fromSystem) {
        this.fromSystem = fromSystem;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getTransformerClass() {
        return transformerClass;
    }

    public void setTransformerClass(String transformerClass) {
        this.transformerClass = transformerClass;
    }
}