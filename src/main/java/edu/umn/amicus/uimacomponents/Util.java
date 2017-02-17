package edu.umn.amicus.uimacomponents;

import edu.umn.amicus.Amicus;
import edu.umn.amicus.DocumentID;
import edu.umn.amicus.AmicusException;
import edu.umn.amicus.aligners.AnnotationAligner;
import edu.umn.amicus.distillers.AnnotationDistiller;
import edu.umn.amicus.pullers.AnnotationPuller;
import edu.umn.amicus.pullers.MultiGetterPuller;
import edu.umn.amicus.pushers.AnnotationPusher;
import edu.umn.amicus.pushers.MultiSetterPusher;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Basic utility functions for use by UIMA components
 * // todo: doc for indiv functions
 *
 * Created by gpfinley on 10/25/16.
 */
public final class Util {

    /**
     * Get the name of a getter method for a given field.
     * Should operate under the exact same rules that UIMA uses when generating sources.
     * @param field the name of a field
     * @return the name of the field's getter method
     */
    public static String getGetterFor(String field) {
        return "get" + field.substring(0, 1).toUpperCase() + field.substring(1, field.length());
    }

    /**
     * Get the name of a setter method for a given field.
     * Should operate under the exact same rules that UIMA uses when generating sources.
     * @param field the name of a field
     * @return the name of the field's setter method
     */
    public static String getSetterFor(String field) {
        return "set" + field.substring(0, 1).toUpperCase() + field.substring(1, field.length());
    }

    public static String getDocumentID(CAS cas) {
        JCas jCas;
        JCas filenameView;
        try {
            jCas = cas.getJCas();
            filenameView = jCas.getView(Amicus.DOCID_VIEW);
        } catch (CASException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        JFSIndexRepository jfsIndexRepository = filenameView.getJFSIndexRepository();
        FSIterator<DocumentID> outPathIter = jfsIndexRepository.getAllIndexedFS(DocumentID.type);
        return outPathIter.next().getDocumentID();
    }

    public static void setDocumentID(CAS cas, String id) {
        JCas docIdView;
        try {
            docIdView = cas.getJCas().getView(Amicus.DOCID_VIEW);
        } catch (CASException e) {
            e.printStackTrace();
            throw new AmicusException(e);
        }
        DocumentID docID = new DocumentID(docIdView);
        docID.setDocumentID(id);
        docID.addToIndexes();
    }

//    public static AnnotationPuller instantiatePuller(String pullerClassName, String fieldName)
//            throws ReflectiveOperationException {
//        Class<? extends AnnotationPuller> pullerClass;
//        if (pullerClassName == null) {
//            if (fieldName == null) {
//                throw new AmicusException("Need to specify field name unless using " +
//                        "a custom AnnotationPuller implementation.");
//            }
//            if (fieldName.contains(MultiGetterPuller.DELIMITER)) {
//                pullerClass = Amicus.DEFAULT_MULTI_PULLER_CLASS;
//            } else {
//                pullerClass = Amicus.DEFAULT_PULLER_CLASS;
//            }
//        } else {
//            pullerClass = (Class<? extends AnnotationPuller>) Class.forName(pullerClassName);
//        }
//        return pullerClass.getConstructor(String.class).newInstance(fieldName);
//    }
//
//    public static AnnotationPusher instantiatePusher(String pusherClassName, String typeName, String fieldName)
//            throws ReflectiveOperationException {
//        Class <? extends AnnotationPusher> pusherClass;
//        if (pusherClassName == null) {
//            if (typeName == null || fieldName == null) {
//                throw new AmicusException("Need to provide output annnotation fields and types UNLESS using" +
//                        " a custom AnnotationPusher implementation that can ignore them.");
//            }
//            if (fieldName.contains(MultiSetterPusher.DELIMITER)) {
//                pusherClass = Amicus.DEFAULT_MULTI_PUSHER_CLASS;
//            } else {
//                pusherClass = Amicus.DEFAULT_PUSHER_CLASS;
//            }
//        } else {
//            pusherClass = (Class<? extends AnnotationPusher>) Class.forName(pusherClassName);
//        }
//        return pusherClass.getConstructor(String.class, String.class).newInstance(typeName, fieldName);
//    }
//
//    public static AnnotationAligner instantiateAligner(String alignerClassName) throws ReflectiveOperationException {
//        if (alignerClassName != null) {
//            return (AnnotationAligner) Class.forName(alignerClassName).newInstance();
//        }
//        return Amicus.DEFAULT_ALIGNER_CLASS.newInstance();
//    }
//
//    public static AnnotationDistiller instantiateDistiller(String distillerClassName) throws ReflectiveOperationException {
//        if (distillerClassName == null) {
//            return Amicus.DEFAULT_DISTILLER_CLASS.newInstance();
//        }
//        return (AnnotationDistiller) Class.forName(distillerClassName).newInstance();
//    }

    public static void createOutputViews(JCas jCas, String sofaData, String... views) throws CASException {
        Set<String> viewsToAdd = new HashSet<>();
        for (String outputViewName : views) {
            if (outputViewName == null) {
                outputViewName = Amicus.DEFAULT_MERGED_VIEW;
            }
            viewsToAdd.add(outputViewName);
        }
        Iterator<JCas> viewIter = jCas.getViewIterator();
        while (viewIter.hasNext()) {
            viewsToAdd.remove(viewIter.next().getViewName());
        }
        for (String viewToAdd : viewsToAdd) {
            JCas addedView = jCas.createView(viewToAdd);
            addedView.setSofaDataString(sofaData, "text");
        }
    }

    public static Object getSofaData(JCas jCas) throws CASException {
        // todo: modify this so that we can get strings or arrays (for audio, etc.)
        String sofaData = "";
        Iterator<JCas> viewIter = jCas.getViewIterator();
        while (viewIter.hasNext() && "".equals(sofaData)) {
            sofaData = viewIter.next().getSofaDataString();
        }
        if ("".equals(sofaData)) {
            // todo: log
//                LOGGER.warning("No sofaData found in any view!");
        }
        return sofaData;
    }

}
