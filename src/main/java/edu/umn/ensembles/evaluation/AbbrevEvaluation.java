//package edu.umn.ensembles.evaluation;
//
//import edu.umn.biomedicus.type.TokenAnnotation;
//import edu.umn.ensembles.config.MergerConfiguration;
//import edu.umn.ensembles.processing.Counter;
//import edu.umn.ensembles.processing.EquivalentAnswerMapper;
//import edu.umn.ensembles.processing.Mapper;
//import org.apache.uima.jcas.tcas.Annotation;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
// todo: revise this to work with new transformers etc.
///**
// * Created by gpfinley on 10/12/16.
// */
//public class AbbrevEvaluation extends Evaluation {
//
//    private int[] falsePositives;
//
//    private final List<String> gold;
//    private final List<List<String>> hypotheses;
//
//    private final Mapper<String, String> mapper;
//
//    private final Map<String, Counter<String>> abbrsAndExpansionsLower;
//    private final Map<String, Counter<String>> abbrsAndExpansions;
//
//    // map each abbreviation to a list of all of its examples (integer offsets of the gold/hypotheses lists)
////    private final Map<String, List<Integer>> abbrsAndExamples;
//
//    private final List<String> abbrs;
//
//    public AbbrevEvaluation(MergerConfiguration configuration) {
//        super(configuration);
//        gold = new ArrayList<>();
//        hypotheses = new ArrayList<>();
//        mapper = EquivalentAnswerMapper.getInstance();
//        abbrsAndExpansionsLower = new HashMap<>();
//        abbrsAndExpansions = new HashMap<>();
////        abbrsAndExamples = new HashMap<>();
//        abbrs = new ArrayList<>();
//    }
//
//    @Override
//    public void addTuple(List<Annotation> annotations) {
//        List<String> hypothesisStrings = new ArrayList<>();
//        if (annotations.size() == 0 || annotations.get(0) == null) return;
//        TokenAnnotation token = (TokenAnnotation) annotations.get(0);
//        if (token.getIsAcronymAbbrev()) {
//            String longform = (String) configuration.getTransformer(0).transform(annotations.get(0));
//            String abbr = annotations.get(0).getCoveredText();
//            if (!excludeThisForm(longform) && !excludeThisAbbr(abbr)) {
//                String mappedLongform = mapper.map(longform.trim());
//                for (int i = 1; i < annotations.size(); i++) {
//                    if (annotations.get(i) != null) {
//                        String hypLong = (String) configuration.getTransformer(i).transform(annotations.get(i));
//                        hypothesisStrings.add(mapper.map(hypLong.trim()));
//                    } else {
//                        hypothesisStrings.add("");
//                    }
//                }
//                gold.add(mappedLongform);
//                hypotheses.add(hypothesisStrings);
//
//                abbrs.add(abbr);
////                abbrsAndExamples.putIfAbsent(abbr.toLowerCase(), new ArrayList<>());
////                abbrsAndExamples.get(abbr.toLowerCase()).add(gold.size() - 1);
//
//                abbrsAndExpansionsLower.putIfAbsent(abbr.toLowerCase(), new Counter<>());
//                abbrsAndExpansionsLower.get(abbr.toLowerCase()).increment(mappedLongform.toLowerCase());
//
//                abbrsAndExpansions.putIfAbsent(abbr, new Counter<>());
//                abbrsAndExpansions.get(abbr).increment(mappedLongform.toLowerCase());
//            }
//        } else {
//            // todo: make this code more elegant/efficient
//            for (int i = 1; i < annotations.size(); i++) {
//                if (falsePositives == null) {
//                    falsePositives = new int[annotations.size() - 1];
//                }
//                falsePositives[i-1]++;
//            }
//        }
//    }
//
//    private static boolean excludeThisForm(String form) {
//        return form == null
//                || form.length() == 0
//                || form.equalsIgnoreCase("unk")
//                || form.equalsIgnoreCase("(unk)")
//                || form.endsWith("(ign)")
//                || form.equals("ign");
//    }
//
//    private static boolean excludeThisAbbr(String abbr) {
//        return abbr == null || !abbr.matches(".*\\w+.*");
//    }
//
//    private static boolean isBlank(String form) {
//        return form == null || form.matches("\\s*");
//    }
//
//    @Override
//    public void runEvaluation() {
//        StringBuilder builder = new StringBuilder();
//
//        Counter<String> numEachAbbr = new Counter(abbrs);
//
//        int majority = 0;
//        int total = 0;
//        for (Counter counter : abbrsAndExpansionsLower.values()) {
//            majority += max (counter.values());
//            for (Integer v : (List<Integer>) counter.values()) total += v;
//        }
//        builder.append(abbrsAndExpansionsLower);
//        builder.append("\n");
//        builder.append("majority sense correct: ");
//        builder.append(majority);
//        builder.append("\n");
//        builder.append("majority sense accuracy: ");
//        builder.append((double)majority / total);
//        builder.append("\n");
//
//        builder.append("ALL ABBREVIATIONS AND COUNTS:\n");
//        abbrsAndExpansions.entrySet().forEach(x -> builder.append(x.getKey() + "\t" + x.getValue().total() + "\n"));
//        builder.append("\n");
//
//        for (int system = 0; system < hypotheses.get(0).size(); system++) {
//
//            builder.append("SYSTEM NAME: ");
//            builder.append(configuration.getOriginSystems().get(system + 1));
//            builder.append("\n");
//
//            Counter<String> coveredByAbbr = new Counter<>();
//            Counter<String> correctByAbbr = new Counter<>();
//
//            Counter<String> incorrectByAbbr = new Counter<>();
//            Counter<String> undetectedByAbbr = new Counter<>();
//
//
//            // todo: this doesn't work quite right (troubleshoot)
////            builder.append(configuration.indexOf(configuration.getAnnotationClasses().get(system-1)))
//
//            final Map<String, Integer> misses = new HashMap<>();
//
//            int correct = 0;
//            int missed = 0;
//            for (int i = 0; i < gold.size(); i++) {
//                String abbr = abbrs.get(i);
//                String hyp = hypotheses.get(i).get(system);
//                String gol = gold.get(i);
//                if (isBlank(hyp)) {
//                    missed++;
//                    undetectedByAbbr.increment(abbr);
//                } else {
//                    coveredByAbbr.increment(abbr);
//                    if (hyp.equalsIgnoreCase(gol)) {
//                        correctByAbbr.increment(abbr);
//                        correct++;
//                    } else {
//                        incorrectByAbbr.increment(abbr);
//                        String key = gol + "\n" + hyp;
//                        misses.put(key, misses.getOrDefault(key, 0) + 1);
//                    }
//                }
//            }
//
//            Map<String, Integer> sortedMisses = new TreeMap<>((String a, String b) -> {
//                int c = misses.get(a).compareTo(misses.get(b));
//                return c == 0 ? a.compareTo(b) : c;
//            });
//            sortedMisses.putAll(misses);
//
//            for (Map.Entry<String, Integer> miss : sortedMisses.entrySet()) {
//                builder.append(miss.getKey());
//                builder.append("\n\t");
//                builder.append(miss.getValue());
//                builder.append("\n");
//            }
//
//            builder.append("incorrect:\n");
//            builder.append(incorrectByAbbr.createSortedMap());
//            builder.append("\n");
//            builder.append("undetected:\n");
//            builder.append(undetectedByAbbr.createSortedMap());
//            builder.append("\n");
//
//            for (String abbr : coveredByAbbr.keySet()) {
//                builder.append(abbr);
//                builder.append("\t");
//                builder.append(coveredByAbbr.get(abbr) / ((double) numEachAbbr.get(abbr)));
//                builder.append(" ");
//                builder.append(correctByAbbr.get(abbr) / ((double) numEachAbbr.get(abbr)));
//                builder.append("\n");
//            }
//
//            builder.append("# of abbrs: ");
//            builder.append(abbrsAndExpansionsLower.size());
//            builder.append("\n");
//
//            List<Integer> sizes = abbrsAndExpansionsLower.values().stream().map(Counter::size).collect(Collectors.toList());
//            builder.append(sizes);
//            builder.append("\n");
//
//            Counter<Integer> sizeCounter = new Counter(sizes);
//            builder.append(sizeCounter);
//            builder.append("\n");
//
//
//            builder.append("correct: ");
//            builder.append(correct);
//            builder.append("\n");
//
//            builder.append("unannotated: ");
//            builder.append(missed);
//            builder.append("\n");
//
//            builder.append("total: ");
//            builder.append(gold.size());
//            builder.append("\n");
//
//            builder.append("false positives (not reliable for abbrs): ");
//            builder.append(falsePositives[system]);
//            builder.append("\n");
//
//            builder.append("coverage: ");
//            builder.append(1. - ((double) missed) / gold.size());
//            builder.append("\n");
//
//            builder.append("accuracy: ");
//            builder.append(correct / (gold.size() - missed - 0.));
//            builder.append("\n");
//
//            builder.append("overall:  ");
//            builder.append(((double) correct) / gold.size());
//            builder.append("\n");
//
//        }
//
//        // todo: set this up to print to file?
//        System.out.println(builder.toString());
//    }
//
//    private int max(Iterable<Integer> ints) {
//        int max = Integer.MIN_VALUE;
//        for (int integer : ints) {
//            if (integer > max) {
//                max = integer;
//            }
//        }
//        return max;
//    }
//
//}
