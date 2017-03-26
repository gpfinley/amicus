package edu.umn.amicus.internal;

import edu.umn.amicus.AmicusException;
import edu.umn.amicus.config.SourceSystemConfig;
import edu.umn.amicus.uimacomponents.CasAdderAE;
import edu.umn.amicus.uimacomponents.CommonFilenameCR;
import edu.umn.amicus.uimacomponents.XmiWriterAE;
import edu.umn.biomedicus.type.TokenAnnotation;
import edu.umn.biomedicus.uima.type1_6.Acronym;
import edu.umn.amicus.Counter;
import edu.umn.amicus.mappers.Mapper;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.yaml.snakeyaml.Yaml;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Used only to process UMN-internal data.
 * Our abbreviation reference standard includes inconsistent strings for the same concept in many cases.
 * This processor collapses those down (via a manually generated mapping).
 * Be sure the correct configuration file for the EquivalentAnswerMapper is in place.
 *
 * Created by gpfinley on 11/10/16.
 */
public class RefStandardProcessor extends JCasAnnotator_ImplBase {

    private static Mapper replacementMapper;

    private static final Logger LOGGER = Logger.getLogger(RefStandardProcessor.class.getName());

    private Counter<String> abbrs;
    private Map<String, Counter<String>> expansions;

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
//        replacementMapper = EquivalentAnswerMapper.getInstance();
        try {
            replacementMapper = (Mapper) new Yaml().load(new FileReader("mapperConfigurations/newEquivMapper.yml"));
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
        abbrs = new Counter<>();
        expansions = new HashMap<>();
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {

        List<TokenAnnotation> acrTokens = new ArrayList<>();
        try {
            jCas = jCas.getView("SystemView");
        } catch (CASException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        for (TokenAnnotation a : jCas.getAnnotationIndex(TokenAnnotation.class)) {
            if (isAcronym(a)) acrTokens.add(a);
        }
        acrTokens.sort(new Comparator<TokenAnnotation>() {
            @Override
            public int compare(TokenAnnotation o1, TokenAnnotation o2) {
                return ((Integer) o1.getBegin()).compareTo(o2.getBegin());
            }
        });
        // java 8
//        acrTokens.sort((x, y) -> ((Integer) x.getBegin()).compareTo(y.getBegin()));

        // might remove annotations from this list as we cycle through the original annotations
        List<Acronym> acronymsToAdd = new ArrayList<>();

        for (TokenAnnotation token : acrTokens) {
            int newBegin = token.getBegin();
            int newEnd = token.getEnd();
            String oldExpansion = token.getAcronymAbbrevExpansion();
            String newExpansion;
            try {
                newExpansion = (String) replacementMapper.map(oldExpansion);
            } catch (AmicusException e) {
                throw new AnalysisEngineProcessException();
            }
            if (acronymsToAdd.size() > 0) {
                Acronym lastAcronym = acronymsToAdd.get(acronymsToAdd.size() - 1);
                if (lastAcronym.getEnd() == token.getBegin()
                            && lastAcronym.getCoveredText().matches("\\w\\W?")) {
                    LOGGER.info("merging " + lastAcronym.getCoveredText() + " with " + token.getCoveredText());
                    newBegin = lastAcronym.getBegin();
                    acronymsToAdd.remove(acronymsToAdd.size()-1);
                    String lastText = lastAcronym.getText();
                    if (!lastText.contains(oldExpansion)) {
                        try {
                            newExpansion = (String) replacementMapper.map(lastText + oldExpansion);
                        } catch (AmicusException e) {
                            throw new AnalysisEngineProcessException(e);
                        }
                    }
                }
            }
            Acronym acronym = new Acronym(jCas, newBegin, newEnd);
            acronym.setText(newExpansion);
            acronymsToAdd.add(acronym);
        }

        for (Acronym acronym : acronymsToAdd) {
            acronym.addToIndexes();

            String thisAbbr = acronym.getCoveredText();
            if (abbrs.increment(thisAbbr) == 1) {
                expansions.put(thisAbbr, new Counter<String>());
            }
            expansions.get(thisAbbr).increment(acronym.getText());

        }
    }

    private boolean isAcronym(TokenAnnotation annotation) {
        String exp = annotation.getAcronymAbbrevExpansion();
        return exp != null
                && !exp.matches("\\d+");
    }

    @Override
    public void collectionProcessComplete() {
        System.out.println(abbrs);
        System.out.println(expansions);
    }

    public static void main(String[] args) throws Exception {
        String inpath = args[0];
        String outpath = args[1];

        CollectionReader reader = CollectionReaderFactory.createReader(CommonFilenameCR.class,
                CommonFilenameCR.SYSTEM_DATA_DIRS, new String[]{inpath});

        AnalysisEngine[] engines = new AnalysisEngine[3];

        engines[0] = AnalysisEngineFactory.createEngine(CasAdderAE.class,
                    CasAdderAE.DATA_DIR, inpath,
                    CasAdderAE.READ_FROM_VIEW, "SystemView",
                    CasAdderAE.COPY_INTO_VIEW, "SystemView"
            );

        engines[1] = AnalysisEngineFactory.createEngine(RefStandardProcessor.class);

        engines[2] = AnalysisEngineFactory.createEngine(XmiWriterAE.class,
                XmiWriterAE.CONFIG_OUTPUT_DIR, outpath);

        SimplePipeline.runPipeline(reader, engines);
    }

}