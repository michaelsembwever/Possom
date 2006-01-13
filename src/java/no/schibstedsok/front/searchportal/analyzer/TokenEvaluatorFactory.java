/*
 * Copyright (2005-2006) Schibsted Søk AS
 */
package no.schibstedsok.front.searchportal.analyzer;

import no.schibstedsok.front.searchportal.query.parser.Query;

/**
 * A TokenEvaluateFactory provides knowledge about which implementation of
 * {@link TokenEvaluator} that can handle a particular token.
 *
 * It also contains state as to what is the current term being tokenised.
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public interface TokenEvaluatorFactory {

    /**
     * FIXME Comment this
     *
     * @param token
     * @return
     * @todo    Simplify. Maybe using different prefixes for different evaluators.
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

}