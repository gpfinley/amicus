package edu.umn.amicus.filters;

/**
 * Created by gpfinley on 2/17/17.
 */
public class RegexFilter extends AnnotationFilter<Object> {

    public RegexFilter(String pattern) {
        super(pattern);
    }

    public boolean passes(Object value) {
        return value != null && pattern.matcher(value.toString()).matches();
    }

}
