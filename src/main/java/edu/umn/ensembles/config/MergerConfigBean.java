package edu.umn.ensembles.config;

import edu.umn.ensembles.EnsemblesException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A serializable bean for a single merge engine
 *
 * Created by gpfinley on 10/24/16.
 */
public class MergerConfigBean {

    private String _name;

    private SingleInputConfig[] inputs;
    private SingleInputConfig[] outputs;

    // what kind of type to output
    private String outputAnnotationClass;
    // can have multiple if separated by ;
    private String outputAnnotationFields;

    private String alignerClass;

    // view to write output annotation to
    private String outputViewName;

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
    public String[] getInputSystemNames() {
        List<String> inputSystemNames = new ArrayList<>();
        for (SingleInputConfig inputConfig : getInputs()) {
            inputSystemNames.add(inputConfig.fromSystem);
        }
        return inputSystemNames.toArray(new String[inputSystemNames.size()]);
    }
    public String[] getInputTypes() {
        List<String> inputTypes = new ArrayList<>();
        for (SingleInputConfig inputConfig : getInputs()) {
            inputTypes.add(inputConfig.annotationType);
        }
        return inputTypes.toArray(new String[inputTypes.size()]);
    }
    public String[] getInputFields() {
        List<String> inputFields = new ArrayList<>();
        for (SingleInputConfig inputConfig : getInputs()) {
            inputFields.add(inputConfig.annotationField);
        }
        return inputFields.toArray(new String[inputFields.size()]);
    }
    public String[] getInputTransformers() {
        List<String> inputTransformers = new ArrayList<>();
        for (SingleInputConfig inputConfig : getInputs()) {
            inputTransformers.add(inputConfig.transformerClass);
        }
        return inputTransformers.toArray(new String[inputTransformers.size()]);
    }



    public String get_name() {
        return _name;
    }

    public void set_name(String name) {
        this._name = name;
    }

    public SingleInputConfig[] getInputs() {
        return inputs;
    }

    public void setInputs(SingleInputConfig[] inputs) {
        this.inputs = inputs;
    }

    public String getOutputAnnotationClass() {
                                           return outputAnnotationClass;
                                                                                                       }

    public void setOutputAnnotationClass(String outputAnnotationClass) {
        this.outputAnnotationClass = outputAnnotationClass;
    }

    public String getOutputAnnotationFields() {
        return outputAnnotationFields;
    }

    public void setOutputAnnotationFields(String outputAnnotationFields) {
        this.outputAnnotationFields = outputAnnotationFields;
    }

    public String getAlignerClass() {
                                  return alignerClass;
                                                                            }

    public void setAlignerClass(String alignerClass) {
        this.alignerClass = alignerClass;
    }

    public String getOutputViewName() {
        return outputViewName;
    }

    public void setOutputViewName(String outputViewName) {
        this.outputViewName = outputViewName;
    }

    public SingleInputConfig[] getOutputs() {
        return outputs;
    }

    public void setOutputs(SingleInputConfig[] outputs) {
        this.outputs = outputs;
    }
}
