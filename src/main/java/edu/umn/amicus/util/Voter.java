package edu.umn.amicus.util;

/**
 * Vote by plurality rule on a value. In cases of ties, will take the one that occurs first.
 *
 * Created by gpfinley on 3/15/17.
 */
public class Voter<T> {

    private final T voted;
    private final int maxCount;

    public Voter(Iterable<T> items) {
        Counter<T> counter = new Counter<>(items);
        if (counter.size() == 0) {
            voted = null;
            maxCount = 0;
            return;
        }
        maxCount = counter.createSortedMap().descendingMap().firstEntry().getValue();
        for (T t : items) {
            if (counter.get(t) == maxCount) {
                voted = t;
                return;
            }
        }
        throw new RuntimeException("Bug in Voter class (did not find a high count)");
    }

    public T getWinner() {
        return voted;
    }

    public int getHighCount() {
        return maxCount;
    }


}
