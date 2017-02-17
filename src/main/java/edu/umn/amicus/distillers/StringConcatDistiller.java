package edu.umn.amicus.distillers;

import edu.umn.amicus.PreAnnotation;

import java.util.List;

/**
 * Distiller that will concatenate String annotations by all systems into a single annotation, with a pipe '|' separator.
 * Uses the min and max of begins and ends for all aligned annotations.
 *
 * Created by gpfinley on 12/9/16.
 */
public class StringConcatDistiller extends AnnotationDistiller<String> {

    public static String separator= "|";

    public static String getSeparator() {
        return separator;
    }

    public static void setSeparator(String separator) {
        StringConcatDistiller.separator = separator;
    }

    /**
     * Concatenate the content of all annotations (using toString() if transformed annotations are not already strings)
     *
     * @param annotations
     */
    @Override
    public PreAnnotation<String> distill(List<PreAnnotation> annotations) {
        if (annotations.size() == 0) return null;

        int begin = Integer.MAX_VALUE;
        int end = Integer.MIN_VALUE;

        StringBuilder builder = new StringBuilder();
        for (PreAnnotation preAnnot : annotations) {
            if (preAnnot != null) {
                builder.append(preAnnot.getValue());
                builder.append("|");
                if (preAnnot.getEnd() > end) end = preAnnot.getEnd();
                if (preAnnot.getBegin() < begin) begin = preAnnot.getBegin();
            }
        }
        String concatenated = builder.substring(0, builder.length() - 1);
        return new PreAnnotation<>(concatenated, begin, end);
    }

}
