package edu.umn.ensembles.uimafit;

import edu.umn.ensembles.DocumentID;
import edu.umn.ensembles.Ensembles;
import edu.umn.ensembles.EnsemblesException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;

/**
 * Utilities for UIMA processing.
 *
 * Created by gpfinley on 10/25/16.
 */
public final class Util {

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
//
//    /**
//     * Get all annotations from an all-systems-in jCas that are sought by a configuration.
//     * @param jCas a jCas as created by MultiCasReader
//     * @param configuration
//     * @return a list (one item per NLP system) of lists of annotations
//     */
//    public static List<List<Annotation>> getAnnotations(JCas jCas, MergerConfiguration configuration) {
//        List<List<Annotation>> allAnnotations = new ArrayList<>();
//        for (int i = 0; i < configuration.getAnnotationClasses().size(); i++) {
//            Class clazz = configuration.getAnnotationClasses().get(i);
//            String system = configuration.getOriginSystems().get(i);
//            JCas readView;
//            try {
//                readView = jCas.getView(Ensembles.systemToViewName(system));
//            } catch (CASException e) {
//                e.printStackTrace();
//                throw new RuntimeException();
//            }
//            List<Annotation> theseAnnotations = new ArrayList<>();
//            allAnnotations.add(theseAnnotations);
//            // Get all annotations of this class and add them to the index
//            readView.getAnnotationIndex(clazz).forEach(a -> theseAnnotations.add((Annotation) a));
//        }
//        return allAnnotations;
//    }


}
