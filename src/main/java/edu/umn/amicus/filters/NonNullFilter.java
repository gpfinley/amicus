package edu.umn.amicus.filters;

/**
 * Keep an annotation if this field isn't null.
 *
 * Created by gpfinley on 2/27/17.
 */
public class NonNullFilter implements Filter {

    public NonNullFilter(String pattern) { }

    @Override
    public boolean passes(Object value) {
        return value != null;
    }
}
