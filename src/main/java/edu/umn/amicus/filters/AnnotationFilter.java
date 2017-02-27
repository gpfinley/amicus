package edu.umn.amicus.filters;

import edu.umn.amicus.Amicus;
import edu.umn.amicus.AnalysisPiece;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * Created by greg on 2/16/17.
 */
public abstract class AnnotationFilter<T> extends AnalysisPiece {

    public static final String DEFAULT_FILTER = RegexFilter.class.getName();
    public static final String DEFAULT_NULL_FILTER = PassthroughFilter.class.getName();

    protected Pattern pattern;

    protected AnnotationFilter() {}

    public AnnotationFilter(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public abstract boolean passes(T value);

    private static final ConcurrentMap<String, AnnotationFilter> filterMap = new ConcurrentHashMap<>();

    public static AnnotationFilter create(String filterClassName, String pattern) {
        if (filterClassName == null) {
            if (pattern != null) {
                filterClassName = DEFAULT_FILTER;
            } else {
                pattern = "";
                filterClassName = DEFAULT_NULL_FILTER;
            }
        }
        return Amicus.getPieceInstance(AnnotationFilter.class, filterClassName, pattern);
    }

}