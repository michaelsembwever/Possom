/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.query.token;

import java.util.Set;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.searchportal.configuration.loader.ResourceContext;
import no.schibstedsok.searchportal.query.QueryStringContext;
import no.schibstedsok.searchportal.site.Site;
import no.schibstedsok.searchportal.site.SiteContext;

/**
 * TokenEvaluationEngine contains state as to what is the current term being tokenised,
 * and the term's sets of known and possible predicates.
 * These sets can be in building process, and provide performance improvement by not having to
 * evaluate the token twice.
 *
 * A TokenEvaluationEngine also provides knowledge about which implementation of
 * {@link TokenEvaluator} that can handle a particular token {@link TokenPredicate}.
 *
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @author <a href="mailto:mick@wever.org">Mck</a>
 * @version <tt>$Revision$</tt>
 */
public interface TokenEvaluationEngine {

    public interface Context extends BaseContext, QueryStringContext, ResourceContext, SiteContext{
    }

    /** Find or create the TokenEvaluator that will evaluate if given (Token)Predicate is true.
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

     /** TODO comment me. **/
    void setCurrentTerm(String term);

     /** TODO comment me. **/
    String getCurrentTerm();

     /** TODO comment me. **/
    void setClausesKnownPredicates(Set<TokenPredicate> knownPredicates);

     /** TODO comment me. **/
    Set<TokenPredicate> getClausesKnownPredicates();

     /** TODO comment me. **/
    void setClausesPossiblePredicates(Set<TokenPredicate> possiblePredicates);

     /** TODO comment me. **/
    Set<TokenPredicate> getClausesPossiblePredicates();

     /** TODO comment me. **/
    Site getSite();

    /** Utility method to perform one-off evaluations on terms.
     * Typically used by TokenTransformers or performing evaluations on non-clause oriented strings.
     **/
    boolean evaluateTerm(TokenPredicate predicate, String term);
}
