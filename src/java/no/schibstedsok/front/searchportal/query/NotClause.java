/* Copyright (2005-2006) Schibsted Søk AS
 * NotClause.java
 *
 * Created on 15 February 2006, 13:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.query;


/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface NotClause extends OperationClause {
    /**
     * Get the clause.
     * 
     * @return the clause.
     */
    Clause getClause();
    
}
