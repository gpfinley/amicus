package edu.umn.amicus.util;

import edu.umn.amicus.Amicus;
import edu.umn.amicus.AmicusException;
import edu.umn.amicus.aligners.Aligner;
import edu.umn.amicus.aligners.PerfectOverlapAligner;
import edu.umn.amicus.distillers.Distiller;
import edu.umn.amicus.distillers.PriorityDistiller;
import edu.umn.amicus.summary.DocumentSummarizer;
import edu.umn.amicus.filters.Filter;
import edu.umn.amicus.filters.PassthroughFilter;
import edu.umn.amicus.filters.RegexFilter;
import edu.umn.amicus.mappers.Mapper;
import edu.umn.amicus.pullers.Puller;
import edu.umn.amicus.pushers.Pusher;
import edu.umn.amicus.summary.CollectionSummarizer;
import edu.umn.amicus.summary.EachSoloCsvSummarizer;
import edu.umn.amicus.SingleFieldAnnotation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Static methods to get Analysis Pieces. Will return defaults if none specified in the configuration.
 * For some, will check to be sure that the proper parameters have also been filled.
 *
 * Created by greg on 2/28/17.
 */
public final class AnalysisPieceFactory {

    private static ConcurrentMap<String, AnalysisPiece> allPieces = new ConcurrentHashMap<>();

    public static final String DEFAULT_PULLER = Puller.class.getName();
    public static final String DEFAULT_PUSHER = Pusher.class.getName();

    public static final String DEFAULT_TYPE_NAME = SingleFieldAnnotation.class.getName();
    public static final String DEFAULT_FIELD_NAME = "field";

    public static final String DEFAULT_ALIGNER = PerfectOverlapAligner.class.getName();
    public static final String DEFAULT_DISTILLER = PriorityDistiller.class.getName();

    public static final String DEFAULT_FILTER = RegexFilter.class.getName();
    public static final String DEFAULT_NOPATTERN_FILTER = PassthroughFilter.class.getName();

    public static final String DEFAULT_DOC_SUMMARIZER = EachSoloCsvSummarizer.class.getName();
    public static final String DEFAULT_COLLECTION_SUMMARIZER = EachSoloCsvSummarizer.class.getName();

    public static Puller puller(String pullerClassName, String fieldName) throws AmicusException {
        if (pullerClassName == null) {
            pullerClassName = DEFAULT_PULLER;
        }
        if (fieldName == null) {
            fieldName = DEFAULT_FIELD_NAME;
        }
        return getPieceInstance(Puller.class, pullerClassName, fieldName);
    }

    public static Pusher pusher(String pusherClassName, String typeName, String fieldName) throws AmicusException {
        if (pusherClassName == null) {
            pusherClassName = DEFAULT_PUSHER;
        }
        if (typeName == null) {
            typeName = DEFAULT_TYPE_NAME;
        }
        if (fieldName == null) {
            fieldName = DEFAULT_FIELD_NAME;
        }
        return getPieceInstance(Pusher.class, pusherClassName, typeName, fieldName);
    }

    public static Aligner aligner(String alignerClassName) throws AmicusException {
        return getPieceInstance(Aligner.class,
                alignerClassName == null ? DEFAULT_ALIGNER : alignerClassName);
    }

    public static Distiller distiller(String distillerClassName) throws AmicusException {
        return getPieceInstance(Distiller.class,
                distillerClassName == null ? DEFAULT_DISTILLER : distillerClassName);
    }

    public static Filter filter(String filterClassName, String pattern) throws AmicusException {
        if (filterClassName == null) {
            if (pattern != null) {
                filterClassName = DEFAULT_FILTER;
            } else {
                pattern = "";
                filterClassName = DEFAULT_NOPATTERN_FILTER;
            }
        }
        return getPieceInstance(Filter.class, filterClassName, pattern);
    }

    public static DocumentSummarizer microSummarizer(String microSummarizerClassName,
                                  String[] viewNames, String[] typeNames, String[] fieldNames) throws AmicusException {
        return getPieceInstance(DocumentSummarizer.class,
                microSummarizerClassName == null ? DEFAULT_DOC_SUMMARIZER : microSummarizerClassName,
                viewNames, typeNames, fieldNames);
    }

    public static CollectionSummarizer macroSummarizer(String macroSummarizerClassName,
                                  String[] viewNames, String[] typeNames, String[] fieldNames) throws AmicusException {
        return getPieceInstance(CollectionSummarizer.class,
                macroSummarizerClassName == null ? DEFAULT_COLLECTION_SUMMARIZER : macroSummarizerClassName,
                viewNames, typeNames, fieldNames);
    }

