package edu.umn.amicus.filters;

import edu.umn.amicus.pushers.Pusher;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Decide to REMOVE annotation values based on a regular expression.
 * List values (drawn from multiple fields) will be converted into pipe-delimited strings.
 * Gives the exact opposite result of RegexFilter (when the annotation is not null, in which case both return false)
 *
 * Created by gpfinley on 2/17/17.
 */
public class RegexNegativeFilter implements Filter {

    protected Pattern pattern;

    public RegexNegativeFilter(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public boolean passes(Object value) {
        if (value == null) return false;
        if (value instanceof List) {
            value = Pusher.buildStringFromList((List) value);
        }
        return !pattern.matcher(value.toString()).matches();
    }

}
