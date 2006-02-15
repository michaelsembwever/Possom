/* Copyright (2005-2006) Schibsted Søk AS
 * AndNotClause.java
 *
 * Created on 15 February 2006, 13:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.front.searchportal.query;

import no.schibstedsok.front.searchportal.query.parser.*;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface AndNotClause extends OperationClause {
    /**
     * Get the firstClause.
     * 
     * @return the firstClause.
     */
    Clause getFirstClause();

    /**
     * Get the secondClause.
     * 
     * @return the secondClause.
     */
    Clause getSecondClause();
    
}
