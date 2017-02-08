package edu.umn.ensembles.config;

import edu.umn.ensembles.EnsemblesException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A serializable bean for a single merge engine.
 * Contains methods for aggregating options across its inputs and outputs, which is needed for uimaFIT configurations.
 *
 * Created by gpfinley on 10/24/16.
 */ public class SingleMergerConfiguration {

    public String _name;

    public SingleInputConfig[] inputs;
    public SingleOutputConfig[] outputs;

    public String alignerClass;

    /**
     * Verify that these mergers have enough config info
     */
    public void verify() {
        if (inputs == null || inputs.length == 0
                || outputs == null) {
            throw new EnsemblesException("Translator or merger configuration incomplete");
        }
        Arrays.asList(inputs).forEach(SingleInputConfig::verify);
    }

    /**
     * Aggregating functions, used to convert Single*putConfigs into the String[]s needed as UIMA config params
     * @return
     */
    public String[] aggregateInputSystemNames() {
        List<String> inputSystemNames = new ArrayList<>();
        for (SingleInputConfig inputConfig : inputs) {
            inputSystemNames.add(inputConfig.fromSystem);
        }
        return inputSystemNames.toArray(new String[inputSystemNames.size()]);
    }
    public String[] aggregateInputTypes() {
        List<String> inputTypes = new ArrayList<>();
        for (SingleInputConfig inputConfig : inputs) {
            inputTypes.add(inputConfig.annotationType);
        }
        return inputTypes.toArray(new String[inputTypes.size()]);
    }
    public String[] aggregateInputFields() {
        List<String> inputFields = new ArrayList<>();
        for (SingleInputConfig inputConfig : inputs) {
            inputFields.add(inputConfig.annotationField);
        }
        return inputFields.toArray(new String[inputFields.size()]);
    }
    public String[] aggregateInputTransformers() {
        List<String> inputTransformers = new ArrayList<>();
        for (SingleInputConfig inputConfig : inputs) {
            inputTransformers.add(inputConfig.transformerClass);
        }
        return inputTransformers.toArray(new String[inputTransformers.size()]);
    }

    public String[] aggregateOutputAnnotationClasses() {
        String[] outputClasses = new String[outputs.length];
        for (int i=0; i<outputs.length; i++) {
            outputClasses[i] = outputs[i].annotationType;
        }
        return outputClasses;
    }
    public String[] aggregateOutputAnnotationFields() {
        String[] outputFields = new String[outputs.length];
        for (int i=0; i<outputs.length; i++) {
            outputFields[i] = outputs[i].annotationField;
        }
        return outputFields;
    }
    public String[] aggregateOutputViewNames() {
        String[] outputViews = new String[outputs.length];
        for (int i=0; i<outputs.length; i++) {
            outputViews[i] = outputs[i].writeView;
        }
        return outputViews;
    }
    public String[] aggregateCreatorClasses() {
        String[] creators = new String[outputs.length];
        for (int i=0; i<outputs.length; i++) {
            creators[i] = outputs[i].creatorClass;
        }
        return creators;
    }
    public String[] aggregateDistillerClasses() {
        String[] distillers = new String[outputs.length];
        for (int i=0; i<outputs.length; i++) {
            distillers[i] = outputs[i].distillerClass;
        }
        return distillers;
    }

}
