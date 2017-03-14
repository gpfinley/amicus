package edu.umn.amicus.aligners;

import edu.umn.amicus.AlignedTuple;
import edu.umn.amicus.Counter;
import org.apache.commons.lang.math.IntRange;
import org.apache.commons.lang.mutable.MutableDouble;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.*;
import java.util.logging.Logger;

/**
 * Aligner that allows for partial overlaps.
 * Will optimize the alignments if the situation gets complicated.
 * Every annotation will be represented once.
 * Every alignment can only contain annotations if ALL of them line up at at least one index.
 *
 * Created by gpfinley on 3/1/17.
 */
public class PartialOverlapAligner implements Aligner {

    private static final Logger LOGGER = Logger.getLogger(PartialOverlapAligner.class.getName());

    // set at the beginning: the input for each annotation
//    private Map<Annotation, Integer> sourceInput;
    // all possible sets that each annotation could be a part of
//    private Map<Annotation, Set<Set<Annotation>>> setMemberships;

    // this variable will be changed and acted upon by a recursive method
//    private List<List<Set<Annotation>>> allCombos;

    /**
     * Generate alignments of annotations that fully overlap shorter annotations.
     * Optimize to leave as few character span units unaligned with anything as possible.
     * The algorithm:
     * // todo: describe!!!
     *
     * @param allAnnotations
     * @return
     */
    public Iterator<AlignedTuple<Annotation>> alignAndIterate(List<List<Annotation>> allAnnotations) {
        // todo: remove
        long time = System.currentTimeMillis();

        allAnnotations = removeTotalOverlapsFromSameSystem(allAnnotations);

        Map<Annotation, Integer> sourceInput = new HashMap<>();
        Map<Annotation, Set<Set<Annotation>>> setMemberships = new HashMap<>();

        List<Set<Annotation>> annotationsAtIndex = new ArrayList<>();
        for (int sysIndex = 0; sysIndex < allAnnotations.size(); sysIndex++) {
            for (Annotation annotation : allAnnotations.get(sysIndex)) {
                // not adding all set memberships yet--just singletons
                setMemberships.put(annotation, new HashSet<Set<Annotation>>());
                setMemberships.get(annotation).add(Collections.singleton(annotation));
                sourceInput.put(annotation, sysIndex);
                while (annotationsAtIndex.size() < annotation.getEnd()) {
                    annotationsAtIndex.add(new HashSet<Annotation>());
                }
                for (int i = annotation.getBegin(); i < annotation.getEnd(); i++) {
                    annotationsAtIndex.get(i).add(annotation);
                }
            }
        }

        Set<Set<Annotation>> allSetsOverAllIndices = new HashSet<>(annotationsAtIndex);
        Set<Set<Annotation>> allLegalSets = new HashSet<>();
        for (Set<Annotation> annotationSet : allSetsOverAllIndices) {
            // if a single input has multiple annotations at this index, split into multiple sets to try
            allLegalSets.addAll(getOnePerInputSets(annotationSet, sourceInput));
        }
        for (Set<Annotation> thisSet : allSetsOverAllIndices) {
            for (Annotation a : thisSet) {
                setMemberships.get(a).add(thisSet);
            }
        }

        // build all the "sprawls," which can be optimized independently
        // (much faster than optimizing every possible combo, assuming non-pathologically overlapping annotations)
        Set<Annotation> annotationsAccountedFor = new HashSet<>(setMemberships.keySet());
        Set<Set<Annotation>> sprawls = new HashSet<>();
        while(annotationsAccountedFor.size() > 0) {
            Annotation annot = annotationsAccountedFor.iterator().next();
            Set<Annotation> thisSprawl = getSprawlOf(annot, null, setMemberships);
            annotationsAccountedFor.removeAll(thisSprawl);
            sprawls.add(thisSprawl);
        }

        // for each sprawl, find the most optimal combo of alignments for that sprawl and add to the big list
        List<AlignedTuple<Annotation>> allAlignments = new ArrayList<>();
        for (Set<Annotation> sprawl : sprawls) {
//            List<List<Set<Annotation>>> allCombos = new ArrayList<>();
//            addAllCombos(new LinkedHashSet<>(sprawl), null);
//            List<Set<Annotation>> bestCombo = allCombos.get(0);
//            if (allCombos.size() > 1) {
//                double bestScore = -Double.MAX_VALUE;
//                for (List<Set<Annotation>> combo : allCombos) {
//                    double score = 0;
//                    for (Set<Annotation> annots : combo) {
//                        score += calculateScore(annots);
//                    }
//                    if (score > bestScore) {
//                        bestScore = score;
//                        bestCombo = combo;
//                    }
//                }
//            }
            List<Set<Annotation>> bestCombo = getBestCombo(new LinkedHashSet<>(sprawl), null, setMemberships, null);
            for (Set<Annotation> set : bestCombo) {
                AlignedTuple<Annotation> byInputTuple = new AlignedTuple<>(allAnnotations.size());
                for (Annotation annotation : set) {
                    byInputTuple.set(sourceInput.get(annotation), annotation);
                }
                allAlignments.add(byInputTuple);
            }
        }

        LOGGER.info("PartialOverlapAligner took " + ((System.currentTimeMillis() - time) / 1000.) + "s");
        return allAlignments.iterator();
    }

