package edu.umn.ensembles.summarizers;

import edu.umn.ensembles.processing.Counter;

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
                return -counter.get(o).compareTo(counter.get(t1));
            }
        });
        treeMap.putAll(counter);

        StringBuilder builder = new StringBuilder();

        int sum = 0;
        for (Map.Entry<Object, Integer> entry : treeMap.entrySet()) {
            sum += entry.getValue();
            builder.append(entry.getKey().toString());
            builder.append(":\t");
            builder.append(entry.getValue());
            builder.append("\n");
//            builder.append(String.format("%s:\t%d\n", entry.getKey().toString(), entry.getValue()));
        }
        builder.append("Total: ");
        builder.append(sum);
        builder.append("\n");
//        System.out.printf("Total: %d\n", sum);

        return builder.toString();
    }
}
