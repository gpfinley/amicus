package edu.umn.amicus.mappers;

import edu.umn.amicus.Amicus;
import edu.umn.amicus.AmicusException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Simple class for storing and calling a mapping between values.
 *
 * Created by gpfinley on 10/13/16.
 */
public class Mapper<T, U> {

    private final static ConcurrentMap<File, Mapper> allMappers = new ConcurrentHashMap<>();

    protected Map<T, U> internalMap;

    public Mapper(Map<T, U> map) {
        this.internalMap = map;
    }

    /**
     * Allow subclasses to call default constructor when deserializing YAML.
     */
    protected Mapper() {}

    /**
     * Specialized mappers may need to build their map from data, not have it assigned in the constructor.
     */
    private void buildInternalMap() { }

    /**
     * Return the value associated with this key.
     * Can return null! Some extensions of this class will not.
     * @param t
     * @return
     */
    public U map(T t) {
        return internalMap.get(t);
    }

    public static Mapper create(String configPath) {
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            throw new AmicusException("No file found at %s.", configPath);
        }
        if (allMappers.containsKey(configFile)) {
            return allMappers.get(configFile);
        }
        try {
            Object mapperObject = Amicus.yaml.load(new FileInputStream(configFile));
            Mapper mapper;
            if (mapperObject instanceof Map) {
                mapper = new Mapper((Map) mapperObject);
            } else if (mapperObject instanceof Mapper) {
                mapper = (Mapper) mapperObject;
            } else {
                throw new AmicusException("File at %s is not a Mapper or Java Map configuration.", configPath);
            }
            mapper.buildInternalMap();
            allMappers.put(configFile, mapper);
            return mapper;
        } catch (IOException e) {
            throw new AmicusException("Could not read config file at %s.", configPath);
        }
    }

}