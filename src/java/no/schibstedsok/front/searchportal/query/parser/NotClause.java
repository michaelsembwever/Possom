/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.query.parser;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public final class NotClause implements OperationClause {

    private Clause clause;

    /**
     *
     * @param clause
     */
    public NotClause(final Clause clause) {
        this.clause = clause;
    }

    /**
     *
     * @return
     */
    public Clause getClause() {
        return clause;
    }

    /**
     *
     * @param visitor
     */
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
