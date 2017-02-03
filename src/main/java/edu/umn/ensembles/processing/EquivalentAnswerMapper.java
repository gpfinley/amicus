package edu.umn.ensembles.processing;

import edu.umn.ensembles.config.ClassConfigurationLoader;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Maps strings to other strings that are considered semantically equivalent.
 * Equivalencies are stored in the class-specific config file:
 *      one term per line, with blank lines between clusters of equivalent terms.
 *
 * Created by gpfinley on 10/13/16.
 */
public class EquivalentAnswerMapper implements Mapper<String, String> {

    private static Logger LOGGER = Logger.getLogger(EquivalentAnswerMapper.class.getName());

    private final static EquivalentAnswerMapper mapper;

    static {
        try {
            LOGGER.info("Loading thesaurus for EquivalentAnswerMapper...");
            String contents = (String) ClassConfigurationLoader.load(EquivalentAnswerMapper.class);
            mapper = new EquivalentAnswerMapper(contents);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
    private final Map<String, String> stringToHeadword;

    protected EquivalentAnswerMapper(String contents) {
        stringToHeadword = new HashMap<>();
        for (String cluster : contents.split("\\n\\s*\\n")) {
            String[] lines = cluster.split("\\s*\\n\\s*");
            for (int i = 0; i < lines.length; i++) {
                stringToHeadword.put(lines[i].toLowerCase(), lines[0]);
            }
        }
    }

    public static EquivalentAnswerMapper getInstance() {
        return mapper;
    }

    public String map(String word) {
        return stringToHeadword.getOrDefault(word.trim().toLowerCase(), word.trim());
    }

    public static void main(String[] args) {

    }

}
