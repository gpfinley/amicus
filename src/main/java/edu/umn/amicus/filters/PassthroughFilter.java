package edu.umn.amicus.filters;

/**
 * No filtering.
 *
 * Created by gpfinley on 2/17/17.
 */
public class PassthroughFilter extends AnnotationFilter<Object> {

    @Override
    public boolean passes(Object value) {
        return true;
    }
}
