/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.analyzer;

import java.util.Set;


/**
 * A TokenEvaluateFactory provides knowledge about which implementation of
 * {@link TokenEvaluator} that can handle a particular token.
 *
 * It also contains state as to what is the current term being tokenised, 
 * and that terms sets of known and possible predicates.
 * These sets can be in building process, and provide performance improvement as a token does not need to be fully
 * evaluated twice.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public interface TokenEvaluatorFactory {

    /**
     *
     * @param token
     * @return
     */
    TokenEvaluator getEvaluator(TokenPredicate token);

    /**
     *
     *
     * @return
     */
     String getQueryString();

     void setCurrentTerm(String term);

     String getCurrentTerm();
     
     void setClausesKnownPredicates(Set/*<Predicate>*/ knownPredicates);
     
     Set/*<Predicate>*/ getClausesKnownPredicates();
     
     void setClausesPossiblePredicates(Set/*<Predicate>*/ possiblePredicates);
     
     Set/*<Predicate>*/ getClausesPossiblePredicates();
}
