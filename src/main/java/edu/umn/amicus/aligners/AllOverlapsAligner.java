package edu.umn.amicus.aligners;

import org.apache.uima.jcas.tcas.Annotation;

import java.util.*;

/**
 * Finds all unique overlapping tuples of annotations.
 * Single annotations may be represented in several tuples, depending on how misaligned the annotations really are.
 * Created by gpfinley on 12/20/16.
 * // todo: unit test
 */
public class AllOverlapsAligner extends AnnotationAligner {

    /**
     * @param allAnnotations
     * @return
     */
    @Override
    public Iterator<List<Annotation>> alignAndIterate(List<List<Annotation>> allAnnotations) {

        List<List<Annotation>> annotationsAtIndex = new ArrayList<>();

        // get a list of annotations at each document character index
        for (List<Annotation> annotations : allAnnotations) {
            for (Annotation annotation : annotations) {
                while (annotationsAtIndex.size() < annotation.getEnd()) {
                    annotationsAtIndex.add(new ArrayList<Annotation>());
                }
                for (int i = annotation.getBegin(); i < annotation.getEnd(); i++) {
                    annotationsAtIndex.get(i).add(annotation);
                }
            }
        }

        // convert into a set to remove duplicate overlapping annotation lists
        return new LinkedHashSet<>(annotationsAtIndex).iterator();
    }
}
