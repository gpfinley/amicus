//package edu.umn.ensembles.internal;
//
//import edu.umn.ensembles.Ensembles;
//import edu.umn.ensembles.uimacomponents.Util;
//import org.apache.uima.UimaContext;
//import org.apache.uima.analysis_component.CasAnnotator_ImplBase;
//import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
//import org.apache.uima.cas.CAS;
//import org.apache.uima.cas.TypeSystem;
//import org.apache.uima.cas.impl.XmiCasSerializer;
//import org.apache.uima.resource.ResourceInitializationException;
//import org.apache.uima.resource.metadata.TypeSystemDescription;
//import org.apache.uima.util.TypeSystemUtil;
//import org.xml.sax.SAXException;
//
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.logging.Logger;
//
///**
// * For internal use: uses UIMA descriptor config instead of the yml config file
// *
// * Created by gpfinley on 10/17/16.
// */
//public class XmiWriterAE extends CasAnnotator_ImplBase {
//
//    private static final Logger LOGGER = Logger.getLogger(XmiWriterAE.class.getName());
//
//    private static String mergedViewName;
//
//    private Path outputDir;
//
//    /**
//     * Force the XMI writer to choose a different view.
//     * @param viewName the name of the alternate view to write, or null to use default
//     */
//    public static void setViewToWrite(String viewName) {
//        mergedViewName = viewName;
//    }
//
//    /**
//     * Initializes the outputDirectory.
//     *
//     * @param context the uima context
//     * @throws ResourceInitializationException if we fail to initialize DocumentIdOutputStreamFactory
//     */
//    @Override
//    public void initialize(UimaContext context) throws ResourceInitializationException {
//        super.initialize(context);
//
//        outputDir = Paths.get((String) context.getConfigParameterValue("outputDirectory"));
//        try {
//            Files.createDirectories(outputDir);
//        } catch (IOException e) {
//            throw new ResourceInitializationException(e);
//        }
//    }
//
//    @Override
//    public void process(CAS cas) throws AnalysisEngineProcessException {
//
//        CAS mergedView = cas.getView(mergedViewName == null ? Ensembles.DEFAULT_MERGED_VIEW : mergedViewName);
//        TypeSystem typeSystem = mergedView.getTypeSystem();
//        TypeSystemDescription typeSystemDescription = TypeSystemUtil.typeSystem2TypeSystemDescription(typeSystem);
//        try {
//            typeSystemDescription.toXML(Files.newOutputStream(outputDir.resolve("TypeSystem.xml")));
//        } catch (IOException | SAXException e) {
//            throw new AnalysisEngineProcessException(e);
//        }
//
//        String docID = Util.getDocumentID(cas);
//        Path xmiOutPath = outputDir.resolve(docID + ".xmi");
//        try (OutputStream out = new FileOutputStream(xmiOutPath.toFile())) {
//            XmiCasSerializer.serialize(cas, out);
//        } catch (IOException | SAXException e) {
//            throw new AnalysisEngineProcessException(e);
//        }
//    }
//}
