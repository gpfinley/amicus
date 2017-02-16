package edu.umn.amicus.config;

import edu.umn.amicus.Amicus;

/**
 * All necessary configuration information for a single type to be merged.
 * Saved as part of the yaml serialization of the AppConfiguration class.
 *
 * Created by gpfinley on 10/24/16.
 */
public class AnnotationOutputConfig {
    public String annotationType;
    public String annotationField;
    public String distillerClass = Amicus.DEFAULT_DISTILLER_CLASS.getName();
    public String pusherClass = Amicus.DEFAULT_PUSHER_CLASS.getName();
    public String writeView = Amicus.DEFAULT_MERGED_VIEW;

    /**
     * Verify that these mergers have enough config info
     * For outputs, there are always defaults available
     */
    public void verify() { }

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
        this.distillerClass = distillerClass;
        return this;
    }
    public AnnotationOutputConfig pusherClass(String creatorClass) {
        this.pusherClass = creatorClass;
        return this;
    }
    public AnnotationOutputConfig writeView(String writeView) {
        this.writeView = writeView;
        return this;
    }

}