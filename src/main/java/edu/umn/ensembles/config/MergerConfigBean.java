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
 */
public class MergerConfigBean {

    public String _name;

    public SingleInputConfig[] inputs;
    public SingleOutputConfig[] outputs;

    // what kind of type to output
    public String outputAnnotationClass;
    // can have multiple if separated by ;
    public String outputAnnotationFields;

    public String alignerClass;

    // view to write output annotation to
    public String outputViewName;

    /**
     * Verify that these mergers have enough config info
     */
    public void verify() {
        if (inputs == null || inputs.length == 0
                || outputAnnotationClass == null || outputAnnotationFields == null || outputViewName == null) {
            throw new EnsemblesException("Translator or merger configuration incomplete");
        }
        Arrays.asList(inputs).forEach(SingleInputConfig::verify);
    }

    /**
     *
     * Used for converting input type beans into the String[] needed as UIMA config params
     * @return
     */
    // todo: confirm these are getting fields from inputs correctly
    public String[] getInputSystemNames() {
        List<String> inputSystemNames = new ArrayList<>();
        for (SingleInputConfig inputConfig : inputs) {
            inputSystemNames.add(inputConfig.fromSystem);
        }
        return inputSystemNames.toArray(new String[inputSystemNames.size()]);
    }
    public String[] getInputTypes() {
        List<String> inputTypes = new ArrayList<>();
        for (SingleInputConfig inputConfig : inputs) {
            inputTypes.add(inputConfig.annotationType);
        }
        return inputTypes.toArray(new String[inputTypes.size()]);
    }
    public String[] getInputFields() {
        List<String> inputFields = new ArrayList<>();
        for (SingleInputConfig inputConfig : inputs) {
            inputFields.add(inputConfig.annotationField);
        }
        return inputFields.toArray(new String[inputFields.size()]);
    }
    public String[] getInputTransformers() {
        List<String> inputTransformers = new ArrayList<>();
        for (SingleInputConfig inputConfig : inputs) {
            inputTransformers.add(inputConfig.transformerClass);
        }
        return inputTransformers.toArray(new String[inputTransformers.size()]);
    }

    public String[] getOutputAnnotationClasses() {
        String[] outputClasses = new String[outputs.length];
        for (int i=0; i<outputs.length; i++) {
            outputClasses[i] = outputs[i].annotationType;
        }
        return outputClasses;
    }
    public String[] getOutputAnnotationFields() {
        String[] outputFields = new String[outputs.length];
        for (int i=0; i<outputs.length; i++) {
            outputFields[i] = outputs[i].annotationType;
        }
        return outputFields;
    }
    public String[] getOutputViewNames() {
        String[] outputViews = new String[outputs.length];
        for (int i=0; i<outputs.length; i++) {
            outputViews[i] = outputs[i].writeView;
        }
        return outputViews;
    }
    public String[] getCreatorClasses() {
        String[] creators = new String[outputs.length];
        for (int i=0; i<outputs.length; i++) {
            creators[i] = outputs[i].writeView;
        }
        return creators;
    }
    public String[] getDistillerClasses() {
        String[] distillers = new String[outputs.length];
        for (int i=0; i<outputs.length; i++) {
            distillers[i] = outputs[i].writeView;
        }
        return distillers;
    }

}
