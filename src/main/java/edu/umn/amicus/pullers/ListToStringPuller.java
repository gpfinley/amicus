package edu.umn.amicus.pullers;

import edu.umn.amicus.Amicus;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.List;

/**
 * Pull from multiple fields (presumably) and save the results into a delimited String rather than a list of objects.
 *
 * Created by gpfinley on 2/27/17.
 */
public class ListToStringPuller extends AnnotationPuller<String> {

    public ListToStringPuller(String delimitedFieldNames) {
        super(delimitedFieldNames);
    }

    @Override
    public String transform(Annotation annotation) {
        Object value = callAnnotationGetters(annotation);
        if (value == null) return null;
        if (value instanceof List) {
            return listToString((List) value);
        }
        return value.toString();
    }

    public static String listToString(List list) {
        if (list.size() == 0) return "";
        StringBuilder builder = new StringBuilder();
        builder.append(list.get(0));
        for (int i=1; i<list.size(); i++) {
            builder.append(Amicus.CONCATENATED_STRING_DELIMITER).append(list.get(i));
        }
        return builder.toString();
    }

}
