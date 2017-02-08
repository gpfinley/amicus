package edu.umn.ensembles.config;

import edu.umn.ensembles.Ensembles;

/**
 * All necessary configuration information for a single type to be merged.
 * Saved as part of the yaml serialization of the AppConfiguration class.
 *
 * Created by gpfinley on 10/24/16.
 */
public class SingleOutputConfig {
    public String annotationType;
    public String annotationField;
    public String distillerClass = Ensembles.DEFAULT_DISTILLER_CLASS.getName();
    public String creatorClass = Ensembles.DEFAULT_CREATOR_CLASS.getName();
    public String writeView = Ensembles.DEFAULT_MERGED_VIEW;

    /**
     * Verify that these mergers have enough config info
     * For outputs, there are always defaults available
     */
    public void verify() { }

    // builder-style setters to more easily code up a configuration

    public SingleOutputConfig annotationType(String annotationType) {
        this.annotationType = annotationType;
        return this;
    }
    public SingleOutputConfig annotationField(String annotationField) {
        this.annotationField = annotationField;
        return this;
    }
    public SingleOutputConfig distillerClass(String distillerClass) {
        this.distillerClass = distillerClass;
        return this;
    }
    public SingleOutputConfig creatorClass(String creatorClass) {
        this.creatorClass = creatorClass;
        return this;
    }
    public SingleOutputConfig writeView(String writeView) {
        this.writeView = writeView;
        return this;
    }

}