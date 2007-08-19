/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 * OperationClause.java
 *
 * Created on 11 January 2006, 14:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.sesat.searchportal.query;


/** An operation clause. Often a join between two other clauses, but can also be a prefix operator
 * to another term.
 *
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface OperationClause extends Clause {
    /**
     * Get the clause.
     * 
     * 
     * @return the clause.
     */
    Clause getFirstClause();

}