    private static List<List<Annotation>> removeTotalOverlapsFromSameSystem(List<List<Annotation>> allAnnotations) {
        List<List<Annotation>> reduced = new ArrayList<>();
        for (List<Annotation> annotations : allAnnotations) {
            List<Annotation> newList = new ArrayList<>();
            Set<BeginEnd> beginEnds = new HashSet<>();
            for (Annotation annotation : annotations) {
                if (beginEnds.add(new BeginEnd(annotation.getBegin(), annotation.getEnd()))) {
                    newList.add(annotation);
                }
            }
            reduced.add(newList);
        }
        return reduced;
    }

    private static double calculateComboScore(List<Set<Annotation>> combo) {
        double score = 0;
        for (Set<Annotation> annots : combo) {
            score += calculateScore(annots);
        }
        return score;
    }

    private static double calculateScore(Collection<Annotation> annotations) {
        double score = 0;
        Counter<Integer> indexCounter = new Counter<>();
        for (Annotation annot : annotations) {
            for (int i = annot.getBegin(); i < annot.getEnd(); i++) {
                indexCounter.increment(i);
            }
        }
        for (Annotation annot : annotations) {
            double thisScore = 0;
            for (int i = annot.getBegin(); i < annot.getEnd(); i++) {
                thisScore += (indexCounter.get(i) - 1);
            }
            score += thisScore / (annot.getEnd() - annot.getBegin());
        }
        return score;
    }

    @Deprecated
    private static double calculateDumbScore(Collection<Annotation> annotations) {
        double score = 0;
        Counter<Integer> indices = new Counter<>();
        for (Annotation annot : annotations) {
            for (int i = annot.getBegin(); i < annot.getEnd(); i++) {
                indices.increment(i);
            }
        }
        for (int count : indices.values()) {
            if (count == 1) score += 1;
        }
        return -score;
    }

    /**
     * Recursive algorithm to find all annotations that are part of the same sprawl as the one passed.
     * "Sprawl" = the set of all annotations that overlap any others in the set, either directly or vicariously.
     * @param annotation the starting point
     * @param currentSprawl the sprawl currently being built; can be null
     * @return a set of every annotation in the sprawl
     */
    private static Set<Annotation> getSprawlOf(Annotation annotation, Set<Annotation> currentSprawl, Map<Annotation, Set<Set<Annotation>>> setMemberships) {
        if (currentSprawl == null) currentSprawl = new HashSet<>();
        currentSprawl.add(annotation);
        for (Set<Annotation> thisSet : setMemberships.get(annotation)) {
            for (Annotation other : thisSet) {
                if (!currentSprawl.contains(other)) {
                    currentSprawl.addAll(getSprawlOf(other, currentSprawl, setMemberships));
                }
            }
        }
        return currentSprawl;
    }

