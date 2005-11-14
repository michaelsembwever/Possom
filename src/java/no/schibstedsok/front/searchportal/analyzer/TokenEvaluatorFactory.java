/*
 * Copyright (2005) Schibsted S¿k AS
 */
package no.schibstedsok.front.searchportal.analyzer;

/**
 * A TokenEvaluateFactory provides knowledge about which implementation of
 * {@link TokenEvaluator} that can handle a particular token.
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
    TokenEvaluator getEvaluator(String token);

    /**
     *  FIXME Comment this
     *
     * @return
     */
     String getQuery();

}