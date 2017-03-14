package edu.umn.amicus.summary;

import edu.umn.amicus.AlignedTuple;
import edu.umn.amicus.PreAnnotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * todo...
 *
 * This summarizer's behavior is unchanged by the Aligner used, except inasmuch as the Aligner under- or over-represents
 *      any annotations.
 * Created by gpfinley on 3/13/17.
 */
public class EachSoloCsvSummarizer extends CsvSummarizer implements DocumentSummarizer, CollectionSummarizer {

    public EachSoloCsvSummarizer(String[] viewNames, String[] types, String[] fields) {
        super(viewNames, types, fields);
    }

    @Override
    public String summarizeDocument(Iterator<AlignedTuple<PreAnnotation>> tuples) {
        return summarizeCollection(tuples, null);
    }

    /**
     * Use this function for both doc and collection summarization. For the former, docIds should be null.
     * @param tuples
     * @param docIds
     * @return
     */
    @Override
    public String summarizeCollection(Iterator<AlignedTuple<PreAnnotation>> tuples, Iterator<String> docIds) {

        int maxFields = 1;

        List<String> lines = new ArrayList<>();
        List<Integer> fieldsWrittenEachLine = new ArrayList<>();

        while (tuples.hasNext()) {
            AlignedTuple<PreAnnotation> annots = tuples.next();
            String docId = null;
            if (docIds != null) {
                docId = docIds.next();
            }
            for (int i=0; i<annots.size(); i++) {
                if (annots.get(i) != null && annots.get(i).getValue() != null) {

                    List<Object> data = new ArrayList<>();
                    if (docIds != null) {
                        data.add(docId);
                    }
                    data.add(i < viewNames.length && viewNames[i] != null ? viewNames[i] : "");
                    data.add(i < types.length && types[i] != null ? types[i] : "");
                    data.add(i < fields.length && fields[i] != null ? fields[i] : "");
                    data.add(annots.get(i).getBegin());
                    data.add(annots.get(i).getEnd());

                    // If this PreAnnotation value is a List, print all the fields...and keep track of that we did
                    if (annots.get(i).getValue() instanceof List) {
                        List multiFields = (List) annots.get(i).getValue();
                        maxFields = Math.max(multiFields.size(), maxFields);
                        for (Object val : multiFields) {
                            data.add(val);
                        }
                        fieldsWrittenEachLine.add(multiFields.size());
                    } else {
                        data.add(annots.get(i).getValue());
                        fieldsWrittenEachLine.add(1);
                    }

                    lines.add(buildLine(data));
                }
            }
        }

        // build header
        List<Object> headerObjects = new ArrayList<>();

        if (docIds != null) {
            headerObjects.add("docID");
        }
        Collections.addAll(headerObjects, "fromView", "typeName", "fieldName", "begin", "end");
        for (int i=0; i<maxFields; i++) {
            headerObjects.add("field");
        }

        StringBuilder builder = new StringBuilder();
        builder.append(buildLine(headerObjects)).append("\n");
        // write each line, with extra delimiters on the end if necessary to make all lines have same # of fields
        for (int i=0; i<lines.size(); i++) {
            builder.append(lines.get(i));
            for (int j = fieldsWrittenEachLine.get(i); j < maxFields; j++) {
                builder.append(delimiter);
            }
            builder.append("\n");
        }

        return builder.toString();
    }

    public String getFileExtension() {
        return super.getFileExtension();
    }

}
