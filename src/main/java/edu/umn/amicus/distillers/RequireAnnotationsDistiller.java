package edu.umn.amicus.distillers;

import edu.umn.amicus.AlignedTuple;
import edu.umn.amicus.PreAnnotation;

/**
 * Distiller that will take the Annotation from the first system--
 * as long as all other systems have an overlapping annotation present.
 *
 * Created by gpfinley on 10/20/16.
 */
public class RequireAnnotationsDistiller implements Distiller<Object> {

    /**
     *
     * @param annotations
     */
    @Override
    public PreAnnotation<Object> distill(AlignedTuple<PreAnnotation<Object>> annotations) {
        for (PreAnnotation pa : annotations) {
            if (pa == null) return null;
        }
        return annotations.size() == 0 ? null : annotations.get(0);
    }

}
