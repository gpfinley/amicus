package edu.umn.amicus.filters;

import java.util.logging.Logger;

/**
 * Determine if the value of this field is greater than or equal to a given number.
 * Can specify other comparisons as the first characters of the "pattern" string: e.g., "<8.5", "=3", "<=0"
 * Will crash if the pattern or value of any compared field is not a properly formatted integer or double.
 *
 * todo: unit test
 *
 * Created by gpfinley on 2/17/17.
 */
public class NumberFilter extends AnnotationFilter<Object> {

    private static final Logger LOGGER = Logger.getLogger(NumberFilter.class.getName());

    private final double thresh;

    // if all false, use >=
    private boolean useEquals;
    private boolean useLessthan;
    private boolean useLessEqual;
    private boolean useGreater;

    public NumberFilter(String pattern) {
        super(pattern);
        String numberPattern = pattern;
        try {
            if (pattern.charAt(0) == '=') {
                useEquals = true;
                if (pattern.charAt(1) == '=') {
                    numberPattern = pattern.substring(2);
                } else {
                    numberPattern = pattern.substring(1);
                }
            } else if (pattern.charAt(0) == '>') {
                if (pattern.charAt(1) == '=') {
                    numberPattern = pattern.substring(2);
                } else {
                    numberPattern = pattern.substring(1);
                    useGreater = true;
                }
            } else if (pattern.charAt(0) == '<') {
                if (pattern.charAt(1) == '=') {
                    numberPattern = pattern.substring(2);
                    useLessEqual = true;
                } else {
                    numberPattern = pattern.substring(1);
                    useLessthan = true;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            LOGGER.severe(pattern + " is not a valid number comparison pattern (check Translator configuration)");
            throw e;
        }
        try {
            thresh = Double.parseDouble(pattern);
        } catch (NumberFormatException e) {
            LOGGER.severe(numberPattern + " is not a valid number (check Translator configuration)");
            throw e;
        }
    }

    public boolean passes(Object value) {
        if (value instanceof Number) {
            return compare(((Number) value).doubleValue(), thresh);
        } else {
            return compare(Double.parseDouble(value.toString()), thresh);
        }
    }

    private boolean compare(Double x, Double y) {
        if (useEquals) return x.equals(y);
        if (useLessthan) return x < y;
        if (useLessEqual) return x <= y;
        if (useGreater) return x > y;
        return x >= y;
    }

}
