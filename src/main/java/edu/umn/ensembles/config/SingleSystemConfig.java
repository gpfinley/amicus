package edu.umn.ensembles.config;

import edu.umn.ensembles.EnsemblesException;

/**
 * Created by greg on 2/7/17.
 */
public class SingleSystemConfig {
    public String _systemName = "untitled system";
    public String dataPath;
    public String readFromView = "_InitialView";
    public String copyIntoView;

    public SingleSystemConfig useSystemName(String systemName) {
        this._systemName = systemName;
        return this;
    }
    public SingleSystemConfig useDataPath(String dataPath) {
        this.dataPath = dataPath;
        return this;
    }
    public SingleSystemConfig useReadFromView(String readFromView) {
        this.readFromView = readFromView;
        return this;
    }
    public SingleSystemConfig useCopyIntoView(String copyIntoView) {
        this.copyIntoView = copyIntoView;
        return this;
    }

    public void verify() {
        if (dataPath == null) {
            throw new EnsemblesException("Need to specify a path to serialized CAS data for all systems.");
        }
    }

}
