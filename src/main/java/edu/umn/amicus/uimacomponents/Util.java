package edu.umn.amicus.uimacomponents;

import edu.umn.amicus.*;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import java.util.*;

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

    public static String getDocumentID(JCas jCas) throws CASException {
        JCas filenameView = jCas.getView(Amicus.DOCID_VIEW);
        JFSIndexRepository jfsIndexRepository = filenameView.getJFSIndexRepository();
        FSIterator<DocumentID> outPathIter = jfsIndexRepository.getAllIndexedFS(DocumentID.type);
        return outPathIter.next().getDocumentID();
    }

    public static void setDocumentID(CAS cas, String id) throws CASException {
        JCas docIdView = cas.getJCas().getView(Amicus.DOCID_VIEW);
        DocumentID docID = new DocumentID(docIdView);
        docID.setDocumentID(id);
        docID.addToIndexes();
    }

    public static void createOutputViews(JCas jCas, String... views) throws CASException {
        Set<String> viewsToAdd = new HashSet<>(Arrays.asList(views));
        Iterator<JCas> viewIter = jCas.getViewIterator();
        while (viewIter.hasNext()) {
            viewsToAdd.remove(viewIter.next().getViewName());
        }
        for (String viewToAdd : viewsToAdd) {
            JCas addedView = jCas.createView(viewToAdd);
            // todo: allow non-text data
            addedView.setSofaDataString(Amicus.getSofaData(getDocumentID(jCas)).toString(), "text");
        }
    }

    public static Class<? extends Annotation> getTypeClass(String typeClassName) throws AmicusException {
        Class<? extends Annotation> typeClass;
        try {
            if (typeClassName == null) {
                typeClassName = AnalysisPieceFactory.DEFAULT_TYPE_NAME;
            }
            typeClass = (Class<? extends Annotation>) Class.forName(typeClassName);
        } catch (ClassNotFoundException e) {
            throw new AmicusException(e);
        }
        return typeClass;
    }

//    public static void createOutputViews(JCas jCas, String sofaData, String... views) throws CASException, MismatchedSofaDataException {
//        Set<String> viewsToAdd = new HashSet<>(Arrays.asList(views));
//        Iterator<JCas> viewIter = jCas.getViewIterator();
//        while (viewIter.hasNext()) {
//            viewsToAdd.remove(viewIter.next().getViewName());
//        }
//        for (String viewToAdd : viewsToAdd) {
//            // todo: allow non-text data
//            JCas addedView = jCas.createView(viewToAdd);
//            addedView.setSofaDataString(sofaData, "text");
//        }
//        Amicus.verifySofaData(sofaData);
//    }
//
//    public static Object getSofaData(JCas jCas) throws CASException, AmicusException {
//        // todo: modify this so that we can get strings or arrays (for audio, etc.)
//        Iterator<JCas> viewIter = jCas.getViewIterator();
//        while (true) {
//            try {
//                String sofaData = viewIter.next().getSofaDataString();
//                if (sofaData != null && !"".equals(sofaData)) return sofaData;
//            } catch (NoSuchElementException e) {
//                throw new AmicusException("Could not find Sofa data in any View!");
//            }
//        }
//    }

}
