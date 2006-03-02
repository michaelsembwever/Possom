/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.query.transform;

import java.util.Map;
import no.schibstedsok.front.searchportal.query.AndClause;
import no.schibstedsok.front.searchportal.query.Clause;
import no.schibstedsok.front.searchportal.query.IntegerClause;
import no.schibstedsok.front.searchportal.query.LeafClause;
import no.schibstedsok.front.searchportal.query.OrClause;
import no.schibstedsok.front.searchportal.query.OrganisationNumberClause;
import no.schibstedsok.front.searchportal.query.PhoneNumberClause;
import no.schibstedsok.front.searchportal.query.PhraseClause;
import no.schibstedsok.front.searchportal.query.WordClause;
import org.apache.log4j.Logger;

/**
 * A transformer to prefix the the terms in a query.
 *
 * @version $Id$
 * @author <a href="mailto:magnus.eklund@sesam.no">Magnus Eklund</a>
 *
 */
public class TermPrefixTransformer extends AbstractQueryTransformer {
    
    private static final Logger LOG = Logger.getLogger(TermPrefixTransformer.class);
    
    private static final String DEBUG_ADDING_PREFIX = "Adding prefix to term ";
    
    private String numberPrefix;
    private String prefix;
    
    /**
     * This is th default fallback. Adds the prefix in the <code>prefix</code>
     * property
     *
     * @param clause The clause to prefix.
     */
    public void visitImpl(final LeafClause clause) {
        addPrefix(clause, getPrefix());
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
     * Prefix a phone number clause with the number prefix.
     *
     * @param clause  The clause to prefix.
     */
    public void visitImpl(final PhoneNumberClause clause) {
        addPrefix(clause, getNumberPrefix());
    }
    
    /**
     * Prefix a org. number clause with the number prefix.
     *
     * @param clause  The clause to prefix.
     */
    public void visitImpl(final OrganisationNumberClause clause) {
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
        
        if (! (term.equals("") || isAlreadyPrefixed(term))) {
            getTransformedTerms().put(clause, prefix + ":" + term);
        }
    }
    
    private static boolean isAlreadyPrefixed(String term) {
        return term.indexOf(":") > -1;
    }
    
    private Map getTransformedTerms() {
        return getContext().getTransformedTerms();
    }
}
