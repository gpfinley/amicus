package edu.umn.amicus.summarizers;

import edu.umn.amicus.processing.Counter;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by greg on 2/10/17.
 */
public class CounterSummarizer implements Summarizer<Object> {

    @Override
    public String summarize(List<Object> list) {
        final Counter<Object> counter = new Counter<>(list);

        // build reversed map sorted by values
        TreeMap<Object, Integer> treeMap = new TreeMap<>(new Comparator<Object>() {
            @Override
            public int compare(Object o, Object t1) {
                int comp = -counter.get(o).compareTo(counter.get(t1));
                return comp == 0 ? ((Comparable) o).compareTo(t1) : comp;
            }
        });
        treeMap.putAll(counter);

        StringBuilder builder = new StringBuilder();

        for (Map.Entry<Object, Integer> entry : treeMap.entrySet()) {
            builder.append(entry.getValue());
            builder.append("\t");
            builder.append(entry.getKey().toString());
            builder.append("\n");
//            builder.append(String.format("%s:\t%d\n", entry.getKey().toString(), entry.getValue()));
        }
        builder.append(counter.total());
        builder.append("\t(total)");
        builder.append("\n");
//        System.out.printf("Total: %d\n", sum);

        return builder.toString();
    }

}
