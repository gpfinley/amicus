package edu.umn.ensembles.uimacomponents;

import edu.umn.ensembles.EnsemblesException;
import org.apache.uima.cas.*;
import org.apache.uima.cas.impl.XCASDeserializer;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.fit.component.CasMultiplier_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.util.CasCopier;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Add a CAS from an xml/xmi file in the specified dir whose filename matches the doc ID added by CommonFilenameCR.
 *
 * Created by gpfinley on 1/18/17 (converted to uimaFIT from old MultiCasReader).
 */
public class CasAdderAE extends CasMultiplier_ImplBase {

    private static final Logger LOGGER = Logger.getLogger(CasAdderAE.class.getName());

//    public static final String SYSTEM_NAME = "systemName";
    public static final String DATA_DIR = "dataDir";
//    public static final String VIEW_NAME = "viewName";
    public static final String READ_FROM_VIEW = "readFromView";
    public static final String COPY_INTO_VIEW = "saveIntoView";

//    @ConfigurationParameter(name = SYSTEM_NAME)
//    private String systemName;
    @ConfigurationParameter(name = DATA_DIR)
    private String dataDirname;
    @ConfigurationParameter(name = READ_FROM_VIEW)
    private String fromView;
    @ConfigurationParameter(name = COPY_INTO_VIEW)
    private String toView;


    @Override
    public int getCasInstancesRequired() {
        return 2;
    }

    @Override
    public void process(CAS cas) {
        Path dataDir = Paths.get(dataDirname);

        CAS tempCas = getEmptyCAS();
        String docID = Util.getDocumentID(cas);
        File xmiFile = dataDir.resolve(docID + ".xmi").toFile();
        try {
            if (xmiFile.exists()) {
                XmiCasDeserializer.deserialize(new FileInputStream(xmiFile), tempCas);
            } else {
                File xmlFile = dataDir.resolve(docID + ".xml").toFile();
                if (xmlFile.exists()) {
                    XCASDeserializer.deserialize(new FileInputStream(xmlFile), tempCas);
                } else {
                    LOGGER.severe("Couldn't find xmi or xml file (make sure extensions are lowercase)");
                    throw new IOException();
                }
            }
        } catch (SAXException | IOException e) {
            LOGGER.severe("Couldn't parse xmi or xml file");
            throw new EnsemblesException(e);
        }
        CAS newSysView = cas.createView(toView);
        CasCopier casCopier = new CasCopier(tempCas, cas);
        CAS relevantView;
        try {
            relevantView = tempCas.getView(fromView);
        } catch (NullPointerException e) {
            LOGGER.severe(String.format("Check config file: no view named '%s' for XMI CAS at %s", fromView, xmiFile.getAbsolutePath()));
            throw new RuntimeException();
        }
        casCopier.copyCasView(relevantView, newSysView, false);
        newSysView.setSofaDataString(tempCas.getView(fromView).getSofaDataString(), tempCas.getView(fromView).getSofaMimeType());
        tempCas.release();
    }

    /**
     * Doesn't output new CASes to iterate through; only uses CasMultiplier functionality internally to process().
     * @return false
     */
    @Override
    public boolean hasNext() {
        return false;
    }

    /**
     * Doesn't output new CASes to iterate through; only uses CasMultiplier functionality internally to process().
     * @return nothing, always throws exception
     */
    @Override
    public AbstractCas next() {
        throw new RuntimeException();
    }

}
