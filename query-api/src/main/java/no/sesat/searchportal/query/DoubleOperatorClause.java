/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 * DoubleOperatorClause.java
 *
 * Created on 11 January 2006, 14:16
 *
 */

package no.sesat.searchportal.query;


/** An operation clause. A join between two other clauses.
 *
 * @version $Id: OperationClause.java 3359 2006-08-03 08:13:22Z mickw $
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public interface DoubleOperatorClause extends OperationClause {
    /**
     * Get the second clause.
     *
     * @return the second clause.
     */
    Clause getSecondClause();

}
