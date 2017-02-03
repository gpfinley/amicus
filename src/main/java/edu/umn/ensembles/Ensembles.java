package edu.umn.ensembles;

import edu.umn.ensembles.aligners.PerfectOverlapAligner;
//import edu.umn.ensembles.config.AppConfiguration;
import edu.umn.ensembles.config.InputTypeBean;
import edu.umn.ensembles.config.MergerConfigBean;
import edu.umn.ensembles.creators.MultiCreator;
import edu.umn.ensembles.distillers.PriorityDistiller;
import edu.umn.ensembles.creators.SimpleCreator;
import edu.umn.ensembles.transformers.GetterTransformer;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Configuration, constants, and simple methods used by this application.
 *
 * Created by gpfinley on 10/17/16.
 */
public final class Ensembles {

    private static Logger LOGGER = Logger.getLogger(Ensembles.class.getName());

    public final static Class DEFAULT_ALIGNER_CLASS = PerfectOverlapAligner.class;
    public final static Class DEFAULT_TRANSFORMER_CLASS = GetterTransformer.class;
    public final static Class DEFAULT_DISTILLER_CLASS = PriorityDistiller.class;
    public final static Class DEFAULT_CREATOR_CLASS = SimpleCreator.class;
    public final static Class DEFAULT_MULTI_CREATOR_CLASS = MultiCreator.class;

//    private final static Map<String, String> usedSystemXmiDirs;
//
//    private final static Set<String> usedOutputViews;

    /**
     * View names used by UIMA modules
     */
    // view holding the basename of all XMI files
    public static final String DOCID_VIEW = "_InitialView";
    // view holding the contents of the merging analysis
    public static final String MERGED_VIEW = "MergedView";


//    public static final AppConfiguration configuration;
//    static {
//        String configPath = System.getProperties().getProperty("config");
//        if (configPath == null) {
//            LOGGER.severe("Need to provide a configuration YAML file as a JVM argument: -Dconfig=<path_to_file>");
//            System.exit(1);
//        }
//        AppConfiguration mc;
//        try {
//            mc = AppConfiguration.load(new FileInputStream(configPath));
//        } catch (IOException e) {
//            LOGGER.severe("Couldn't load config file; check formatting.");
//            throw new EnsemblesException();
//        }
//        configuration = mc;
//
//        // only look in data directories of systems that are actually utilized by any of the mergers
//        Set<String> usedSystems = new HashSet<>();
//        configuration.getMergerConfigurations().stream()
//                .map(MergerConfigBean::getInputs)
//                .forEach(b -> Arrays.asList(b).stream()
//                        .map(InputTypeBean::getFromSystem)
//                        .forEach(usedSystems::add));
//        usedSystemXmiDirs = configuration.getSystemXmiDirs().entrySet().stream()
//                .filter(e -> usedSystems.contains(e.getKey()))
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//        usedOutputViews = configuration.getMergerConfigurations().stream()
//                .map(MergerConfigBean::getOutputViewName)
//                .collect(Collectors.toSet());
//    }

    /**
     * Return the systems and their directories only for those systems whose annotations
     * are actually used by this configuration.
     * This prevents configurations with inconsequential errors from encountering exceptions.
     * This method is preferable to configuration.getSystemXmiDirs() in most cases.
     * @return maps between system names and the directories containing their annotations
     */
//    public static Map<String, String> getUsedXmiDirs() {
//        return usedSystemXmiDirs;
//    }
//
//    public static Set<String> getUsedOutputViews() {
//        return usedOutputViews;
//    }

    public static String systemToViewName(String systemName) {
        return systemName + "View";
    }

    /**
     * Get the name of a getter method for a given field.
     * Should operate under the exact same rules that UIMA uses when generating sources.
     * @param field the name of a field
     * @return the name of the field's getter method
     */
    public static String getGetterFor(String field) {
        return "get" + field.substring(0, 1).toUpperCase() + field.substring(1, field.length());
    }

    /**
     * Get the name of a setter method for a given field.
     * Should operate under the exact same rules that UIMA uses when generating sources.
     * @param field the name of a field
     * @return the name of the field's setter method
     */
    public static String getSetterFor(String field) {
        return "set" + field.substring(0, 1).toUpperCase() + field.substring(1, field.length());
    }


    // todo: delete. Don't do it this way; define through typesystem and use jcasgen
//    /**
//     * Simple implementation of a UIMA annotation that stores a single string value
//     */
//    public static class SingleFieldAnnotation extends Annotation {
//        private String field;
//
//        public SingleFieldAnnotation(JCas jCas, int begin, int end) {
//            super(jCas, begin, end);
//        }
//
//        public String getField() {
//            return field;
//        }
//
//        public void setField(String field) {
//            this.field = field;
//        }
//    }

}
