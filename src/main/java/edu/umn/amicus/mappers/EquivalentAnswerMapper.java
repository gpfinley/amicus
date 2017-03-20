package edu.umn.amicus.mappers;

import edu.umn.amicus.Amicus;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Maps strings to other strings that are considered equivalent for evaluation purposes.
 * Total case insensitivity.
 * Equivalencies are stored in the class-specific config file:
 *      one term per line, with blank lines between clusters of equivalent terms.
 *
 * Created by gpfinley on 10/13/16.
 */
public class EquivalentAnswerMapper extends Mapper {

    private final static Logger LOGGER = Logger.getLogger(EquivalentAnswerMapper.class.getName());

    private List<List<String>> equivalentsList;

    public void initialize() {
        internalMap = new HashMap<>();
        for (List<String> cluster : equivalentsList) {
            for (int i = 1; i < cluster.size(); i++) {
                if (internalMap.containsKey(cluster.get(i))) {
                    //todo; warn
                }
                String word = cluster.get(i).toLowerCase().trim();
                String headword = cluster.get(0).trim();
                if (internalMap.containsKey(word)) {
                    LOGGER.warning(word + " appears twice in EquivalentAnswerMapper config");
//                    internalMap.put(headword.toLowerCase(), internalMap.get(word));
//                    cluster.set(0, String.valueOf(internalMap.get(word)));
                } else {
                    internalMap.put(word, headword);
                }
            }
        }
        equivalentsList = null;
    }

    @Override
    public Object mappingFunction(Object key) {
        if (key == null) return null;
        if (internalMap == null) {
            initialize();
        }
        String string = key.toString().trim().toLowerCase();
        Object val = internalMap.get(string);
        return val == null ? string : val;
    }

    /**
     * Convert between the original format (clusters in groups of lines, extra newlines separating clusters)
     *      and the YAML format.
     * @param args the input file (text with line breaks) and output (.yml)
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String infile = args[0];
        String outfile = args[1];

        List<List<String>> equiv = new ArrayList<>();
        String contents = new Scanner(new FileInputStream(infile)).useDelimiter("\\Z").next();
        for (String cluster : contents.split("\\n\\s*\\n")) {
            String[] lines = cluster.split("\\s*\\n\\s*");
            equiv.add(Arrays.asList(lines));
        }

        EquivalentAnswerMapper m = new EquivalentAnswerMapper();
        m.setEquivalentsList(equiv);

        Amicus.yaml.dump(m, new FileWriter(outfile));
    }

    /*
     * Getters and setters for YAML
     */

    public List<List<String>> getEquivalentsList() {
        return equivalentsList;
    }

    public void setEquivalentsList(List<List<String>> equivalentsList) {
        this.equivalentsList = equivalentsList;
    }

}
