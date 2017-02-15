package edu.umn.ensembles.config;

import edu.umn.ensembles.Ensembles;
import edu.umn.ensembles.EnsemblesException;

/**
 * All necessary configuration information for a single type to be merged.
 * Saved as part of the yaml serialization of the AppConfiguration class.
 *
 * Created by gpfinley on 10/24/16.
 */
public class AnnotationInputConfig {
    public String annotationType;
    public String annotationField;
    public String fromView;
    public String pullerClass = Ensembles.DEFAULT_PULLER_CLASS.getName();

    /**
     * Verify that these mergers have enough config info
     */
    public void verify() {
        if (fromView == null) {
            throw new EnsemblesException("Input type configuration incomplete");
        }
    }

    // builder-style setters to more easily code up a configuration

    public AnnotationInputConfig annotationType(String annotationType) {
        this.annotationType = annotationType;
        return this;
    }
    public AnnotationInputConfig annotationField(String annotationField) {
        this.annotationField = annotationField;
        return this;
    }
    public AnnotationInputConfig fromView(String fromView) {
        this.fromView = fromView;
        return this;
    }
    public AnnotationInputConfig transformerClass(String transformerClass) {
        this.pullerClass = transformerClass;
        return this;
    }

}