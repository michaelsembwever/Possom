/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.query.parser;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public class IntegerClause extends WordClause {

    /**
     *
     * @param term
     */
    public IntegerClause(final String term) {
        super(term);
    }

    /**
     *
     * @param term
     * @param field
     */
    public IntegerClause(final String term, final String field) {
        super(term, field);
    }

}
