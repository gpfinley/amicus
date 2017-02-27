package edu.umn.amicus.pullers;

import edu.umn.amicus.distillers.StringConcatDistiller;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Get a list of values from a concatenated String, as would have been created using StringConcatDistiller.
 * // todo: what uses this? And test.
 *
 * Created by gpfinley on 2/17/17.
 */
@Deprecated
public class ConcatStringToListPuller extends AnnotationPuller<List<String>> {

    public ConcatStringToListPuller(String fieldName) {
        super(fieldName);
    }

    @Override
    public List<String> transform(Annotation annotation) {
        String multiString = callAnnotationGetters(annotation).toString();
        if (multiString == null) return null;
        return Arrays.asList(multiString.split(
                Pattern.quote(StringConcatDistiller.getSeparator())
        ));
    }
}
