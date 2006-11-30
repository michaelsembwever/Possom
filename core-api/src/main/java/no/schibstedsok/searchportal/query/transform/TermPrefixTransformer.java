/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.query.transform;

import java.util.Map;
import no.schibstedsok.searchportal.query.AndClause;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.DefaultOperatorClause;
import no.schibstedsok.searchportal.query.IntegerClause;
import no.schibstedsok.searchportal.query.LeafClause;
import no.schibstedsok.searchportal.query.OperationClause;
import no.schibstedsok.searchportal.query.OrClause;
import no.schibstedsok.searchportal.query.PhoneNumberClause;
import org.apache.log4j.Logger;

/**
 * A transformer to prefix the terms in a query.
 *
 * @version $Id: TermPrefixTransformer.java 3369 2006-08-07 08:26:53Z mickw $
 * @author <a href="mailto:magnus.eklund@sesam.no">Magnus Eklund</a>
 *
 */
public final class TermPrefixTransformer extends AbstractQueryTransformer {

    private static final Logger LOG = Logger.getLogger(TermPrefixTransformer.class);

    private String numberPrefix;
    private String prefix;

    /**
     * This is th default fallback. Adds the prefix in the <code>prefix</code>
     * property
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final LeafClause clause) {
        if (clause.getField() != null && getContext().getFieldFilter(clause) == null) {
            addPrefix(clause, getPrefix());
        }
    }

    /**
     * Add prefix to an integer clause.
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final IntegerClause clause) {
        addPrefix(clause, getNumberPrefix());
    }

    /**
     * Add prefixes to an or clause. The two operand clauses are prefixed
     * individually.
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final OrClause clause) {
        clause.getFirstClause().accept(this);
        clause.getSecondClause().accept(this);
    }

    /**
     * Add prefixes to an default operator clause. The two operand clauses are prefixed
     * individually.
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final DefaultOperatorClause clause) {
        clause.getFirstClause().accept(this);
        clause.getSecondClause().accept(this);
    }

    /**
     * Add prefixes to an and operator clause. The two operand clauses are prefixed
     * individually.
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final AndClause clause) {
        clause.getFirstClause().accept(this);
        clause.getSecondClause().accept(this);
    }

    /**
     * Add prefixes to an generic operator clause. The child operand clauses is prefixed
     * individually.
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final OperationClause clause) {
        clause.getFirstClause().accept(this);
    }

    /**
     * Prefix a phone number clause with the number prefix.
     *
     * @param clause  The clause to prefix.
     */
    public void visitImpl(final PhoneNumberClause clause) {
        addPrefix(clause, getNumberPrefix());
    }

    /**
     * Get the prefix to be used for words.
     *
     * @return the prefix.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Get the prefix to be used for integers.
     *
     * @return the numberPrefix.
     */
    public String getNumberPrefix() {
        return numberPrefix;
    }

    /**
     * Set the prefix to used for numbers.
     *
     * @param numberPrefix The prefix.
     */
    public void setNumberPrefix(final String numberPrefix) {
        this.numberPrefix = numberPrefix;
    }

    /**
     * Set the prefix to be used for words.
     * @param prefix The prefix to set.
     */
    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    private void addPrefix(final Clause clause, final String prefix) {
        final String term = (String) getTransformedTerms().get(clause);

        if (!(term.equals("") || isAlreadyPrefixed(term, prefix))) {
            getTransformedTerms().put(clause, prefix + ':' + term);
        }
    }

    private static boolean isAlreadyPrefixed(final String term, final String prefix) {
        return term.indexOf(prefix + ':') > -1;
    }

    private Map getTransformedTerms() {
        return getContext().getTransformedTerms();
    }

    public Object clone() throws CloneNotSupportedException {
        final TermPrefixTransformer retValue = (TermPrefixTransformer)super.clone();
        retValue.numberPrefix = numberPrefix;
        retValue.prefix = prefix;
        return retValue;
    }
}
