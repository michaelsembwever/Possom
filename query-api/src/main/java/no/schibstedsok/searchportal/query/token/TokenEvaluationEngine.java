/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.searchportal.query.token;

import java.util.Set;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.searchportal.query.Clause;
import no.schibstedsok.searchportal.query.Query;
import no.schibstedsok.searchportal.site.config.ResourceContext;
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

    public interface State{
        /** the current clause's term, or null if in query-evaluation mode. **/
        String getTerm();
        /** the current query, or null if in term-evaluation mode. **/
        Query getQuery();
        /** known matching predicates. by making this available performance is improved. **/
        Set<TokenPredicate> getKnownPredicates();
        /** possible matching predicates. by making this available performance is improved. **/
        Set<TokenPredicate> getPossiblePredicates();
    }

    /** Find or create the TokenEvaluator that will evaluate if given (Token)Predicate is true.
     *
     * @param token
     * @return
     */
    TokenEvaluator getEvaluator(TokenPredicate token) throws VeryFastListQueryException;

    /**
     *
     *
     * @return
     */
    String getQueryString();

     /** TODO comment me. **/
    Site getSite();

    /** Utility method to perform one-off evaluations on terms from non RunningQuery threads.
     * Typically used by TokenTransformers or performing evaluations on non-clause oriented strings.
     **/
    boolean evaluateTerm(TokenPredicate predicate, String term);

    /** Utility method to perform one-off evaluations on clauses from non RunningQuery threads.
     * Typically used by TokenTransformers or performing evaluations on non-clause oriented strings.
     **/
    boolean evaluateClause(TokenPredicate predicate, Clause clause);

    /** Utility method to perform one-off evaluations on queries from non RunningQuery threads.
     * Typically used by TokenTransformers or performing evaluations on non-clause oriented strings.
     **/
    boolean evaluateQuery(TokenPredicate predicate, Query query);

    Thread getOwningThread();

    /**
     * Getter for property state.
     * @return Value of property state.
     */
    public State getState();

    /**
     * Setter for property state.
     * @param state New value of property state.
     */
    public void setState(State state);


}
