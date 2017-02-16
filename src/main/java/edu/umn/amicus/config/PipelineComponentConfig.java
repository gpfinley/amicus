package edu.umn.amicus.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic superclass for pipeline components: Mergers, Collectors, and Exporters.
 * Any actual processing is class-dependent and will require polling objects' types.
 *
 * Created by greg on 2/10/17.
 */
public abstract class PipelineComponentConfig {

    // TODO: should we make these static methods non-static and inherited? Will need to declare inputs and outputs
    // TODO:    for all implementing classes in that case, even if they don't have them (can through unsupported op exc)
//    // not all pipeline components will have both inputs and outputs
//    public AnnotationInputConfig[] inputs;
//    public AnnotationOutputConfig[] outputs;

    public abstract void verify();

    /**
     * Aggregating functions, used to convert Single*putConfigs into the String[]s needed as UIMA config params
     * @return
     */
    public static String[] aggregateInputSystemNames(AnnotationInputConfig[] inputs) {
        List<String> inputSystemNames = new ArrayList<>();
        for (AnnotationInputConfig inputConfig : inputs) {
            inputSystemNames.add(inputConfig.fromView);
        }
        return inputSystemNames.toArray(new String[inputSystemNames.size()]);
    }
    public static String[] aggregateInputTypes(AnnotationInputConfig[] inputs) {
        List<String> inputTypes = new ArrayList<>();
        for (AnnotationInputConfig inputConfig : inputs) {
            inputTypes.add(inputConfig.annotationType);
        }
        return inputTypes.toArray(new String[inputTypes.size()]);
    }
    public static String[] aggregateInputFields(AnnotationInputConfig[] inputs) {
        List<String> inputFields = new ArrayList<>();
        for (AnnotationInputConfig inputConfig : inputs) {
            inputFields.add(inputConfig.annotationField);
        }
        return inputFields.toArray(new String[inputFields.size()]);
    }
    public static String[] aggregateInputPullers(AnnotationInputConfig[] inputs) {
        List<String> inputTransformers = new ArrayList<>();
        for (AnnotationInputConfig inputConfig : inputs) {
            inputTransformers.add(inputConfig.pullerClass);
        }
        return inputTransformers.toArray(new String[inputTransformers.size()]);
    }

    public static String[] aggregateOutputAnnotationClasses(AnnotationOutputConfig[] outputs) {
        String[] outputClasses = new String[outputs.length];
        for (int i=0; i<outputs.length; i++) {
            outputClasses[i] = outputs[i].annotationType;
        }
        return outputClasses;
    }
    public static String[] aggregateOutputAnnotationFields(AnnotationOutputConfig[] outputs) {
        String[] outputFields = new String[outputs.length];
        for (int i=0; i<outputs.length; i++) {
            outputFields[i] = outputs[i].annotationField;
        }
        return outputFields;
    }
    public static String[] aggregateOutputViewNames(AnnotationOutputConfig[] outputs) {
        String[] outputViews = new String[outputs.length];
        for (int i=0; i<outputs.length; i++) {
            outputViews[i] = outputs[i].writeView;
        }
        return outputViews;
    }
    public static String[] aggregateOutputPushers(AnnotationOutputConfig[] outputs) {
        String[] creators = new String[outputs.length];
        for (int i=0; i<outputs.length; i++) {
            creators[i] = outputs[i].pusherClass;
        }
        return creators;
    }
    public static String[] aggregateOutputDistillers(AnnotationOutputConfig[] outputs) {
        String[] distillers = new String[outputs.length];
        for (int i=0; i<outputs.length; i++) {
            distillers[i] = outputs[i].distillerClass;
        }
        return distillers;
    }

}
