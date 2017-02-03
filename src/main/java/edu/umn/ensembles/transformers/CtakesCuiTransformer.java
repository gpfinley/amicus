package edu.umn.ensembles.transformers;

import edu.umn.ensembles.PreAnnotation;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Parse the FSArray of UmlsConcepts provided by cTAKES and return the first CUI.
 * Created by gpfinley on 10/20/16.
 */
public class CtakesCuiTransformer extends AnnotationTransformer<String> {

    public CtakesCuiTransformer(String fieldName) {
        super(fieldName);
    }

    /**
     * Loop through the FSArray in the cTAKES system and return the first result with a CUI.
     * @param annotation
     * @return
     */
    @Override
    public PreAnnotation<String> transform(Annotation annotation) {
        return new PreAnnotation(getCui(annotation), annotation);
    }

    protected String getCui(Annotation annotation) {
        String cui = "";
        FSArray conceptArray = (FSArray) callAnnotationGetter(annotation);
        if (conceptArray != null) {
            for (int i = 0; i < conceptArray.size(); i++) {
                if (conceptArray.get(i) instanceof UmlsConcept) {
                    UmlsConcept ontologyConcept = (UmlsConcept) conceptArray.get(0);
                    cui = ontologyConcept.getCui();
                    break;
                }
            }
        }
        return cui;
    }

}






// todo: delete

//        for (Type type : typeList) {
//
//            List<List<Annotation>> annotationsBySystem = new ArrayList<>();
////        for (String systemName : AppConfiguration.getSystemNames()) {
//            for (int i = 0; i < AppConfiguration.getNumSystems(); i++) {
//                List<Annotation> annotations = new ArrayList<>();
//                annotationsBySystem.add(annotations);
//                Iterator<Annotation> iter = systemViews.get(i).getAnnotationIndex(type).iterator();
//                while (iter.hasNext()) {
//                    annotations.add(iter.next());
//
//                }
//            }
//
//
//        }

//        Iterator<FSIndex<FeatureStructure>> fsi = systemViews.get(0).getFSIndexRepository().getIndexes();
//        while (fsi.hasNext()) {
//
//            Iterator<FeatureStructure> it = fsi.next().iterator();
//            while (it.hasNext()) {
//                FeatureStructure fs = it.next();
//                if (fs instanceof NonEmptyFSList) {
//                    List<TOP> tops = getTopsFromFsList((NonEmptyFSList) fs);
////                    System.out.println(tops);
////                    NonEmptyFSList fsList = (NonEmptyFSList) fs;
////                    System.out.println(fsList.getHead());
////                    System.out.println(fsList.getTail());
//////                    System.out.println(((FSList) fs).getNthElement(0));
//////                    System.out.println(((FSList) fs).getNthElement(1));
//////                    System.out.println(((FSList) fs).getNthElement(2));
//
//                }
////                if (fs instanceof ArrayFS) {
////                    System.out.println("LSDKJF");
////                    ArrayFS arrayFs = (ArrayFS) fs;
////                    for (int i=0; i<arrayFs.size(); i++) {
////                        System.out.println(arrayFs.get(i).getType().toString());
////                    }
////                }
//
//                if (fs instanceof IdentifiedAnnotation) {
//                    FSArray fsa = ((IdentifiedAnnotation) fs).getOntologyConceptArr();
//                    for (int i=0; i<fsa.size(); i++) {
//                        System.out.println(fsa.get(0));
////                        fsa.get
//                    }
//                }
//
//            }
//        }



//    private List<TOP> getTopsFromFsList(NonEmptyFSList fs) {
//        List<TOP> tops = new ArrayList<>();
//        FSList next = fs;
//        while (!(next instanceof EmptyFSList)) {
//            tops.add(((NonEmptyFSList) next).getHead());
//            next = ((NonEmptyFSList) next).getTail();
//        }
//        return tops;
//    }