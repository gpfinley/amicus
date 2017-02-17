package edu.umn.amicus.aligners;

/**
 * Basic tuple class for storing begin/end locations that can be hashed for perfect alignments.
 *
 * Created by gpfinley on 2/17/17.
 */
public class BeginEnd {
    public int begin;
    public int end;

    public BeginEnd(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BeginEnd beginEnd = (BeginEnd) o;

        if (begin != beginEnd.begin) return false;
        return end == beginEnd.end;

    }

    @Override
    public int hashCode() {
        int result = begin;
        result = 31 * result + end;
        return result;
    }
}
