package edu.umn.amicus.distillers;

import edu.umn.amicus.AlignedTuple;
import edu.umn.amicus.ANA;

/**
 * Distiller that will concatenate String annotations by all systems into a single annotation, with a pipe '|' separator.
 * Uses the min and max of begins and ends for all aligned annotations.
 *
 * Created by gpfinley on 12/9/16.
 */
public class StringConcatDistiller implements Distiller<String> {

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
    public ANA<String> distill(AlignedTuple annotations) {
        if (annotations.size() == 0) return null;

        int begin = Integer.MAX_VALUE;
        int end = Integer.MIN_VALUE;

        StringBuilder builder = new StringBuilder();
        for (ANA preAnnot : annotations) {
            if (preAnnot != null) {
                builder.append(preAnnot.getValue())
                        .append("|");
                if (preAnnot.getEnd() > end) end = preAnnot.getEnd();
                if (preAnnot.getBegin() < begin) begin = preAnnot.getBegin();
            }
        }
        String concatenated = builder.substring(0, builder.length() - 1);
        return new ANA<>(concatenated, begin, end);
    }

}
