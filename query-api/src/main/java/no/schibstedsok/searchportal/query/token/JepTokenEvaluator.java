/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 *
 * JepTokenEvaluator.java
 *
 * Created on 14 March 2006, 23:14
 *
 */

package no.schibstedsok.searchportal.query.token;

import org.apache.log4j.Logger;
import org.nfunk.jep.JEP;
import org.nfunk.jep.type.Complex;

/**
 * @version $Id$
 * @author <a href="mailto:mick@wever.org">Michael Semb Wever</a>
 */
public final class JepTokenEvaluator implements TokenEvaluator {

    // Constants -----------------------------------------------------

    private static final Logger LOG = Logger.getLogger(JepTokenEvaluator.class);
    private static final String DEBUG_NOT_INTEGER = "Was not an integer ";

    // Attributes ----------------------------------------------------

    private final Complex result;

    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------

    /**
     * Creates a new instance of JepTokenEvaluator
     */
    public JepTokenEvaluator(final String query) {

        final JEP parser = new JEP();

        parser.addStandardConstants();
        parser.addStandardFunctions();
        parser.addComplex();
//        parser.setImplicitMul(true);


        parser.parseExpression(query);

        result = parser.getComplexValue();
    }

    // Public --------------------------------------------------------

    public Complex getComplex() {
        return result;
    }

    // TokenEvaluator implementation ----------------------------------------------

    /**
     * Returns true if any of the query satifies a JED expression.
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
        return result != null;
    }

    public boolean isQueryDependant(TokenPredicate predicate) {
        return true;
    }

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------



}
