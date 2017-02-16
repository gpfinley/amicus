package edu.umn.amicus.eval;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * For summarizing over multiple concurrent CAS objects. Pass it D objects with listen(D value),
 * then retrieve them all with regurgitate().
 *
 * Multiple listeners can be active and are named with Strings.
 *
 * Created by gpfinley on 2/8/17.
 */
public class DataListener<D> {

    private static ConcurrentMap<String, DataListener> dataListeners = new ConcurrentHashMap<>();

    public static DataListener getDataListener(String name) {
        DataListener dl = dataListeners.get(name);
        if (dl == null) {
            dl = new DataListener();
            dataListeners.put(name, dl);
        }
        return dl;
    }

    private List<D> responseList;
    private ConcurrentLinkedQueue<D> heard;

    // prevent instantiation except through the getInstance
    protected DataListener() {
        heard = new ConcurrentLinkedQueue<>();
    }

    public void listen(D value) {
        if (responseList != null) {
            // todo: log warning: adding to a list that has already been polled!
            responseList = null;
        }
        if (value != null) {
            heard.add(value);
        } else {
            // todo: warn that null value being considered? That would mean that pullers are assigning nulls
        }
    }

    public List<D> regurgitate() {
        if (responseList == null) {
            responseList = new ArrayList<>(heard);
        }
        return responseList;
    }

}
