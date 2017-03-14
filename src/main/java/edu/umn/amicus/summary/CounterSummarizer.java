package edu.umn.amicus.summary;

import edu.umn.amicus.AlignedTuple;
import edu.umn.amicus.Counter;
import edu.umn.amicus.PreAnnotation;

import java.util.*;

/**
 * Created by greg on 2/10/17.
 */
public class CounterSummarizer extends Summarizer implements DocumentSummarizer, CollectionSummarizer {

    @Override
    public String summarizeCollection(Iterator<AlignedTuple<PreAnnotation>> tuples, Iterator<String> docIds) {
        return summarizeDocument(tuples);
    }

    @Override
    public String summarizeDocument(Iterator<AlignedTuple<PreAnnotation>> tuples) {
        StringBuilder builder = new StringBuilder();

        List<Counter<Object>> inputCounters = null;

        while (tuples.hasNext()) {
            AlignedTuple<PreAnnotation> tuple = tuples.next();
            // assuming that all tuples are the same size (Aligner contract says they are)
            if (inputCounters == null) {
                inputCounters = new ArrayList<>();
                for (int i=0; i<tuple.size(); i++) {
                    inputCounters.add(new Counter<>());
                }
            }
            for (int i=0; i<tuple.size(); i++) {
                inputCounters.get(i).increment(tuple.get(i).getValue());
            }
        }

        if (inputCounters == null) return "No annotations";

        for (final Counter counter : inputCounters) {

            // build reversed map sorted by values
            TreeMap<Object, Integer> treeMap = new TreeMap<>(new Comparator<Object>() {
                @Override
                public int compare(Object o, Object t1) {
                    int comp = -counter.get(o).compareTo(counter.get(t1));
                    return comp == 0 ? ((Comparable) o).compareTo(t1) : comp;
                }
            });
            treeMap.putAll(counter);

            for (Map.Entry<Object, Integer> entry : treeMap.entrySet()) {
                builder.append(entry.getValue())
                        .append("\t")
                        .append(entry.getKey().toString())
                        .append("\n");
            }
            builder.append(counter.total())
                    .append("\t(total)")
                    .append("\n");
        }

        return builder.toString();
    }

    @Override
    public String getFileExtension() {
        return "txt";
    }

}
