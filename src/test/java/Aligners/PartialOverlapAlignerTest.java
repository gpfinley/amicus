package Aligners;

import edu.umn.amicus.AlignedTuple;
import edu.umn.amicus.ANA;
import edu.umn.amicus.aligners.Aligner;
import edu.umn.amicus.aligners.PartialOverlapAligner;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Simple test for partial overlap aligner
 *
 * Created by gpfinley on 3/1/17.
 */
public class PartialOverlapAlignerTest {

    private static List<ANA> annotationListFromString(String str) {
        List<ANA> annotations = new ArrayList<>();
        char[] chars = str.toCharArray();
        String current = "";
        int start = 0;
        for (int i=0; i<=chars.length; i++) {
            if (i == chars.length || chars[i] == ' ') {
                if (!"".equals(current)) {
                    annotations.add(new ANA(current, start, i));
                }
                start = i+1;
                current = "";
            } else {
                current += chars[i];
            }
        }
        return annotations;
    }

    public static void main(String[] args) throws Exception {
        Aligner aligner = new PartialOverlapAligner();

        List<List<ANA>> bigList = new ArrayList<>();

        bigList.add(annotationListFromString("         hh     iiii j  k"));
        bigList.add(annotationListFromString("aaaaaaaaa        bbbb   cccccccccc"));
        bigList.add(annotationListFromString("dddddddddd      eee ff  gggggggg"));

        Iterator<AlignedTuple> iterator = aligner.alignAndIterate(bigList);
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }

    static class Annot extends Annotation {

        static JCas jCas;
        static {
            try {
                jCas = JCasFactory.createJCas();
            } catch(UIMAException e) {
                throw new RuntimeException();
            }
        }


        private String value;
        private int begin;
        private int end;

        private Annot() {}

        Annot(String value, int begin, int end) {
            super(jCas);
            this.value = value;
            this.begin = begin;
            this.end = end;
        }

        @Override
        public int getBegin() {
            return begin;
        }

        @Override
        public int getEnd() {
            return end;
        }

        @Override
        public String toString() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            Annot annot = (Annot) o;

            if (begin != annot.begin) return false;
            if (end != annot.end) return false;
            return !(value != null ? !value.equals(annot.value) : annot.value != null);

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (value != null ? value.hashCode() : 0);
            result = 31 * result + begin;
            result = 31 * result + end;
            return result;
        }
    }
}
