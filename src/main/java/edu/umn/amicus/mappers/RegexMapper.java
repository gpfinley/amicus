package edu.umn.amicus.mappers;

import java.util.regex.Pattern;

/**
 * Created by gpfinley on 3/3/17.
 */
public class RegexMapper extends Mapper {

    private String matchPattern;
    private String replacePattern;

    private Pattern pattern;

    @Override
    protected String mappingFunction(Object from) {
        return from == null ? null : pattern.matcher(from.toString()).replaceAll(replacePattern);
    }

    @Override
    public void initialize() {
        pattern = Pattern.compile(matchPattern);
    }

    public String getMatchPattern() {
        return matchPattern;
    }

    public void setMatchPattern(String matchPattern) {
        this.matchPattern = matchPattern;
    }

    public String getReplacePattern() {
        return replacePattern;
    }

    public void setReplacePattern(String replacePattern) {
        this.replacePattern = replacePattern;
    }
}
