package edu.umn.ensembles.config;

import edu.umn.ensembles.EnsemblesException;

/**
 * Stores configuration for a single input system.
 * Created by greg on 2/7/17.
 */
public class SingleSystemConfig {
    public String _systemName = "untitled system";
    public String dataPath;
    public String readFromView = "_InitialView";
    public String saveIntoView;

    /**
     * Builder-style pattern to make coding up a configuration easier.
     * @param systemName the name to use for this system (unused; for human readability only).
     * @return this configuration
     */
    public SingleSystemConfig useSystemName(String systemName) {
        this._systemName = systemName;
        return this;
    }

    /**
     * Builder-style pattern to make coding up a configuration easier.
     * @param dataPath the directory path containing xmi/xml files of this system's outputs.
     * @return this configuration
     */
    public SingleSystemConfig useDataPath(String dataPath) {
        this.dataPath = dataPath;
        return this;
    }

    /**
     * Builder-style pattern to make coding up a configuration easier.
     * @param readFromView the view that this system uses for its annotations (default "_InitialView").
     * @return this configuration
     */
    public SingleSystemConfig useReadFromView(String readFromView) {
        this.readFromView = readFromView;
        return this;
    }

    /**
     * Builder-style pattern to make coding up a configuration easier.
     * @param saveIntoView the view to copy this system's annotations in for the summary CAS.
     * @return this configuration
     */
    public SingleSystemConfig useSaveIntoView(String saveIntoView) {
        this.saveIntoView = saveIntoView;
        return this;
    }

    /**
     * Throw an exception of this configuration is lacking essential information.
     */
    public void verify() {
        if (dataPath == null) {
            throw new EnsemblesException("Need to specify a path to serialized CAS data for all systems.");
        }
    }

}
