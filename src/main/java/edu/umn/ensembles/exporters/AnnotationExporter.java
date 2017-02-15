package edu.umn.ensembles.exporters;

import edu.umn.ensembles.PreAnnotation;

import java.util.Iterator;
import java.util.List;

/**
 * Created by greg on 2/11/17.
 */
public abstract class AnnotationExporter {

    protected String[] systemNames;
    protected String[] typeNames;
    protected String[] fieldNames;

    public String[] getSystemNames() {
        return systemNames;
    }

    public void setSystemNames(String[] systemNames) {
        this.systemNames = systemNames;
    }

    public String[] getTypeNames() {
        return typeNames;
    }

    public void setTypeNames(String[] typeNames) {
        this.typeNames = typeNames;
    }

    public String[] getFieldNames() {
        return fieldNames;
    }

    public void setFieldNames(String[] fieldNames) {
        this.fieldNames = fieldNames;
    }

    public String getFileExtension() {
        return "txt";
    }

    public abstract String exportContents(Iterator<List<PreAnnotation>> annotIterator);

}
