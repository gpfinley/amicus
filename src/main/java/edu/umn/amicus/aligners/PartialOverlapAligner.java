package edu.umn.amicus.aligners;

import edu.umn.amicus.Counter;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.*;

/**
 * Aligner that allows for partial overlaps.
 * Will optimize the alignments if the situation gets complicated.
 * Every annotation will be represented once.
 * Every alignment can only contain annotations if ALL of them line up at at least one index.
 *
 * Created by gpfinley on 3/1/17.
 */
public class PartialOverlapAligner implements Aligner {

    // set at the beginning: the input for each annotation
    private Map<Annotation, Integer> sourceInput;
    // all possible sets that each annotation could be a part of
    private Map<Annotation, Set<Set<Annotation>>> setMemberships;

    // this variable will be changed and acted upon by a recursive method
    private List<List<Set<Annotation>>> allCombos;

    /**
     * Generate alignments of annotations that fully overlap shorter annotations.
     * Optimize to leave as few character span units unaligned with anything as possible.
     * The algorithm:
     * // todo: describe!!!
     *
     * @param allAnnotations
     * @return
     */
    public Iterator<List<Annotation>> alignAndIterate(List<List<Annotation>> allAnnotations) {
        sourceInput = new HashMap<>();
        setMemberships = new HashMap<>();
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

        // determine all possible set memberships for every annotation
        for (Set<Annotation> annotationsThisIndex : annotationsAtIndex) {
            // if a single input has multiple annotations at this index, split into multiple sets to try
            // todo: test that this function actually works
            Set<Set<Annotation>> allPossibleSetsThisIndex = getOnePerInputSets(annotationsThisIndex);
            for (Set<Annotation> thisSet : allPossibleSetsThisIndex) {
                for (Annotation annotation : thisSet) {
                    setMemberships.get(annotation).add(thisSet);
                }
            }
        }

        // build all the "sprawls," which can be optimized independently
        // (much faster than optimizing every possible combo)
        Set<Annotation> annotationsAccountedFor = new HashSet<>(setMemberships.keySet());
        Set<Set<Annotation>> sprawls = new HashSet<>();
        while(annotationsAccountedFor.size() > 0) {
            Annotation annot = annotationsAccountedFor.iterator().next();
            Set<Annotation> thisSprawl = getSprawlOf(annot, null);
            annotationsAccountedFor.removeAll(thisSprawl);
            sprawls.add(thisSprawl);
        }

        // for each sprawl, find the most optimal combo of alignments for that sprawl and add to the big list
        List<List<Annotation>> allAlignments = new ArrayList<>();
        for (Set<Annotation> sprawl : sprawls) {
            allCombos = new ArrayList<>();
            addAllCombos(new LinkedHashSet<>(sprawl), null);
            List<Set<Annotation>> bestCombo = null;
            int bestScore = Integer.MAX_VALUE;
            for (List<Set<Annotation>> combo : allCombos) {
                int score = 0;
                for (Set<Annotation> annots : combo) {
                    Counter<Integer> indices = new Counter<>();
                    for (Annotation annot : annots) {
                        for (int i = annot.getBegin(); i < annot.getEnd(); i++) {
                            indices.increment(i);
                        }
                    }
                    for (int count : indices.values()) {
                        if (count == 1) score++;
                    }
                }
                if (score < bestScore) {
                    bestScore = score;
                    bestCombo = combo;
                }
            }
            for (Set<Annotation> set : bestCombo) {
                List<Annotation> byInputList = new ArrayList<>();
                for (List l : allAnnotations) {
                    byInputList.add(null);
                }
                for (Annotation annotation : set) {
                    byInputList.set(sourceInput.get(annotation), annotation);
                }
                allAlignments.add(byInputList);
            }
        }
        return allAlignments.iterator();
    }

    /**
     * Recursive algorithm to find all annotations that are part of the same sprawl as the one passed.
     * "Sprawl" = the set of all annotations that overlap any others in the set, either directly or vicariously.
     * @param annotation the starting point
     * @param currentSprawl the sprawl currently being built; can be null
     * @return a set of every annotation in the sprawl
     */
    private Set<Annotation> getSprawlOf(Annotation annotation, Set<Annotation> currentSprawl) {
        if (currentSprawl == null) currentSprawl = new HashSet<>();
        currentSprawl.add(annotation);
        for (Set<Annotation> thisSet : setMemberships.get(annotation)) {
            for (Annotation other : thisSet) {
                if (!currentSprawl.contains(other)) {
                    currentSprawl.addAll(getSprawlOf(other, currentSprawl));
                }
            }
        }
        return currentSprawl;
    }

    // find all possible combinations of aligned sets given annotationsLeft
    private void addAllCombos(LinkedHashSet<Annotation> annotationsLeft, List<Set<Annotation>> builtThusFar) {
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
                addAllCombos(newAnnotationsLeft, newBuiltThusFar);
            }
        }
    }

    // get all possible sets at this index--with a maximum of one annotation per input (if there are any overlapping)
    private Set<Set<Annotation>> getOnePerInputSets(Set<Annotation> annotations) {
        Set<Set<Annotation>> theseSets = new HashSet<>();
        Set<Integer> inputsUsed = new HashSet<>();
        boolean addThisSetAsIs = true;
        for (Annotation annotation : annotations) {
            Integer source = sourceInput.get(annotation);
            if (inputsUsed.contains(source)) {
                Set<Annotation> newSet = new HashSet<>(annotations);
                newSet.remove(annotation);
                theseSets.addAll(getOnePerInputSets(newSet));
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
