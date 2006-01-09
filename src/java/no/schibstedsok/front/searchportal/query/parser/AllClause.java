/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.query.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public final class AllClause implements OperationClause {

    private final List/*<Clause>*/ clauses;

    /**
     */
    public AllClause() {
        clauses = new ArrayList();
    }

    /**
     *
     * @param clauses
     */
    public AllClause(final List/*<Clause>*/ clauses) {
        this.clauses = clauses;
    }

    /**
     *
     * @param c
     */
    public void addClause(final Clause c) {
        clauses.add(c);
    }

    /**
     *
     * @return
     */
    public List/*<Clause>*/ getClauses() {
        return clauses != null
                ? Collections.unmodifiableList(clauses)
                : null;
    }

    /**
     *
     * @param visitor
     */
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }
}
