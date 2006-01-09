/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.query.parser;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public class WordClause implements LeafClause {

    private final String term;
    private final String field;

    /**
     *
     * @param term
     */
    public WordClause(final String term) {
        this(term, null);
    }

    /**
     *
     * @param term
     * @param field
     */
    public WordClause(final String term, final String field) {
        this.term = term;
        this.field = field;
    }

    /**
     *
     * @param visitor
     */
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    /**
     * Get the term.
     *
     * @return the term.
     */
    public String getTerm() {
        return term;
    }

    /**
     * Get the field.
     *
     * @return the field.
     */
    public String getField() {
        return field;
    }

}
