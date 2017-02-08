package edu.umn.ensembles;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;

/**
 * Basic utility functions for use by UIMA components
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
            filenameView = jCas.getView(Ensembles.DOCID_VIEW);
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
            docIdView = cas.getJCas().getView(Ensembles.DOCID_VIEW);
        } catch (CASException e) {
            e.printStackTrace();
            throw new EnsemblesException();
        }
        DocumentID docID = new DocumentID(docIdView);
        docID.setDocumentID(id);
        docID.addToIndexes();
    }

}
