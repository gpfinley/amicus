package edu.umn.ensembles.uimafit;

import edu.umn.ensembles.Ensembles;
import edu.umn.ensembles.EnsemblesException;
import org.apache.uima.cas.AbstractCas;
import org.apache.uima.cas.CAS;
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
 * Requires annotations provided by the collection reader FilenameReader.
 *
 * Created by gpfinley on 1/18/17 (converted to uimaFIT from old MultiCasReader).
 */
public class MultiSystemDataReader extends CasMultiplier_ImplBase {

    private static final Logger LOGGER = Logger.getLogger(MultiSystemDataReader.class.getName());

    public static final String SYSTEM_NAMES = "systemNames";
    public static final String DATA_DIRS = "dataDirs";
    public static final String VIEW_NAMES = "viewNames";

    @ConfigurationParameter(name = SYSTEM_NAMES)
    private String[] systemNames;
    @ConfigurationParameter(name = DATA_DIRS)
    private String[] dataDirs;
    @ConfigurationParameter(name = VIEW_NAMES)
    private String[] viewNames;

    @Override
    public int getCasInstancesRequired() {
        return 2;
    }

    @Override
    public void process(CAS cas) {
        try {
            assert systemNames.length == dataDirs.length && systemNames.length == viewNames.length;
        } catch (AssertionError e) {
            throw new EnsemblesException("Configuration parameter lists should be the same length!");
        }

        for (int i=0; i<systemNames.length; i++) {
            String systemName = systemNames[i];
            Path dataDir = Paths.get(dataDirs[i]);
            String originalViewName = viewNames[i];

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
                e.printStackTrace();
                throw new RuntimeException();
            }
            CAS newSysView = cas.createView(Ensembles.systemToViewName(systemName));
            CasCopier casCopier = new CasCopier(tempCas, cas);
            CAS relevantView;
            try {
                relevantView = tempCas.getView(originalViewName);
            } catch (NullPointerException e) {
                LOGGER.severe(String.format("Check config file: no view named '%s' for XMI CAS at %s", originalViewName, xmiFile.getAbsolutePath()));
                throw new RuntimeException();
            }
            casCopier.copyCasView(relevantView, newSysView, false);
            newSysView.setSofaDataString(tempCas.getView(originalViewName).getSofaDataString(), tempCas.getView(originalViewName).getSofaMimeType());
            tempCas.release();
        }
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
