package edu.umn.amicus.processing;

import edu.umn.amicus.AmicusException;
import edu.umn.amicus.config.ClassConfigurationLoader;

import java.io.FileNotFoundException;
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

    private static EquivalentAnswerMapper mapper;

    private final Map<String, String> stringToHeadword;

    public static EquivalentAnswerMapper getInstance() {
        if (mapper == null) {
            try {
                LOGGER.info("Loading thesaurus for EquivalentAnswerMapper...");
                String contents = (String) ClassConfigurationLoader.load(EquivalentAnswerMapper.class);
                mapper = new EquivalentAnswerMapper(contents);
            } catch (FileNotFoundException e) {
                throw new AmicusException(e);
            }
        }
        return mapper;
    }

    protected EquivalentAnswerMapper(String contents) {
        stringToHeadword = new HashMap<>();
        for (String cluster : contents.split("\\n\\s*\\n")) {
            String[] lines = cluster.split("\\s*\\n\\s*");
            for (int i = 0; i < lines.length; i++) {
                stringToHeadword.put(lines[i].toLowerCase(), lines[0]);
            }
        }
    }

    @Override
    public String map(String word) {
        return stringToHeadword.getOrDefault(word.trim().toLowerCase(), word.trim());
    }

}
