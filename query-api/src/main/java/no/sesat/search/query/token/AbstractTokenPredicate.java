/* Copyright (2008) Schibsted Søk AS
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
 */
package no.sesat.search.query.token;

/** Base class helping with equals, hashcode, and toString overrides which all TokenPredicate implementations share.
 * Based off the requirement that TokenPredicate, of any implementation, have a unique name.
 *
 * @version $Id$
 */
public abstract class AbstractTokenPredicate implements TokenPredicate {

    private static final String ERR_ARG_NOT_TOKEN_EVALUATOR_FACTORY
            = "Argument to evaluate must be an instance of a TokenEvaluationEngine";

    // Constants -----------------------------------------------------

    //private static final Logger LOG = Logger.getLogger(AbstractTokenPredicate.class);

    // Attributes ----------------------------------------------------

    // Static --------------------------------------------------------

    static boolean evaluate(final TokenPredicate token, final Object evalFactory) {

        if (!(evalFactory instanceof TokenEvaluationEngine)) {
            throw new IllegalArgumentException(ERR_ARG_NOT_TOKEN_EVALUATOR_FACTORY);
        }
        return ((TokenEvaluationEngine) evalFactory).evaluate(token);
    }

    // Constructors --------------------------------------------------

    public AbstractTokenPredicate() {
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TokenPredicate && name().equals(((TokenPredicate) obj).name());
    }

    @Override
    public int hashCode() {

        int hash = 7;
        hash = 41 * hash + (name() != null ? (name()).hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return name();
    }

    public boolean evaluate(final Object evalFactory) {

        return evaluate(this, evalFactory);
    }

    // Public --------------------------------------------------------

    // Z implementation ----------------------------------------------

    // Y overrides ---------------------------------------------------

    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    // Inner classes -------------------------------------------------
}
