package edu.umn.amicus.mappers;

import edu.umn.amicus.AmicusException;
import edu.umn.amicus.AnalysisPiece;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Simple class for storing and calling a mapping between values.
 *
 * Created by gpfinley on 10/13/16.
 */
public class Mapper implements AnalysisPiece {

    protected Map<Object, Object> internalMap;

    public Mapper(Map<Object, Object> map) {
        this.internalMap = map;
    }

    /**
     * Allow subclasses to call default constructor when deserializing YAML.
     */
    protected Mapper() {}

    /**
     * Specialized mappers may need to build their map from data, not have it assigned in the constructor.
     */
    public void initialize() throws AmicusException { }

    /**
     * Return the value(s) associated with this key.
     * Can return null! Some extensions of this class may not.
     * @param key
     * @return
     */
    public Object map(Object key) throws AmicusException {
        if (key instanceof Collection) {
            List<Object> toReturn = new ArrayList<>();
            for (Object obj : (Collection) key) {
                toReturn.add(mappingFunction(key));
            }
            return toReturn;
        }
        return mappingFunction(key);
    }

    /**
     * Subclasses might override this (case insensitivity, e.g.).
     * @param k the map key
     * @return the mapped value
     */
    protected Object mappingFunction(Object k) throws AmicusException {
        return internalMap.get(k);
    }

}