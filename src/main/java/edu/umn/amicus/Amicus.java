package edu.umn.amicus;

import edu.umn.amicus.aligners.EachSoloAligner;
import edu.umn.amicus.aligners.PerfectOverlapAligner;
import edu.umn.amicus.exporters.TsvExporter;
import edu.umn.amicus.pullers.MultiGetterPuller;
import edu.umn.amicus.pushers.MultiPusher;
import edu.umn.amicus.distillers.PriorityDistiller;
import edu.umn.amicus.pushers.SimplePusher;
import edu.umn.amicus.pullers.GetterPuller;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration, constants, and simple methods used by this application.
 *
 * Created by gpfinley on 10/17/16.
 */
public final class Amicus {

    public final static Path CLASS_CONFIG_DIR = Paths.get("classConfigurations");

    // todo: can we do without runtime type systems if they're being handled through autodetection and maven building?
    public final static Path TYPE_SYSTEMS_DIR = Paths.get("typeSystems");
    public final static Path MY_TYPE_SYSTEM = TYPE_SYSTEMS_DIR.resolve("EnsemblesTypeSystem.xml");

    public final static Class<PerfectOverlapAligner> DEFAULT_ALIGNER_CLASS = PerfectOverlapAligner.class;
    public final static Class<TsvExporter> DEFAULT_EXPORTER_CLASS = TsvExporter.class;
    public final static Class<EachSoloAligner> DEFAULT_ALIGNER_CLASS_FOR_EXPORTER = EachSoloAligner.class;
    public final static Class<PriorityDistiller> DEFAULT_DISTILLER_CLASS = PriorityDistiller.class;
    public final static Class<GetterPuller> DEFAULT_PULLER_CLASS = GetterPuller.class;
    // todo: figure out where multi pusher needs to be used and use it
    public final static Class<MultiGetterPuller> DEFAULT_MULTI_PULLER_CLASS = MultiGetterPuller.class;
    public final static Class<SimplePusher> DEFAULT_PUSHER_CLASS = SimplePusher.class;
    public final static Class<MultiPusher> DEFAULT_MULTI_PUSHER_CLASS = MultiPusher.class;

    /**
     * View names used by UIMA modules
     */
    // view holding the basename of all XMI files
    public static final String DOCID_VIEW = "_InitialView";

    // todo: get rid of this?
    // view holding the contents of the merging analysis
    public static final String DEFAULT_MERGED_VIEW = "MergedView";

}
