package edu.umn.amicus;

import edu.umn.amicus.aligners.EachSoloAligner;
import edu.umn.amicus.export.EachSoloTsvExportWriter;
import org.yaml.snakeyaml.Yaml;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Configuration, constants, and simple methods used by this application.
 *
 * Created by gpfinley on 10/17/16.
 */
public final class Amicus {

    private static ConcurrentMap<String, AnalysisPiece> allPieces = new ConcurrentHashMap<>();

    public final static Path CLASS_CONFIG_DIR = Paths.get("classConfigurations");

    public final static Class<EachSoloTsvExportWriter> DEFAULT_EXPORTER_CLASS = EachSoloTsvExportWriter.class;
    public final static Class<EachSoloAligner> DEFAULT_ALIGNER_CLASS_FOR_EXPORTER = EachSoloAligner.class;

    public final static String CONCATENATED_STRING_DELIMITER = "|";

    /**
     * View names used by UIMA modules
     */
    // view holding the basename of all XMI files
    public static final String DOCID_VIEW = "_InitialView";

    // todo: get rid of this?
    // view holding the contents of the merging analysis
    public static final String DEFAULT_MERGED_VIEW = "MergedView";

    public final static Yaml yaml = new Yaml();

    // set this once. If a sofa is based on a different string, we have a problem
    private static String sofaData;

    public static void verifySofaData(String sofaData) {
        if (Amicus.sofaData == null) {
            Amicus.sofaData = sofaData;
        } else {
            if (!Amicus.sofaData.equals(sofaData)) {
                throw new AmicusException("Data does not match across sofas!");
            }
        }
    }

    /**
     * Try to instantiate a class from the given arguments.
     * If a constructor does not exist, try to call an empty constructor.
     * @param className the class to instantiate
     * @param args all arguments the class wants
     * @return a new object
     * @throws ReflectiveOperationException
     */
    public static Object instantiateClassFromArgs(String className, Object... args) throws ReflectiveOperationException {
        Class<?> clazz = Class.forName(className);
        Class[] classes = new Class[args.length];
        boolean argsHaveNullClasses = false;
        for (int i=0; i<args.length; i++) {
            if (args[i] == null) {
                argsHaveNullClasses = true;
            } else {
                classes[i] = args[i].getClass();
            }
        }
        // todo: should it fall back on a default (empty) constructor if this doesn't exist? Or should implementations be forced to carry it?
        if (!argsHaveNullClasses) {
            return clazz.getConstructor(classes).newInstance(args);
        }
        // loop through constructors to see if we can find one that has the right number of arguments
        Constructor[] constructors = clazz.getConstructors();
        for (Constructor constructor : constructors) {
            if (classes.length == constructor.getParameterTypes().length) {
                useSameLengthConstructor: {
                    for (int i = 0; i < classes.length; i++) {
                        // don't use this one if the known (non-null argument) classes don't match
                        if (classes[i] != null && !classes[i].equals(constructor.getParameterTypes()[i])) {
                            break useSameLengthConstructor;
                        }
                    }
                    return constructor.newInstance(args);
                }
            }
        }

        // if no matching constructor was found, return the default and don't use the arguments
        // todo: warn
        return clazz.getConstructor().newInstance();
    }

    /**
     * Create or fetch a AnalysisPiece. Used by factories.
     *
     * @param superclass the superclass of this AnalysisPiece type
     * @param implementationName the full name of the class to instantiate
     * @param args all arguments that class needs
     * @param <T> a AnalysisPiece implementation
     * @return a new instance or the previously created instance of implementationName
     */
    public static <T extends AnalysisPiece> T getPieceInstance(Class<T> superclass, String implementationName, Object... args) {
        StringBuilder idBuilder = new StringBuilder();
        idBuilder.append(implementationName);
        for (Object arg : args) {
            idBuilder.append("_");
            if (arg != null) {
                idBuilder.append(arg.toString());
            }
        }
        String id = idBuilder.toString();
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

}
