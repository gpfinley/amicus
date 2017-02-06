package edu.umn.ensembles.uimacomponents;

import edu.umn.ensembles.Ensembles;
import edu.umn.ensembles.Util;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.*;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.component.CasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.TypeSystemUtil;
import org.xml.sax.SAXException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Created by gpfinley on 10/17/16.
 */
public class XmiWriter extends CasAnnotator_ImplBase {

    private static final Logger LOGGER = Logger.getLogger(XmiWriter.class.getName());

    public final static String CONFIG_OUTPUT_DIR = "outputDir";
    public final static String TYPE_SYSTEM_VIEW = "typeSystemView";

    @ConfigurationParameter(name = CONFIG_OUTPUT_DIR)
    private String outputDirName;
    @ConfigurationParameter(name = TYPE_SYSTEM_VIEW, defaultValue = Ensembles.DEFAULT_MERGED_VIEW)
    private static String typeSystemView;

    private Path outputDir;

    /**
     * Initializes the outputDirectory.
     *
     * @param context the uima context
     * @throws ResourceInitializationException if we fail to initialize DocumentIdOutputStreamFactory
     */
    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        outputDir = Paths.get(outputDirName);
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(CAS cas) throws AnalysisEngineProcessException {

        // todo: implement concurrency/semaphores for this (can copy from biomedicus) so that it only writes TS once

//        String typeSystemView = mergedViewName == null ? Ensembles.DEFAULT_MERGED_VIEW : mergedViewName;
        try {
            CAS mergedView = cas.getView(typeSystemView);
            TypeSystem typeSystem = mergedView.getTypeSystem();
            TypeSystemDescription typeSystemDescription = TypeSystemUtil.typeSystem2TypeSystemDescription(typeSystem);
            try {
                typeSystemDescription.toXML(Files.newOutputStream(outputDir.resolve("TypeSystem.xml")));
            } catch (IOException | SAXException e) {
                throw new AnalysisEngineProcessException(e);
            }
        } catch (CASRuntimeException e) {
            LOGGER.warning(String.format("No view with name %s; not writing type system", typeSystemView));
        }

        String docID = Util.getDocumentID(cas);
        Path xmiOutPath = outputDir.resolve(docID + ".xmi");
        try (OutputStream out = new FileOutputStream(xmiOutPath.toFile())) {
            XmiCasSerializer.serialize(cas, out);
        } catch (IOException | SAXException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
