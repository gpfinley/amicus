package edu.umn.amicus.exporters;

import java.util.regex.Pattern;

/**
 * Created by greg on 2/14/17.
 */
public class CsvExporter extends TsvExporter {

    private static final Pattern badChar = Pattern.compile("[\"\n\r,]");

    public CsvExporter() {
        super();
        delimiter = ",";
    }

    /**
     * Escapes delimiters and deals with quotes according to the CSV spec.
     * @param raw
     * @return
     */
    @Override
    protected String escapeDelimiter(String raw) {
        if (badChar.matcher(raw).find()) {
            // escape existing quotes with double quotes
            String modified = raw.replace("\"", "\"\"");
            return "\"" + modified + "\"";
        } else {
            return raw;
        }
    }

    @Override
    public String getFileExtension() {
        return "csv";
    }

}
