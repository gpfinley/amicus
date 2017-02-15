package edu.umn.ensembles.pullers;

import org.apache.uima.jcas.tcas.Annotation;

import java.util.ArrayList;
import java.util.List;

/**
 * Get multiple fields from an annotation and put them into a list. Field names are delimited by semicolon.
 *
 * Created by greg on 2/10/17.
 */
public class MultiGetterPuller extends AnnotationPuller<List> {

    public static final String DELIMITER = ";";

    protected String[] fields;

    public MultiGetterPuller(String fields) {
        super(fields);
        this.fields = fields.split(DELIMITER);
    }

    @Override
    public List transform(Annotation annotation) {
        List<Object> list = new ArrayList<>();
        for(String field : fields) {
            list.add(AnnotationPuller.callAnnotationGetter(field, annotation));
        }
        return list;
    }
}
