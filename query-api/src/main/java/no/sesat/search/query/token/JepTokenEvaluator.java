/* Copyright (2005-2008) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 *
 * JepTokenEvaluator.java
 *
 * Created on 14 March 2006, 23:14
 *
 */

package no.sesat.search.query.token;

import org.nfunk.jep.JEP;
import org.nfunk.jep.type.Complex;

/** Evaluates whether a term or query forms a mathimatical expression.
 *
 *   <b>Immutable</b>
 *
 * @version $Id$
 *
 */
public final class JepTokenEvaluator implements TokenEvaluator {

    // Constants -----------------------------------------------------

    //private static final Logger LOG = Logger.getLogger(JepTokenEvaluator.class);

    // Attributes ----------------------------------------------------

    private String query = null;
    private final Complex result;
    private final boolean queryDependant;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /**
     * Creates a new instance of JepTokenEvaluator and evaluate the query.
     * @param query the query evaluation is occuring against.
     * @param queryDependant create a complex for every term or just the query? performance consideration.
     */
    public JepTokenEvaluator(final String query, final boolean queryDependant) {

        // avoid evaulation on just a number
        result = query.matches("[0-9.]+")
                ? null
                : getComplex(query);

        this.query = query;
        this.queryDependant = queryDependant;

    }

    // Public --------------------------------------------------------

    public Complex getComplex(final String expression) {

        if(null != query && !expression.equals(query)){

            final JEP parser = new JEP();

            parser.addStandardConstants();
            parser.addStandardFunctions();
            parser.addComplex();
            //parser.setImplicitMul(true);

            parser.parseExpression(expression);

            return parser.getComplexValue();

        }else{
            return result;
        }
    }

    // TokenEvaluator implementation ----------------------------------------------

    /**
     * Returns true if the query (or term) satifies a JED expression.
     *
     * @param token
     *            not used by this implementation.
     * @param term
     *            the term currently parsing.
     * @param query
     *            the query to find matches in.
     *              can be null. this indicates we can just use the term.
     *
     * @return true if any of the patterns matches.
     */
    public boolean evaluateToken(final TokenPredicate token, final String term, final String query) {

        return null != query
                ? null != getComplex(query)
                : isQueryDependant(token) ? null != getComplex(term) : false;
    }

    public boolean isQueryDependant(final TokenPredicate predicate) {
        return queryDependant;
    }

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------



}
