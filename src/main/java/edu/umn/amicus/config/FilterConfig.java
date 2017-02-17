package edu.umn.amicus.config;

import edu.umn.amicus.aligners.AnnotationAligner;

/**
 * Created by greg on 2/16/17.
 */
public class FilterConfig extends PipelineComponentConfig {

    public AnnotationInputConfig filterThis;

    public AnnotationInputConfig basedOn;
    public AnnotationAligner aligner;

    public AnnotationOutputConfig saveTo;

    // probably a String; maybe a list!
    public Object value;

    public void verify() {
        //todo

    }

}
