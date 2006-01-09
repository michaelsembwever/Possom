/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.query.parser;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public class PhraseClause implements LeafClause {

    private final String phrase;
    private final String field;

    /**
     *
     * @param phrase
     * @param field
     */
    public PhraseClause(final String phrase, final String field) {
        this.phrase = phrase;
        this.field = field;
    }

    /**
     *
     * @param phrase
     */
    public PhraseClause(final String phrase) {
         this(phrase, null);
    }

    /**
     *
     * @param visitor
     */
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    /**
     * Get the phrase.
     *
     * @return the phrase.
     */
    public String getPhrase() {
        return phrase;
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
