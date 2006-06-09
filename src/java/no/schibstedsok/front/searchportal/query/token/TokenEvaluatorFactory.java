/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.query.token;

import java.util.Set;
import no.schibstedsok.common.ioc.BaseContext;
import no.schibstedsok.front.searchportal.configuration.loader.ResourceContext;
import no.schibstedsok.front.searchportal.query.QueryStringContext;
import no.schibstedsok.front.searchportal.site.Site;
import no.schibstedsok.front.searchportal.site.SiteContext;

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

    public interface Context extends BaseContext, QueryStringContext, ResourceContext, SiteContext{
    }
    
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
     
     void setClausesKnownPredicates(Set<TokenPredicate> knownPredicates);
     
     Set<TokenPredicate> getClausesKnownPredicates();
     
     void setClausesPossiblePredicates(Set<TokenPredicate> possiblePredicates);
     
     Set<TokenPredicate> getClausesPossiblePredicates();
     
     Site getSite();
}
