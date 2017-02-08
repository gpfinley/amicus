package edu.umn.ensembles.internal;

import edu.umn.biomedicus.type.TokenAnnotation;
import edu.umn.biomedicus.uima.type1_6.Acronym;
import edu.umn.ensembles.processing.Counter;
import edu.umn.ensembles.processing.EquivalentAnswerMapper;
import edu.umn.ensembles.processing.Mapper;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Used only to process UMN-internal data.
 * Our abbreviation reference standard includes inconsistent strings for the same concept in many cases.
 * This processor collapses those down (via a manually generated mapping).
 * Be sure the correct configuration file for the EquivalentAnswerMapper is in place.
 *
 * Created by gpfinley on 11/10/16.
 */
public class RefStdProcessor extends JCasAnnotator_ImplBase {

    private static Mapper<String, String> replacementMapper;

    private static final Logger LOGGER = Logger.getLogger(RefStdProcessor.class.getName());

    private Counter<String> abbrs;
    private Map<String, Counter<String>> expansions;

    @Override
    public void initialize(UimaContext context) {
        replacementMapper = EquivalentAnswerMapper.getInstance();
        abbrs = new Counter<>();
        expansions = new HashMap<>();
    }

    @Override
    public void process(JCas jCas) {

        List<TokenAnnotation> acrTokens = new ArrayList<>();
        try {
            jCas = jCas.getView("SystemView");
        } catch (CASException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        jCas.getAnnotationIndex(TokenAnnotation.class).forEach(a -> {
            if (isAcronym(a))
                acrTokens.add(a);
        });
        acrTokens.sort((x, y) -> ((Integer) x.getBegin()).compareTo(y.getBegin()));

        // might remove annotations from this list as we cycle through the original annotations
        List<Acronym> acronymsToAdd = new ArrayList<>();

        for (TokenAnnotation token : acrTokens) {
            int newBegin = token.getBegin();
            int newEnd = token.getEnd();
            String oldExpansion = token.getAcronymAbbrevExpansion();
            String newExpansion = replacementMapper.map(oldExpansion);
            if (acronymsToAdd.size() > 0) {
                Acronym lastAcronym = acronymsToAdd.get(acronymsToAdd.size() - 1);
                if (lastAcronym.getEnd() == token.getBegin()
                            && lastAcronym.getCoveredText().matches("\\w\\W?")) {
                    LOGGER.info("merging " + lastAcronym.getCoveredText() + " with " + token.getCoveredText());
                    newBegin = lastAcronym.getBegin();
                    acronymsToAdd.remove(acronymsToAdd.size()-1);
                    String lastText = lastAcronym.getText();
                    if (!lastText.contains(oldExpansion)) {
                        newExpansion = replacementMapper.map(lastText + oldExpansion);
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
                expansions.put(thisAbbr, new Counter<>());
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

}
