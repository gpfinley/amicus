package edu.umn.ensembles.internal;

import edu.umn.ensembles.Main;
import edu.umn.ensembles.uimafit.XmiWriter;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Preprocess our reference standard:
 *      - fix spelling errors and variants in the annotations
 *      - merge consecutive annotations that are part of the same term ("A" "/" "P", etc.)
 * Created by gpfinley on 11/8/16.
 */
//todo: undeprecate by instantiating a uimaFIT pipeline here
@Deprecated
public class AcronymRefStandardPreprocessScript {

    private static final String PREPROCESS_CPE = "descriptors/umn-internal/RefStdPreprocessCPE.xml";

    public static void main(String[] args) throws IOException {
//        XmiWriter.setTypeSystemView("SystemView");
        Main.runCpe(Main.xmlToCpe(Paths.get(PREPROCESS_CPE)));
    }
}
