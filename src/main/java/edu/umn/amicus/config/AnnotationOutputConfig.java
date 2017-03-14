package edu.umn.amicus.config;

import edu.umn.amicus.AmicusConfigurationException;

/**
 * All necessary configuration information for a single type to be merged.
 * Saved as part of the yaml serialization of the AppConfiguration class.
 *
 * Created by gpfinley on 10/24/16.
 */
public class AnnotationOutputConfig {
    public String annotationType;
    public String annotationField;
    public String distiller;
    public String pusher;
    public String writeView;

    /**
     * Verify that these mergers have enough config info
     * For outputs, there are always defaults available except for writeView
     */
    public void verify() {
        if (writeView == null) {
            throw new AmicusConfigurationException("Output configuration incomplete: need to specify 'writeView'");
        }
    }

    // builder-style setters to more easily code up a configuration

    public AnnotationOutputConfig annotationType(String annotationType) {
        this.annotationType = annotationType;
        return this;
    }
    public AnnotationOutputConfig annotationField(String annotationField) {
        this.annotationField = annotationField;
        return this;
    }
    public AnnotationOutputConfig distillerClass(String distillerClass) {
        this.distiller = distillerClass;
        return this;
    }
    public AnnotationOutputConfig pusherClass(String creatorClass) {
        this.pusher = creatorClass;
        return this;
    }
    public AnnotationOutputConfig writeView(String writeView) {
        this.writeView = writeView;
        return this;
    }

}