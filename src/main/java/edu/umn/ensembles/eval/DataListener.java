package edu.umn.ensembles.eval;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by gpfinley on 2/8/17.
 */
public class DataListener<D, S extends Summarizer<D, T>, T> {

    private static DataListener dataListener;

    private static ConcurrentMap<String, DataListener> dataListeners = new ConcurrentHashMap<>();

    public static DataListener getDataListener(String name, Summarizer summarizer) {
        DataListener dl = dataListeners.get(name);
        if (dl == null) {
            dl = new DataListener(name, summarizer);
            dataListeners.put(name, dl);
        } else {
            if (!summarizer.getClass().equals(dl.summarizer.getClass())) {
                // todo: warn. Using summarizer as previously specified
            }
        }
        return dl;
    }

    private final String name;
    private final S summarizer;

    protected DataListener(String name, S summarizer) {
        this.name = name;
        this.summarizer = summarizer;
    }

    ConcurrentLinkedQueue<D> heard = new ConcurrentLinkedQueue<>();

    public void listen(D value) {
        heard.add(value);
    }

    public T respond() {
        return summarizer.summarize(heard);
    }

}
