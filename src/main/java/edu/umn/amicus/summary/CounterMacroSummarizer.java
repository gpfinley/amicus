package edu.umn.amicus.summary;

import edu.umn.amicus.Counter;
import edu.umn.amicus.PreAnnotation;

import java.util.*;

/**
 * Created by greg on 2/10/17.
 */
public class CounterMacroSummarizer implements MacroSummarizer {

    @Override
    public String summarize(List<List<PreAnnotation>> list, List<String> docIds) {

        StringBuilder builder = new StringBuilder();

        // transpose the list of lists while pulling out objects from the PreAnnotations (don't care about begin/end)
        List<List<Object>> allObjects = new ArrayList<>();
        for (Object x : list.get(0)) {
            allObjects.add(new ArrayList<>());
        }
        for (List<PreAnnotation> preAnnotations : list) {
            for (int i=0; i<preAnnotations.size(); i++) {
                allObjects.get(i).add(preAnnotations.get(i).getValue());
            }
        }

        for(List<Object> objects : allObjects) {

            final Counter<Object> counter = new Counter<>(objects);

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

}
