/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 */
/*
 * TokenEvaluationEngineContext.java
 *
 * Created on 22 December 2006, 16:10
 *
 */

package no.sesat.search.query.token;

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
