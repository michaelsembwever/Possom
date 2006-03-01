/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.query.transform;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TermPrefixTransformer extends AbstractQueryTransformer {

    private static Pattern numbers = Pattern.compile("^\\d+$");

    private String numberPrefix;
    private String prefix;

    protected String prefixTerms(final String query) {

        String stripped = query;

        stripped = stripped.replaceAll(",", "");
        stripped = stripped.replaceAll("#", "");
        stripped = stripped.replaceAll("\\s+", " ");

        stripped = stripped.replaceAll("\"", "");

        if (stripped.length() == 0) {
            return "";
        }

        final String[] tokens = stripped.split("\\s+");


        final StringBuffer newQuery = new StringBuffer();


        for (int i = 0; i < tokens.length; i++) {

            if (numberPrefix != null) {
                final Matcher m = numbers.matcher(tokens[i]);

                if (m.find()) {
                    newQuery.append(numberPrefix).append(":");
                } else {
                    newQuery.append(prefix).append(":");
                }
            } else {
                newQuery.append(prefix).append(":");
            }

            newQuery.append(tokens[i]);

            if (i < tokens.length - 1) {
                newQuery.append(" ");
            }
        }

        return newQuery.toString();
    }

    public TermPrefixTransformer() {
        super();
    }

    public TermPrefixTransformer(final String prefix, final String numberPrefix) {
        super();
        this.prefix = prefix;
        this.numberPrefix = numberPrefix;
    }

    /**
     * Get the numberPrefix.
     *
     * @return the numberPrefix.
     */
    public String getNumberPrefix() {
        return numberPrefix;
    }

    /**
     * Get the prefix.
     *
     * @return the prefix.
     */
    public String getPrefix() {
        return prefix;
    }

    public String getTransformedQuery() {
        return prefixTerms(getContext().getTransformedQuery());
    }


    /**
     * Set the numberPrefix.
     *
     * @param numberPrefix The numberPrefix to set.
     */
    public void setNumberPrefix(final String numberPrefix) {
        this.numberPrefix = numberPrefix;
    }

    /**
     * Set the prefix.
     *
     * @param prefix The prefix to set.
     */
    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }
}
