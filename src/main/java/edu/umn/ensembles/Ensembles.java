package edu.umn.ensembles;

import edu.umn.ensembles.aligners.PerfectOverlapAligner;
import edu.umn.ensembles.creators.MultiCreator;
import edu.umn.ensembles.distillers.PriorityDistiller;
import edu.umn.ensembles.creators.SimpleCreator;
import edu.umn.ensembles.transformers.GetterTransformer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration, constants, and simple methods used by this application.
 *
 * Created by gpfinley on 10/17/16.
 */
public final class Ensembles {

//    private static Logger LOGGER = Logger.getLogger(Ensembles.class.getName());

    public final static Path CLASS_CONFIG_DIR = Paths.get("classConfigurations");
    public final static Path TYPE_SYSTEMS_DIR = Paths.get("typeSystems");
    public final static Path MY_TYPE_SYSTEM = TYPE_SYSTEMS_DIR.resolve("EnsemblesTypeSystem.xml");

    public final static Class DEFAULT_ALIGNER_CLASS = PerfectOverlapAligner.class;
    public final static Class DEFAULT_TRANSFORMER_CLASS = GetterTransformer.class;
    public final static Class DEFAULT_DISTILLER_CLASS = PriorityDistiller.class;
    public final static Class DEFAULT_CREATOR_CLASS = SimpleCreator.class;
    public final static Class DEFAULT_MULTI_CREATOR_CLASS = MultiCreator.class;

    /**
     * View names used by UIMA modules
     */
    // view holding the basename of all XMI files
    public static final String DOCID_VIEW = "_InitialView";
    // view holding the contents of the merging analysis
    public static final String DEFAULT_MERGED_VIEW = "MergedView";

}
