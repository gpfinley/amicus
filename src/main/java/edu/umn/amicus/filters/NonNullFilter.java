package edu.umn.amicus.filters;

/**
 * Keep an annotation if this field isn't null.
 *
 * Created by gpfinley on 2/27/17.
 */
public class NonNullFilter extends AnnotationFilter<Object> {

    public NonNullFilter() { }

    public NonNullFilter(String pattern) {
        this();
    }

    @Override
    public boolean passes(Object value) {
        return value != null;
    }
}
