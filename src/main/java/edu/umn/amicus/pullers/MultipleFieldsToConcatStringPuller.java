package edu.umn.amicus.pullers;

import edu.umn.amicus.Amicus;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.List;

/**
 * Call all fields and put their contents into a concatenated string rather than a list.
 * This can be useful for filtering using regular expressions if you want to filter on a specific field (or multiple)
 *      that is not the same as the field whose content you want to keep.
 *
 * Created by gpfinley on 10/20/16.
 */
@Deprecated
public class MultipleFieldsToConcatStringPuller extends AnnotationPuller<String> {

    public MultipleFieldsToConcatStringPuller(String fieldName) {
        super(fieldName);
    }

    @Override
    public String transform(Annotation annotation) {
        Object transformed = callAnnotationGetters(annotation);
        if (transformed instanceof List) {
            List list = (List) transformed;
            if (list.size() == 0) return "";
            StringBuilder builder = new StringBuilder();
            for (Object o : list) {
                if (o == null) o = "null";
                builder.append(o.toString())
                        .append(Amicus.CONCATENATED_STRING_DELIMITER);
            }
            builder.deleteCharAt(builder.length()-1);
            return builder.toString();
        }
        return transformed.toString();
    }

}