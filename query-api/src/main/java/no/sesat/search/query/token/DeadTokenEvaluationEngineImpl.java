/*
 * Copyright (2008) Schibsted Søk AS
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

import no.sesat.search.query.Clause;
import no.sesat.search.query.Query;
import no.sesat.search.site.Site;

/** A dead evaluation engine. Used when evaluation is turned off.
 * Rather than solely relying on the ALWAYS_FALSE_EVALUTOR
 * evaluate(..) always throws a EvaluationRuntimeException that is treated as a false evaluation
 *  but also prevents the evaluation for being cached by clauses (which are implemented within the flyweight pattern).
 *
 * @version $Id$
 */
public final class DeadTokenEvaluationEngineImpl implements TokenEvaluationEngine {

    private final String queryStr;

    private final Site site;

    public DeadTokenEvaluationEngineImpl(final String queryStr, final Site site) {
        this.queryStr = queryStr;
        this.site = site;
    }

    private State state;

    public TokenEvaluator getEvaluator(TokenPredicate token) throws EvaluationException {
        return DEAD_EVALUATOR;
    }

    public String getQueryString() {
        return queryStr;
    }

    public Site getSite() {
        return site;
    }

    public boolean evaluate(TokenPredicate token) {
        return false;
    }

    public boolean evaluateTerm(TokenPredicate predicate, String term) {
        return false;
    }

    public boolean evaluateClause(TokenPredicate predicate, Clause clause) {
        return false;
    }

    public boolean evaluateQuery(TokenPredicate predicate, Query query) {
        return false;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
