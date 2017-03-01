package Aligners;

import edu.umn.amicus.aligners.Aligner;
import edu.umn.amicus.aligners.FullOverlapAligner;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by gpfinley on 3/1/17.
 */
public class FullOverlapAlignerTest {

    public static void main(String[] args) {
        Aligner aligner = new FullOverlapAligner();

        /*
        three blind mice
        hickory dickory dock
        stop in the name of love

        predicted:
        three, hickory, stop
        0, hickory, in
        blind, 0, the
        0, dickory, the
        mice, 0, name
        0, dock, of
        0, 0, love
         */

        Annotation[] inputs1 = new Annotation[]{
                new Annot("three", 0, 5),
                new Annot("blind", 6, 11),
                new Annot("mice", 12, 16)
        };
        Annotation[] inputs2 = new Annotation[]{
                new Annot("hickory", 0, 7),
                new Annot("dickory", 8, 15),
                new Annot("dock", 16, 20)
        };
        Annotation[] inputs3 = new Annotation[]{
                new Annot("stop", 0, 4),
                new Annot("in", 5, 7),
                new Annot("the", 8, 11),
                new Annot("name", 12, 16),
                new Annot("of", 17, 19),
                new Annot("love", 20, 24)
        };


        List<List<Annotation>> bigList = new ArrayList<>();
        bigList.add(Arrays.asList(inputs1));
        bigList.add(Arrays.asList(inputs2));
        bigList.add(Arrays.asList(inputs3));

        Iterator<List<Annotation>> iterator = aligner.alignAndIterate(bigList);
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }

    private static class Annot extends Annotation {

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
