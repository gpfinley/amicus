package edu.umn.amicus.uimacomponents;

import edu.umn.amicus.Amicus;
import edu.umn.amicus.DocumentID;
import edu.umn.amicus.AmicusException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;

import java.util.Arrays;
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

    public static void createOutputViews(JCas jCas, String sofaData, String... views) throws CASException {
        Set<String> viewsToAdd = new HashSet<>(Arrays.asList(views));
        Iterator<JCas> viewIter = jCas.getViewIterator();
        while (viewIter.hasNext()) {
            viewsToAdd.remove(viewIter.next().getViewName());
        }
        for (String viewToAdd : viewsToAdd) {
            JCas addedView = jCas.createView(viewToAdd);
            try {
                Amicus.verifySofaData(sofaData);
            } catch (AmicusException e) {
                throw new AmicusException("Non-matching sofa data in view " + viewToAdd);
            }
            // todo: allow non-text data
            addedView.setSofaDataString(sofaData, "text");
        }
    }

    public static Object getSofaData(JCas jCas) throws CASException {
        // todo: modify this so that we can get strings or arrays (for audio, etc.)
        String sofaData = null;
        Iterator<JCas> viewIter = jCas.getViewIterator();
        while (viewIter.hasNext() && (sofaData == null || "".equals(sofaData))) {
            sofaData = viewIter.next().getSofaDataString();
        }
        if (sofaData == null) {
            throw new AmicusException("Could not find Sofa data in any View!");
        }
        return sofaData;
    }

}
