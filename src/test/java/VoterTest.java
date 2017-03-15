import edu.umn.amicus.Counter;
import edu.umn.amicus.Voter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by gpfinley on 3/15/17.
 */
public class VoterTest {

    public static void main(String[] args) {
        List<Integer> myInts = new ArrayList<>();
        Random random = new Random();
        for (int i=0; i<40; i++) {
            myInts.add(random.nextInt(10));
        }

        Voter voter = new Voter(myInts);

        System.out.println(myInts.subList(0, 40));
        System.out.println(new Counter(myInts).entrySet());
        System.out.println(voter.getWinner());
    }


}
