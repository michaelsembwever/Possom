/* Copyright (2005-2006) Schibsted Søk AS
 * DoubleOperatorClause.java
 *
 * Created on 11 January 2006, 14:16
 *
 */

package no.schibstedsok.searchportal.query;


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