    /**
     * ...
     * Mappers work a little differently because they are not specified by named implementation, but rather by the
     *      path of the configuration file. They're stored in the same ConcurrentMap.
     * @param configPath
     * @return
     */
    public static Mapper mapper(String configPath) throws AmicusException {
        String keyPath = "Mapper_" + configPath;
        if (allPieces.containsKey(keyPath)) {
            return (Mapper) allPieces.get(keyPath);
        }
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            try {
                Class<? extends Mapper> mapperClass = (Class<? extends Mapper>) Class.forName(configPath);
                return mapperClass.getConstructor().newInstance();
            } catch (ReflectiveOperationException | ClassCastException e) {
                throw new AmicusException("No file found at %s.", configPath);
            }
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
            mapper.initialize();
            allPieces.put(keyPath, mapper);
            return mapper;
        } catch (IOException e) {
            throw new AmicusException("Could not read config file at %s.", configPath);
        }
    }

    /**
     * Every analysis piece will have its own ID that is unique to the level of the implementation and its arguments.
     * This is to prevent redundant instantiation for parallelized processing.
     * @param implName the class name to implement
     * @param args the arguments that will be passed to the constructor
     * @return a String key for this piece
     */
    private static String getStringId(String implName, Object... args) {
        StringBuilder idBuilder = new StringBuilder();
        idBuilder.append(implName);
        for (Object arg : args) {
            idBuilder.append("_");
            if (arg != null) {
                idBuilder.append(arg.toString());
            }
        }
        return idBuilder.toString();
    }

    /**
     * Create or fetch an AnalysisPiece. Used by static factory methods in this class.
     *
     * @param superclass the superclass of this AnalysisPiece type
     * @param implementationName the full name of the class to instantiate
     * @param args all arguments that class needs
     * @param <T> an AnalysisPiece implementation
     * @return a new instance or the previously created instance of implementationName
     */
    // todo: i've made it public in case API users want to use this. should i leave it that way?
    public static <T extends AnalysisPiece> T getPieceInstance(Class<T> superclass, String implementationName, Object... args) throws AmicusException {
        String id = getStringId(implementationName, args);
        T piece;
        try {
            piece = superclass.cast(allPieces.get(id));
            if (piece != null) return piece;
        } catch (ClassCastException e) {
            throw new AmicusException("%s is not an implementation of %s.", implementationName, superclass.getName());
        }

        try {
            piece = superclass.cast(instantiateClassFromArgs(implementationName, args));
        } catch (ClassCastException e) {
            throw new AmicusException("%s is not an implementation of %s.", implementationName, superclass.getName());
        } catch (ReflectiveOperationException e) {
            throw new AmicusException(e);
        }
        allPieces.putIfAbsent(id, piece);
        return piece;
    }

    /**
     * Try to instantiate a class from the given arguments.
     * If a constructor does not exist, try to call an empty constructor.
     * @param className the class to instantiate
     * @param args all arguments the class wants
     * @return a new object
     * @throws ReflectiveOperationException
     */
    private static Object instantiateClassFromArgs(String className, Object... args) throws ReflectiveOperationException {
        Class<?> clazz = Class.forName(className);
        Class[] parameterClasses = new Class[args.length];
        boolean argsHaveNullClasses = false;
        for (int i=0; i<args.length; i++) {
            if (args[i] == null) {
                argsHaveNullClasses = true;
            } else {
                parameterClasses[i] = args[i].getClass();
            }
        }
        if (!argsHaveNullClasses) {
            try {
                return clazz.getConstructor(parameterClasses).newInstance(args);
            } catch (NoSuchMethodException e) {
                return clazz.getConstructor().newInstance();
            }
        }
        // loop through constructors to see if we can find one that has the right number of arguments
        Constructor[] constructors = clazz.getConstructors();
        for (Constructor constructor : constructors) {
            if (parameterClasses.length == constructor.getParameterTypes().length) {
                useSameLengthConstructor: {
                    for (int i = 0; i < parameterClasses.length; i++) {
                        // don't use this one if the known (non-null argument) classes don't match
                        if (parameterClasses[i] != null && !parameterClasses[i].equals(constructor.getParameterTypes()[i])) {
                            break useSameLengthConstructor;
                        }
                    }
                    return constructor.newInstance(args);
                }
            }
        }
        // if no matching constructor was found, return the default and don't use the arguments
        return clazz.getConstructor().newInstance();
    }

}
