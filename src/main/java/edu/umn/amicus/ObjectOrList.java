package edu.umn.amicus;

import java.util.Arrays;
import java.util.List;

/**
 * Object for storing either one object or a list of objects.
 * Lists will be converted to Strings; other objects will be converted to lists (via toString and the same delimiter).
 *
 * Created by gpfinley on 2/27/17.
 */
@Deprecated
public class ObjectOrList {

    public static final String DELIMITER = "|";

    private final Object object;

    public ObjectOrList(Object object) {
        this.object = object;
    }

    public Object getAsObject() {
        if (object == null) return null;
        if (object instanceof List) {
            return buildStringFromList((List) object);
        } else {
            return object;
        }
    }

    public List getAsList() {
        if (object == null) return null;
        if (object instanceof List) {
            return (List) object;
        } else {
            return buildListFromString(object.toString());
        }
    }

    @Override
    public String toString() {
        return object == null ? null : getAsObject().toString();
    }

    public static String buildStringFromList(List list) {
        if (list.size() == 0) return "";
        StringBuilder builder = new StringBuilder();
        builder.append(list.get(0));
        for (int i=1; i<list.size(); i++) {
            builder.append("|").append(list.get(i));
        }
        return builder.toString();
    }

    public static List<String> buildListFromString(String string) {
        return Arrays.asList(string.split(DELIMITER));
    }
}
