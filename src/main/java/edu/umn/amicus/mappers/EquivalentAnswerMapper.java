package edu.umn.amicus.mappers;

import edu.umn.amicus.Amicus;
import edu.umn.amicus.AmicusException;
import edu.umn.amicus.config.ClassConfigurationLoader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Maps strings to other strings that are considered equivalent for evaluation purposes.
 * Equivalencies are stored in the class-specific config file:
 *      one term per line, with blank lines between clusters of equivalent terms.
 *
 * Created by gpfinley on 10/13/16.
 */
public class EquivalentAnswerMapper extends Mapper<String, String> {

    private static Logger LOGGER = Logger.getLogger(EquivalentAnswerMapper.class.getName());

    private List<List<String>> equivalentsList;

    void buildInternalMap() {
        internalMap = new HashMap<>();
        for (List<String> cluster : equivalentsList) {
            for (int i = 1; i < cluster.size(); i++) {
                if (internalMap.containsKey(cluster.get(i))) {
                    //todo; warn
                }
                internalMap.put(cluster.get(i).toLowerCase(), cluster.get(0));
            }
        }
    }

    @Override
    public String map(String word) {
        if (internalMap == null) {
            buildInternalMap();
            equivalentsList = null;
        }
        // todo: why would it be null?
        if (word == null) return internalMap.get(null);
        return internalMap.getOrDefault(word.trim().toLowerCase(), word.trim());
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
