package edu.umn.amicus.summary;

import edu.umn.amicus.util.AlignedTuple;
import edu.umn.amicus.util.ANA;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by gpfinley on 3/13/17.
 */
public class AlignedCsvSummarizer extends CsvSummarizer implements DocumentSummarizer, CollectionSummarizer {

    public AlignedCsvSummarizer(String[] viewNames, String[] types, String[] fields) {
        super(viewNames, types, fields);
    }

    /**
     * ...
     * Document summarization for CSV is the same as collection summarization without doc ID
     * @param tuples
     * @return
     */
    @Override
    public String summarizeDocument(Iterator<AlignedTuple> tuples, String docId, String text) {
        return summarizeCollection(tuples, null);
    }

    @Override
    public String summarizeCollection(Iterator<AlignedTuple> tuples, Iterator<String> docIds) {
        List<String> lines = new ArrayList<>();
        int nFields = 0;

        while (tuples.hasNext()) {
            List<Object> values = new ArrayList<>();
            if (docIds != null) {
                values.add(docIds.next());
            }
            AlignedTuple tuple = tuples.next();
            nFields = tuple.size();

            for (ANA annot : tuple) {
                if (annot == null) {
                    values.add("");
                    values.add("");
                    values.add("");
                } else {
                    values.add(annot.getValue());
                    values.add(annot.getBegin());
                    values.add(annot.getEnd());
                }
            }
            lines.add(buildLine(values));
        }

        // build header
        List<Object> headerObjects = new ArrayList<>();
        if (docIds != null) {
            headerObjects.add("docID");
        }
        for (int i=0; i<nFields; i++) {
            String viewName = viewNames[i] == null ? "view" : viewNames[i];
            String typeName = types[i] == null ? "type" : types[i];
            String fieldName = fields[i] == null ? "field" : fields[i];
            headerObjects.add(viewName + ":" + typeName + ":" + fieldName);
            headerObjects.add("begin");
            headerObjects.add("end");
        }
        StringBuilder builder = new StringBuilder();
        builder.append(buildLine(headerObjects)).append("\n");
        for (String line : lines) {
            builder.append(line).append("\n");
        }
        return builder.toString();
    }

    @Override
    public String getFileExtension() {
        return super.getFileExtension();
    }

}
