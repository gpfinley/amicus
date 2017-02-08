package edu.umn.ensembles.processing;

import edu.umn.ensembles.config.ClassConfigurationLoader;

import java.io.FileNotFoundException;
import java.util.Map;

/**
 * Simple class for storing and calling a mapping between values. Used by MapperPuller.
 * Map must be stored in YAML in the class configuration file corresponding to this class.
 * In the current implementation, only one Mapper can be active during a single run of the application.
 * This is to prevent confusing which configured map goes with which Mapper instantiation.
 * If multiple mappers are needed, either:
 *      1) put all keys for all mappers into a single comprehensive map, assuming there is no overlap in keys;
 *      2) use (and possibly implement) other classes that extend Mapper and have their own configurations; or
 *      3) run the pipeline multiple times.
 * Extensions of the Mapper class--and any resource used by parallelized UIMA components--should implement a singleton
 *      paradigm to minimize memory usage and loading time.
 *
 * Created by gpfinley on 10/13/16.
 */
public class Mapper<T, U> {

    private final static Mapper mapper;
    static {
        try {
            Map yamlMap = (Map) ClassConfigurationLoader.load(Mapper.class);
            mapper = new Mapper(yamlMap);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    protected Map<T, U> internalMap;

    protected Mapper() {}

    private Mapper(Map<T, U> mapToUse) {
        internalMap = mapToUse;
    }

    /**
     * Return the value associated with this key.
     * Can return null! Some extensions of this class will not.
     * @param t
     * @return
     */
    public U map(T t) {
        return internalMap.get(t);
    }

    public static Mapper getInstance() {
        return mapper;
    }

    /**
     * For test purposes. Runs without error and prints the map if the config file is present and formatted correctly.
     * @param args
     */
    public static void main(String[] args) {
        Mapper mapper = getInstance();
        System.out.println(mapper.internalMap);
    }

}