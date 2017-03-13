//package edu.umn.amicus.summary;
//
//import edu.umn.amicus.PreAnnotation;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
///**
// * // todo: doc
// * // todo: test! will need to test with multi-field (List) PreAnnotations
// * Created by greg on 2/11/17.
// */
//@Deprecated
//public class EachSoloTsvMicroSummarizer extends DocumentSummarizer {
//
//    protected String delimiter = "\t";
//
//    @Override
//    public String getFileExtension() {
//        return "tsv";
//    }
//
//    @Override
//    public String exportContents(Iterator<List<PreAnnotation>> annotIterator) {
//
//        int maxFields = 1;
//
//        List<String> lines = new ArrayList<>();
//        List<Integer> fieldsWrittenEachLine = new ArrayList<>();
//
//        while (annotIterator.hasNext()) {
//            List<PreAnnotation> annots = annotIterator.next();
//            for (int i=0; i<annots.size(); i++) {
//                if (annots.get(i) != null && annots.get(i).getValue() != null) {
//
//                    StringBuilder builder = new StringBuilder();
//
//                    // give the name or number of the source system along with the begin and end
//                    builder.append(viewNames != null && viewNames.length > i ? viewNames[i] : ((Integer) i).toString());
//                    if (typeNames != null) {
//                        builder.append(delimiter);
//                        builder.append(i < typeNames.length  && typeNames[i] != null? typeNames[i] : "unknown");
//                    }
//                    if (fieldNames != null) {
//                        builder.append(delimiter);
//                        builder.append(i < fieldNames.length && fieldNames[i] != null ? fieldNames[i] : "unknown");
//                    }
//                    builder.append(delimiter);
//                    builder.append(annots.get(i).getBegin());
//                    builder.append(delimiter);
//                    builder.append(annots.get(i).getEnd());
//
//                    // If this PreAnnotation value is a List, print all the fields
//                    if (annots.get(i).getValue() instanceof List) {
//                        List multiFields = (List) annots.get(i).getValue();
//                        maxFields = Math.max(multiFields.size(), maxFields);
//                        for (Object val : multiFields) {
//                            builder.append(delimiter);
//                            builder.append(escapeDelimiter(val.toString()));
//                        }
//                        fieldsWrittenEachLine.add(multiFields.size());
//                    } else {
//                        builder.append(delimiter);
//                        builder.append(escapeDelimiter(annots.get(i).getValue().toString()));
//                        fieldsWrittenEachLine.add(1);
//                    }
//                    lines.add(builder.toString());
//                }
//            }
//        }
//
//        StringBuilder builder = new StringBuilder();
//        // build header
//        builder.append("fromView");
//        if (typeNames != null) {
//            builder.append(delimiter);
//            builder.append("fromType");
//        }
//        if (fieldNames != null) {
//            builder.append(delimiter);
//            builder.append("fieldName");
//        }
//        builder.append(delimiter);
//        builder.append("begin");
//        builder.append(delimiter);
//        builder.append("end");
//        for (int i=0; i<maxFields; i++) {
//            builder.append(delimiter);
//            builder.append("field");
//            builder.append(i);
//        }
//        builder.append("\n");
//
//        // write each line, with extra delimiters on the end if necessary to make all lines have same # of fields
//        for (int i=0; i<lines.size(); i++) {
//            builder.append(lines.get(i));
//            for (int j = fieldsWrittenEachLine.get(i); j < maxFields; j++) {
//                builder.append(delimiter);
//            }
//            builder.append("\n");
//        }
//
//        return builder.toString();
//    }
//
//    protected String escapeDelimiter(String raw) {
//        return raw.replace(delimiter, "    ");
//    }
//
//    public String getDelimiter() {
//        return delimiter;
//    }
//
//    public void setDelimiter(String delimiter) {
//        this.delimiter = delimiter;
//    }
//
//}
