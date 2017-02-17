package edu.umn.amicus.filters;

/**
 * Created by gpfinley on 2/17/17.
 */
public class RegexFilter extends AnnotationFilter<String> {

    public RegexFilter(String pattern) {
        super(pattern);
    }

    public boolean passes(String value) {
        return pattern.matcher(value).matches();
    }

}
