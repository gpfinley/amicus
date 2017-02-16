package edu.umn.amicus.internal;

import java.io.IOException;

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
//        XmiWriterAE.setTypeSystemView("SystemView");
//        Main.runCpe(Main.xmlToCpe(Paths.get(PREPROCESS_CPE)));
    }
}
