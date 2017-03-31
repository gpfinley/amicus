//package edu.umn.amicus.internal;
//
//import edu.umn.amicus.uimacomponents.CasAdderAE;
//import edu.umn.amicus.uimacomponents.CommonFilenameCR;
//import edu.umn.amicus.uimacomponents.XmiWriterAE;
//import edu.umn.biomedicus.type.TokenAnnotation;
//import edu.umn.biomedicus.uima.type1_6.Acronym;
//import edu.umn.amicus.Counter;
//import edu.umn.amicus.mappers.Mapper;
//import org.apache.uima.UimaContext;
//import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
//import org.apache.uima.analysis_engine.AnalysisEngine;
//import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
//import org.apache.uima.cas.CASException;
//import org.apache.uima.collection.CollectionReader;
//import org.apache.uima.fit.factory.AnalysisEngineFactory;
//import org.apache.uima.fit.factory.CollectionReaderFactory;
//import org.apache.uima.fit.pipeline.SimplePipeline;
//import org.apache.uima.jcas.JCas;
//import org.apache.uima.resource.ResourceInitializationException;
//import org.yaml.snakeyaml.Yaml;
//
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.*;
//import java.util.logging.Logger;
//
///**
// * Used only to process UMN-internal data.
// * Our abbreviation reference standard includes inconsistent strings for the same concept in many cases.
// * This processor collapses those down (via a manually generated mapping).
// * Be sure the correct configuration file for the EquivalentAnswerMapper is in place.
// *
// * Created by gpfinley on 11/10/16.
// */
//public class RefStandardProcessor extends JCasAnnotator_ImplBase {
//
//    private static Mapper replacementMapper;
//
//    private static final Logger LOGGER = Logger.getLogger(RefStandardProcessor.class.getName());
//
//    private Counter<String> abbrs;
//    private Map<String, Counter<String>> expansions;
//
//    @Override
//    public void initialize(UimaContext context) throws ResourceInitializationException {
////        replacementMapper = EquivalentAnswerMapper.getInstance();
//        try {
//            replacementMapper = (Mapper) new Yaml().load(new FileReader("mapperConfigurations/newEquivMapper.yml"));
//        } catch (IOException e) {
//            throw new ResourceInitializationException(e);
//        }
//        abbrs = new Counter<>();
//        expansions = new HashMap<>();
//    }
//
//    private void saveNewAcronym(JCas jCas, int begin, int end, String sense) {
//        TokenAnnotation acr = new TokenAnnotation(jCas, begin, end);
//        acr.setIsAcronymAbbrev(true);
//        acr.setAcronymAbbrevExpansion(sense);
//        acr.addToIndexes();
//    }
//
//    @Override
//    public void process(JCas jCas) throws AnalysisEngineProcessException {
//
//        try {
//            jCas.createView("nullView");
//            jCas.getView("nullView").setSofaDataString(jCas.getSofaDataString(), "text");
//            Acronym acronym = new Acronym(jCas.getView("nullView"), 0, 10);
//            acronym.addToIndexes();
//        } catch (CASException e) {
//            throw new AnalysisEngineProcessException(e);
//        }
//
//        try {
//            jCas = jCas.getView("SystemView");
//        } catch (CASException e) {
//            e.printStackTrace();
//            throw new RuntimeException();
//        }
//
//        List<TokenAnnotation> acrTokens = new ArrayList<>();
//        for (TokenAnnotation a : jCas.getAnnotationIndex(TokenAnnotation.class)) {
//            if (isAcronym(a)) acrTokens.add(a);
//        }
//        if (acrTokens.size() == 0) return;
//
//        acrTokens.sort(new Comparator<TokenAnnotation>() {
//            @Override
//            public int compare(TokenAnnotation o1, TokenAnnotation o2) {
//                return ((Integer) o1.getBegin()).compareTo(o2.getBegin());
//            }
//        });
//
//        List<TokenAnnotation> allTokens = new ArrayList<>();
//        for (TokenAnnotation a : jCas.getAnnotationIndex(TokenAnnotation.class)) {
//            allTokens.add(a);
//        }
//        Iterator<TokenAnnotation> tokenIterator = allTokens.iterator();
//        if (!tokenIterator.hasNext()) return;
//        TokenAnnotation nextTok = tokenIterator.next();
//        while (true) {
//            int begin = nextTok.getBegin();
//            int end = nextTok.getEnd();
//            String sense = nextTok.getAcronymAbbrevExpansion();
//            if (sense == null) {
//                if (!tokenIterator.hasNext()) {
//                    break;
//                }
//                nextTok = tokenIterator.next();
//                continue;
//            }
//            if (tokenIterator.hasNext()) {
//                nextTok = tokenIterator.next();
//                while (sense.equalsIgnoreCase(nextTok.getAcronymAbbrevExpansion())) {
//                    System.out.println(sense);
//                    end = nextTok.getEnd();
//                    if (tokenIterator.hasNext()) {
//                        nextTok = tokenIterator.next();
//                    } else {
//                        break;
//                    }
//                }
//            } else {
//                saveNewAcronym(jCas, begin, end, sense);
//                break;
//            }
//            saveNewAcronym(jCas, begin, end, sense);
//        }
//
//        for (TokenAnnotation token : allTokens) {
//            if (token.getIsAcronymAbbrev()) {
//                token.removeFromIndexes();
//            }
//        }
//
//    }
//
//    private boolean isAcronym(TokenAnnotation annotation) {
//        String exp = annotation.getAcronymAbbrevExpansion();
//        return exp != null
//                && !exp.matches("\\d+");
//    }
//
//    @Override
//    public void collectionProcessComplete() {
//        System.out.println(abbrs);
//        System.out.println(expansions);
//    }
//
//    public static void main(String[] args) throws Exception {
//        String inpath = args[0];
//        String outpath = args[1];
//
//        CollectionReader reader = CollectionReaderFactory.createReader(CommonFilenameCR.class,
//                CommonFilenameCR.SYSTEM_DATA_DIRS, new String[]{inpath});
//
//        AnalysisEngine[] engines = new AnalysisEngine[3];
//
//        engines[0] = AnalysisEngineFactory.createEngine(CasAdderAE.class,
//                    CasAdderAE.DATA_DIR, inpath,
//                    CasAdderAE.READ_FROM_VIEW, "SystemView",
//                    CasAdderAE.COPY_INTO_VIEW, "SystemView"
//            );
//
//        engines[1] = AnalysisEngineFactory.createEngine(RefStandardProcessor.class);
//
//        engines[2] = AnalysisEngineFactory.createEngine(XmiWriterAE.class,
//                XmiWriterAE.CONFIG_OUTPUT_DIR, outpath);
//
//        SimplePipeline.runPipeline(reader, engines);
//    }
//
//}