/* Copyright (2005-2006) Schibsted Søk AS
 * AbstractOperationClause.java
 *
 * Created on 7 January 2006, 16:05
 *
 */

package no.schibstedsok.front.searchportal.query.parser;

import java.util.Collections;
import java.util.Set;

/**
 * 
 * 
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 * @version $Id$
 */
public abstract class AbstractOperationClause extends AbstractClause implements OperationClause {

    protected AbstractOperationClause(){}
    
    protected AbstractOperationClause(
            final Set/*<Predicate>*/ knownPredicates,
            final Set/*<Predicate>*/ possiblePredicates){
        
        super(knownPredicates,possiblePredicates);
    }
}
