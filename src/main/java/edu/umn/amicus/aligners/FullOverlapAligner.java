//package edu.umn.amicus.aligners;
//
//import edu.umn.amicus.Counter;
//import org.apache.uima.jcas.tcas.Annotation;
//
//import java.util.*;
//
///**
// * Created by gpfinley on 3/1/17.
// */
//@Deprecated
//public class FullOverlapAligner implements Aligner {
//
//    private static Map<Annotation, Integer> sourceInput;
//
//    /**
//     * Generate alignments of annotations that fully overlap shorter annotations.
//     * The algorithm:
//     *      build an inventory of all annotations from all inputs at each character index;
//     *      for every annotation, find all annotations also present at all of its character indices;
//     *      pare these down to just those annotations that are within the bounds of our annotation;
//     *      point each of these annotations to the tuple of all annotations that line up with our annotation;
//     *      if any previously pointed to other tuples, delete those tuples and redirect all annotations in those
//     *           tuples to this new one;
//     *      collapse each of these tuples into a list of maximum one annotation per input (prefer longer annotations).
//     *
//     * @param allAnnotations
//     * @return
//     */
//    public Iterator<List<Annotation>> alignAndIterate(List<List<Annotation>> allAnnotations) {
//        sourceInput = new HashMap<>();
//        Map<Annotation, Set<Set<Annotation>>> eachAnnotationsSets = new HashMap<>();
//        List<List<Annotation>> annotationsAtIndex = new ArrayList<>();
//        for (int sysIndex = 0; sysIndex < allAnnotations.size(); sysIndex++) {
//            for (Annotation annotation : allAnnotations.get(sysIndex)) {
//                // pre-load some hashmaps
//                sourceInput.put(annotation, sysIndex);
//                eachAnnotationsSets.put(annotation, new HashSet<Set<Annotation>>());
//                while (annotationsAtIndex.size() < annotation.getEnd()) {
//                    annotationsAtIndex.add(new ArrayList<Annotation>());
//                }
//                for (int i = annotation.getBegin(); i < annotation.getEnd(); i++) {
//                    annotationsAtIndex.get(i).add(annotation);
//                }
//            }
//        }
//
//        Set<Set<Annotation>> allSets = new HashSet<>();
//        for (List<Annotation> annotations : allAnnotations) {
//            for (Annotation annotation : annotations) {
//                // skip if we've already put this annotation into a set
//                if (eachAnnotationsSets.get(annotation).size() != 0) continue;
//
////                Set<Set<Annotation>> mySets = eachAnnotationsSets.get(annotation);
////                if (mySets == null) {
////                    mySets = new HashSet<>();
////                    eachAnnotationsSets.put(annotation, mySets);
////                }
//
//                Set<Annotation> overlapping = new HashSet<>();
//                for (int i = annotation.getBegin(); i < annotation.getEnd(); i++) {
//                    overlapping.addAll(annotationsAtIndex.get(i));
//                }
//                for (Annotation otherAnnotation : overlapping) {
//                    if (annotation.getBegin() <= otherAnnotation.getBegin() && annotation.getEnd() >= otherAnnotation.getEnd()) {
//
//                        Set<Set<Annotation>> otherAnnotationsSets = eachAnnotationsSets.get(otherAnnotation);
//                        if (otherAnnotationsSets != null) {
//                            allSets.remove(otherAnnotationsSets);
//                        }
//
//                    }
//                }
//            }
//        }
//
//
//        return null;
//
////        Map<Annotation, Set<Annotation>> subsumes = new HashMap<>();
////        Map<Annotation, Set<Annotation>> subsumedBy = new HashMap<>();
////
////        for (List<Annotation> annotations : allAnnotations) {
////            for (Annotation annotation : annotations) {
////                Set<Annotation> overlapping = new HashSet<>();
////                for (int i = annotation.getBegin(); i < annotation.getEnd(); i++) {
////                    overlapping.addAll(annotationsAtIndex.get(i));
////                }
////                for (Annotation otherAnnotation : overlapping) {
////                    if (annotation.getBegin() <= otherAnnotation.getBegin() && annotation.getEnd() >= otherAnnotation.getEnd()) {
////                        Set<Annotation> thisSubsumes = subsumes.get(annotation);
////                        if (thisSubsumes == null) {
////                            thisSubsumes = new HashSet<>();
////                            subsumes.put(annotation, thisSubsumes);
////                        }
////                        thisSubsumes.add(otherAnnotation);
////
////                        Set<Annotation> thisIsSubsumedBy = subsumedBy.get(otherAnnotation);
////                        if (thisIsSubsumedBy == null) {
////                            thisIsSubsumedBy = new HashSet<>();
////                            subsumedBy.put(otherAnnotation, thisIsSubsumedBy);
////                        }
////                        thisIsSubsumedBy.add(annotation);
////                    }// else if (annotation.getBegin() >= otherAnnotation.getBegin() && annotation.getEnd() <= otherAnnotation.getEnd()) {
////                }
////            }
////        }
////
//////        System.out.println(subsumes);
//////        System.out.println(subsumedBy);
////
////        Set<Set<Annotation>> allSubsumptions = new HashSet<>();
////        for (Annotation annotation : subsumes.keySet()) {
////            Set<Annotation> thisSubsumption = new HashSet<>(subsumes.get(annotation));
////            thisSubsumption.addAll(subsumedBy.get(annotation));
////            allSubsumptions.add(thisSubsumption);
////        }
////
////        System.out.println(allSubsumptions);
////
////        for (Set<Annotation> sub : allSubsumptions) {
////            System.out.println(getOnePerInputSets(sub));
////        }
////
////        return null;
//    }
//
//    private Set<Set<Annotation>> getOnePerInputSets(Set<Annotation> annotations) {
//        Set<Set<Annotation>> theseSets = new HashSet<>();
//        Set<Integer> inputsUsed = new HashSet<>();
//        boolean addThisSetAsIs = true;
//        for (Annotation annotation : annotations) {
//            Integer source = sourceInput.get(annotation);
//            if (inputsUsed.contains(source)) {
//                Set<Annotation> newSet = new HashSet<>(annotations);
//                newSet.remove(annotation);
//                theseSets.addAll(getOnePerInputSets(newSet));
//                addThisSetAsIs = false;
//            } else {
//                inputsUsed.add(source);
//            }
//        }
//        if (addThisSetAsIs) {
//            theseSets.add(annotations);
//        }
//        return theseSets;
//    }
//
//
////
////
////
////        // point every annotation to the tuple that it's a part of
////        Map<Annotation, Set<AnnotFromInput>> annotationsAndTheirTuples = new HashMap<>();
////        // loop through every annotation that is not already spoken for as part of a tuple
////        for (int input = 0; input < allAnnotations.size(); input++) {
//////        for (List<Annotation> annotations : allAnnotations) {
////            for (Annotation annotation : allAnnotations.get(input)) {
////                if (annotationsAndTheirTuples.containsKey(annotation)) continue;
////
////                Set<AnnotFromInput> overlapping = new HashSet<>();
////                for (int i = annotation.getBegin(); i < annotation.getEnd(); i++) {
////                    overlapping.addAll(annotationsAtIndex.get(i));
////                }
////
////                System.out.println(annotation);
////                System.out.println(overlapping);
////                System.out.println(annotationsAndTheirTuples);
////                try {
////                    Thread.sleep(1000);
////                } catch(Exception e) {}
////
////                Set<AnnotFromInput> thisTuple = new HashSet<>();
////                thisTuple.add(new AnnotFromInput(annotation, input));
////                annotationsAndTheirTuples.put(annotation, thisTuple);
////
////                // for every partially overlapping annotation, put it in a tuple with this one
////                for (AnnotFromInput otherAnnot : overlapping) {
////                    if (otherAnnot.annotation.getBegin() >= annotation.getBegin()
////                            && otherAnnot.annotation.getEnd() <= annotation.getEnd()) {
////                        // if this annotation was already part of a tuple,
////                        //      move all annots from that tuple into this one and redirect pointers
////                        Set<AnnotFromInput> subTuple = annotationsAndTheirTuples.get(otherAnnot.annotation);
////                        if (subTuple != null) {
////                            subTuple = new HashSet<>(subTuple);
////                            for (AnnotFromInput annotToRedirect : subTuple) {
////                                annotationsAndTheirTuples.put(annotToRedirect.annotation, thisTuple);
////                                thisTuple.add(annotToRedirect);
////                            }
////                        } else {
////                            thisTuple.add(otherAnnot);
////                            System.out.println("overlaps with " + otherAnnot);
////                            annotationsAndTheirTuples.put(otherAnnot.annotation, thisTuple);
////                        }
////                    }
////                }
////            }
////        }
////
////        Set<List<Annotation>> toIterateOver = new HashSet<>();
////        for (Set<AnnotFromInput> alignedAnnotations : annotationsAndTheirTuples.values()) {
////            Annotation[] forEachSystem = new Annotation[allAnnotations.size()];
////            for (AnnotFromInput annotFromInput : alignedAnnotations) {
////                Annotation alreadySet = forEachSystem[annotFromInput.input];
////                // put this annotation into the one-per-input list if nothing else is there yet
////                //      --or if bigger than what is there already
////                if (alreadySet == null || annotFromInput.annotation.getEnd() - annotFromInput.annotation.getEnd() >
////                                                          alreadySet.getEnd() - alreadySet.getBegin()) {
////                    forEachSystem[annotFromInput.input] = annotFromInput.annotation;
////                }
////            }
////            toIterateOver.add(Arrays.asList(forEachSystem));
////        }
////        return toIterateOver.iterator();
//
////    /**
////     * Simple struct class, only used here
////     */
////    private static class AnnotFromInput {
////        Annotation annotation;
////        int input;
////        AnnotFromInput(Annotation annotation, int input) {
////            this.annotation = annotation;
////            this.input = input;
////        }
////        @Override
////        public String toString() {
////            return String.format("<%s, %d>", annotation, input);
////        }
////
////        @Override
////        public boolean equals(Object o) {
////            if (this == o) return true;
////            if (o == null || getClass() != o.getClass()) return false;
////
////            AnnotFromInput that = (AnnotFromInput) o;
////
////            if (input != that.input) return false;
////            return !(annotation != null ? !annotation.equals(that.annotation) : that.annotation != null);
////
////        }
////
////        @Override
////        public int hashCode() {
////            int result = annotation != null ? annotation.hashCode() : 0;
////            result = 31 * result + input;
////            return result;
////        }
////    }
//
//}
