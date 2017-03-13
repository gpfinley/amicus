package edu.umn.amicus.summary;

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
@Deprecated
public class DataListener<D> {

    private static ConcurrentMap<String, DataListener> dataListeners = new ConcurrentHashMap<>();

    private static final String NO_ID = "no_id";

    public static DataListener getDataListener(String name) {
        DataListener dl = dataListeners.get(name);
        if (dl == null) {
            dl = new DataListener();
            dataListeners.put(name, dl);
        }
        return dl;
    }

    private List<D> responseList;
    private List<String> idList;
    private ConcurrentLinkedQueue<D> heard;
    private ConcurrentLinkedQueue<String> ids;

    // prevent instantiation except through the getInstance
    protected DataListener() {
        heard = new ConcurrentLinkedQueue<>();
        ids = new ConcurrentLinkedQueue<>();
    }

    public void listen(D value) {
        listen(value, NO_ID);
    }

    public void listen(D value, String id) {
        if (responseList != null) {
            // todo: log warning: adding to a list that has already been polled!
            responseList = null;
        }
        if (value != null) {
            heard.add(value);
            ids.add(id);
        } else {
            // todo: warn that null value being considered? That would mean that pullers are assigning nulls
        }
    }

    public List<D> regurgitate() {
        if (responseList == null) {
            responseList = new ArrayList<>(heard);
            idList = new ArrayList<>(ids);
        }
        return responseList;
    }

    public List<String> regurgitateIds() {
        if (idList == null) {
            regurgitate();
        }
        return idList;
    }

}