package edu.umn.amicus.aligners;

import org.apache.uima.jcas.tcas.Annotation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by gpfinley on 10/21/16.
 */
public class FirstSystemOverlapAligner extends AnnotationAligner {

    /**
     * Each iteration will provide one annotation from the first system,
     * plus all annotations from all systems that overlap.
     * Each annotation from the first system will be represented exactly once.
     * Some annotations from systems other than the first may be ignored.
     * @param allAnnotations
     * @return
     */
    @Override
    public Iterator<List<Annotation>> alignAndIterate(List<List<Annotation>> allAnnotations) {

        // for every annotation in first system, find all annotations in all other systems that at least partially overlap
        // result will be asymmetric! But that's okay because generally the first system listed is the most trusted

        List<List<Annotation>> listToIterate = new ArrayList<>();

        for (Annotation firstSysAnnotation : allAnnotations.get(0)) {
            List<Annotation> thisList = new ArrayList<>();
            listToIterate.add(thisList);
            thisList.add(firstSysAnnotation);
            for (int i=1; i<allAnnotations.size(); i++) {
                for (Annotation annotation : allAnnotations.get(i)) {
                    if (annotation.getBegin() <= firstSysAnnotation.getEnd()
                            && annotation.getEnd() >= firstSysAnnotation.getEnd()) {
                        thisList.add(annotation);
                    }
                }
            }
        }

        return listToIterate.iterator();
    }

}
