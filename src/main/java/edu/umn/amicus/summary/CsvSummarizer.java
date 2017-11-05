package edu.umn.amicus.summary;

import edu.umn.amicus.config.ClassConfigurationLoader;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * All summarizers that implement delimited output should use this.
 *
 * Created by gpfinley on 3/13/17.
 */
public abstract class CsvSummarizer extends Summarizer {

    protected final static String delimiter;
    private static final String TAB = "\t";
    private static final String COMMA = ",";
    private static final String PIPE = "|";
    private static final Pattern mustEscapeCsvChar = Pattern.compile("[\"\n\r,]");

    protected CsvSummarizer(String[] viewNames, String[] types, String[] fields) {
        super(viewNames, types, fields);
    }

    protected String getFileExtension() {
        if (TAB.equals(delimiter)) return "tsv";
        if (PIPE.equals(delimiter)) return "txt";
        return "csv";
    }

    protected String escapeDelimiter(String raw) {
        if (TAB.equals(delimiter)) {
            return raw.replace(delimiter, "    ");
        } else if (PIPE.equals(delimiter)) {
            return raw.replace("|", "/");
        } else {
            if (mustEscapeCsvChar.matcher(raw).find()) {
                // escape existing quotes with double quotes
                String modified = raw.replace("\"", "\"\"");
                return "\"" + modified + "\"";
            } else {
                return raw;
            }
        }
    }

    /**
     * todo...
     * Does not append the newline character!
     * @param data
     * @return
     */
    protected String buildLine(List<Object> data) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.valueOf((Object) data.get(0)));
        for (int i=1; i<data.size(); i++) {
            builder.append(delimiter)
                    .append(escapeDelimiter(String.valueOf((Object) data.get(i))));
        }
        return builder.toString();
    }

    private static class Config {
        public boolean useTabs;
        public boolean usePipe;
    }

    static {
        Config config;
        try {
            config = (Config) ClassConfigurationLoader.load(CsvSummarizer.Config.class);
        } catch (FileNotFoundException e) {
            config = null;
        }
        if (config != null) {
            if (config.useTabs) {
                delimiter = TAB;
            } else if(config.usePipe) {
                delimiter = PIPE;
            } else {
                delimiter = COMMA;
            }
        } else {
            delimiter = COMMA;
        }
    }
}
