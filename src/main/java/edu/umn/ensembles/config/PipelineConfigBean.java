package edu.umn.ensembles.config;

import edu.umn.ensembles.EnsemblesException;

import java.util.List;

/**
 * Created by gpfinley on 1/20/17.
 */
public class PipelineConfigBean {

    private String _pipelineName;
    private List<String> inputNames;
    private List<String> inputDirectories;
    private List<String> inputViews;
    private List<MergerConfigBean> translatorConfigurations;
    private String outPath;

    /**
     * Throw an exception if not enough information was provided
     */
    public void verify() {
        if (inputNames == null || inputDirectories == null || inputViews == null
                || inputNames.size() != inputDirectories.size() || inputNames.size() != inputViews.size()) {
            throw new EnsemblesException("Pipeline configuration incomplete");
        }
        translatorConfigurations.forEach(MergerConfigBean::verify);
    }

    public String get_pipelineName() {
        return _pipelineName;
    }

    public void set_pipelineName(String _pipelineName) {
        this._pipelineName = _pipelineName;
    }

    public String getOutPath() {
        return outPath;
    }

    public void setOutPath(String outPath) {
        this.outPath = outPath;
    }

    public List<String> getInputNames() {
        return inputNames;
    }

    public void setInputNames(List<String> inputNames) {
        this.inputNames = inputNames;
    }

    public List<String> getInputDirectories() {
        return inputDirectories;
    }

    public void setInputDirectories(List<String> inputDirectories) {
        this.inputDirectories = inputDirectories;
    }

    public List<String> getInputViews() {
        return inputViews;
    }

    public void setInputViews(List<String> inputViews) {
        this.inputViews = inputViews;
    }

    public List<MergerConfigBean> getTranslatorConfigurations() {
        return translatorConfigurations;
    }

    public void setTranslatorConfigurations(List<MergerConfigBean> translatorConfigurations) {
        this.translatorConfigurations = translatorConfigurations;
    }
}
