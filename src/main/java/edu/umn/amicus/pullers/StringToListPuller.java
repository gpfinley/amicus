package edu.umn.amicus.pullers;

import edu.umn.amicus.Amicus;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Pull from a single field, split on a delimiter, and save the results into a list of objects.
 *
 * Created by gpfinley on 2/27/17.
 */
public class StringToListPuller extends AnnotationPuller<List> {

    public StringToListPuller(String delimitedFieldNames) {
        super(delimitedFieldNames);
        if (delimitedFieldNames.contains(FIELD_NAME_DELIMITER)) {
            throw new UnsupportedOperationException(StringToListPuller.class.getName() + " cannot pull from multiple fields.");
        }
    }

    @Override
    public List transform(Annotation annotation) {
        Object value = callAnnotationGetters(annotation);
        if (value == null) return null;
        return stringToList(value.toString());
    }

    public static List stringToList(String string) {
        if (string.length() == 0) return new ArrayList();
        return Arrays.asList(string.split(Amicus.CONCATENATED_STRING_DELIMITER));
    }

}
