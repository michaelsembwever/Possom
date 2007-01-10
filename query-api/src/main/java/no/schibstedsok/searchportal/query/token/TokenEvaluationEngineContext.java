/*
 * TokenEvaluationEngineContext.java
 *
 * Created on 22 December 2006, 16:10
 *
 */

package no.schibstedsok.searchportal.query.token;

import no.schibstedsok.commons.ioc.BaseContext;

/**
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface TokenEvaluationEngineContext extends BaseContext{

    /** Get the tokenEvalautorFactory.
     * Responsible for  handing out evaluators against TokenPredicates.
     * Also holds state information about the current term/clause we are finding predicates against.
     *
     * @return the TokenEvaluationEngine this Parser will use.
     */
    TokenEvaluationEngine getTokenEvaluationEngine();    
}
