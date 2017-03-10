package edu.umn.amicus.summary;

import edu.umn.amicus.PreAnnotation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * // todo: doc
 * // todo: test! will need to test with multi-field (List) PreAnnotations
 * Created by greg on 2/11/17.
 */
public class AlignedTsvMicroSummarizer extends MicroSummarizer {

    protected String delimiter = "\t";

    @Override
    public String getFileExtension() {
        return "tsv";
    }

    @Override
    public String exportContents(Iterator<List<PreAnnotation>> annotIterator) {

        int nFields = 0;

        List<String> lines = new ArrayList<>();

        while (annotIterator.hasNext()) {
            List<PreAnnotation> annots = annotIterator.next();
            nFields = annots.size();

            StringBuilder builder = new StringBuilder();
            int firstAnnot = 0;
            while (annots.get(firstAnnot) != null) firstAnnot++;
            builder.append(delimiter);
            builder.append(annots.get(firstAnnot).getBegin());
            builder.append(delimiter);
            builder.append(annots.get(firstAnnot).getEnd());

            for (PreAnnotation annot : annots) {
                builder.append(delimiter);
                if (annot != null) {
                    // give the name or number of the source system along with the begin and end
                    Object val = annot.getValue();
                    if (val != null) {
                        builder.append(escapeDelimiter(val.toString()));
                    }
                }
            }
            lines.add(builder.toString());
        }

        StringBuilder builder = new StringBuilder();
        // build header
        builder.append("begin");
        builder.append(delimiter);
        builder.append("end");
        for (int i=0; i<nFields; i++) {
            builder.append(delimiter);
            String viewName = viewNames[i] == null ? "unknown" : viewNames[i];
            String typeName = typeNames[i] == null ? "unknown" : typeNames[i];
            String fieldName = fieldNames[i] == null ? "unknown" : fieldNames[i];
            builder.append(viewName)
                    .append(":")
                    .append(typeName)
                    .append(":")
                    .append(fieldName);
        }
        builder.append("\n");

        for (String line : lines) {
            builder.append(line).append("\n");
        }

        return builder.toString();
    }

    protected String escapeDelimiter(String raw) {
        return raw.replace(delimiter, "    ");
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

}