    // find all possible combinations of aligned sets given annotationsLeft
    private static void addAllCombos(List<List<Set<Annotation>>> allCombos,
                                     LinkedHashSet<Annotation> annotationsLeft,
                                     List<Set<Annotation>> builtThusFar,
                                     Map<Annotation, Set<Set<Annotation>>> setMemberships) {
        if (builtThusFar == null) {
            builtThusFar = new ArrayList<>();
        }
        if (annotationsLeft.size() == 0) {
            allCombos.add(builtThusFar);
            return;
        }
        Annotation annotation = annotationsLeft.iterator().next();
        for (Set<Annotation> trySet : setMemberships.get(annotation)) {
            tryThisSet:
            {
                for (Annotation otherAnnotation : trySet) {
                    if (!annotationsLeft.contains(otherAnnotation)) {
                        break tryThisSet;
                    }
                }
                List<Set<Annotation>> newBuiltThusFar = new ArrayList<>(builtThusFar);
                newBuiltThusFar.add(trySet);
                LinkedHashSet<Annotation> newAnnotationsLeft = new LinkedHashSet<>(annotationsLeft);
                newAnnotationsLeft.removeAll(trySet);
                addAllCombos(allCombos, newAnnotationsLeft, newBuiltThusFar, setMemberships);
            }
        }
    }

    private static List<Set<Annotation>> getBestCombo(LinkedHashSet<Annotation> annotationsLeft,
                                     List<Set<Annotation>> builtThusFar,
                                     Map<Annotation, Set<Set<Annotation>>> setMemberships,
                                     MutableDouble bestScore) {
        if (bestScore == null) {
            bestScore = new MutableDouble(-1);
        }
        if (builtThusFar == null) {
            builtThusFar = new ArrayList<>();
        }
        if (annotationsLeft.size() == 0) {
            double score = calculateComboScore(builtThusFar);
            if (score > bestScore.doubleValue()) {
                bestScore.setValue(score);
                return builtThusFar;
            }
            return null;
        }
        List<Set<Annotation>> bestCombo = null;
        Annotation annotation = annotationsLeft.iterator().next();
        for (Set<Annotation> trySet : setMemberships.get(annotation)) {
            tryThisSet:
            {
                for (Annotation otherAnnotation : trySet) {
                    if (!annotationsLeft.contains(otherAnnotation)) {
                        break tryThisSet;
                    }
                }
                List<Set<Annotation>> newBuiltThusFar = new ArrayList<>(builtThusFar);
                newBuiltThusFar.add(trySet);
                LinkedHashSet<Annotation> newAnnotationsLeft = new LinkedHashSet<>(annotationsLeft);
                newAnnotationsLeft.removeAll(trySet);
                List<Set<Annotation>> thisCombo = getBestCombo(newAnnotationsLeft, newBuiltThusFar, setMemberships, bestScore);
                if (thisCombo != null) {
                    bestCombo = thisCombo;
                }
            }
        }
        return bestCombo;
    }

//    static int recursed;
    // get all possible sets at this index--with a maximum of one annotation per input (if there are any overlapping)
    private static Set<Set<Annotation>> getOnePerInputSets(Set<Annotation> annotations, Map<Annotation, Integer> sourceInput) {
        Set<Set<Annotation>> theseSets = new HashSet<>();
        Set<Integer> inputsUsed = new HashSet<>();
        boolean addThisSetAsIs = true;
        for (Annotation annotation : annotations) {
            Integer source = sourceInput.get(annotation);
            if (inputsUsed.contains(source)) {
                Set<Annotation> newSet = new HashSet<>(annotations);
                newSet.remove(annotation);
//                recursed ++;
//                System.out.println(recursed);
//                System.out.println(annotation);
//                System.out.println(annotations);
                theseSets.addAll(getOnePerInputSets(newSet, sourceInput));
                addThisSetAsIs = false;
            } else {
                inputsUsed.add(source);
            }
        }
        if (addThisSetAsIs) {
            theseSets.add(annotations);
        }
        return theseSets;
    }

}
