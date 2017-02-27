package edu.umn.amicus;

import edu.umn.amicus.aligners.EachSoloAligner;
import edu.umn.amicus.export.EachSoloTsvExportWriter;
import org.yaml.snakeyaml.Yaml;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Configuration, constants, and simple methods used by this application.
 *
 * Created by gpfinley on 10/17/16.
 */
public final class Amicus {

    private static ConcurrentMap<String, Piece> allPieces = new ConcurrentHashMap<>();

    public final static Path CLASS_CONFIG_DIR = Paths.get("classConfigurations");

    public final static Class<EachSoloTsvExportWriter> DEFAULT_EXPORTER_CLASS = EachSoloTsvExportWriter.class;
    public final static Class<EachSoloAligner> DEFAULT_ALIGNER_CLASS_FOR_EXPORTER = EachSoloAligner.class;

    /**
     * View names used by UIMA modules
     */
    // view holding the basename of all XMI files
    public static final String DOCID_VIEW = "_InitialView";

    // todo: get rid of this?
    // view holding the contents of the merging analysis
    public static final String DEFAULT_MERGED_VIEW = "MergedView";

    public final static Yaml yaml = new Yaml();

    /**
     * Try to instantiate a class from the given arguments.
     * If a constructor does not exist, try to call an empty constructor.
     * @param className the class to instantiate
     * @param args all arguments the class wants
     * @return a new object
     * @throws ReflectiveOperationException
     */
    public static Object instantiateClassFromArgs(String className, Object... args) throws ReflectiveOperationException {
        Class[] classes = new Class[args.length];
        for (int i=0; i<args.length; i++) {
            classes[i] = args[i].getClass();
        }
        Class<?> clazz = Class.forName(className);
        try {
            return clazz.getConstructor(classes).newInstance(args);
        } catch(NoSuchMethodException e) {
            return clazz.getConstructor().newInstance();
        }
    }

    /**
     * Create or fetch a Piece. Used by factories.
     *
     * @param superclass the superclass of this Piece type
     * @param implementationName the full name of the class to instantiate
     * @param args all arguments that class needs
     * @param <T> a Piece implementation
     * @return a new instance or the previously created instance of implementationName
     */
    public static <T extends Piece> T getPieceInstance(Class<T> superclass, String implementationName, Object... args) {
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
