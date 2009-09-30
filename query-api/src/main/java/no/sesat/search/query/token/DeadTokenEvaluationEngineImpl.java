/*
 * Copyright (2008-2009) Schibsted ASA
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


/** A dead evaluation engine. Used when evaluation is turned off.
 * Rather than solely relying on the ALWAYS_FALSE_EVALUTOR
 * evaluate(..) always throws a EvaluationRuntimeException that is treated as a false evaluation
 *  but also prevents the evaluation for being cached by clauses (which are implemented within the flyweight pattern).
 *
 * @version $Id$
 */
public final class DeadTokenEvaluationEngineImpl extends TokenEvaluationEngineImpl {

    private static final String DEAD_EXCEPTION_TITLE = "DEAD_EVALUATOR";

    public DeadTokenEvaluationEngineImpl(final Context cxt) {
        super(cxt);
    }

    @Override
    public TokenEvaluator getEvaluator(TokenPredicate token) throws EvaluationException {
        return DEAD_EVALUATOR;
    }

    @Override
    public boolean evaluate(final TokenPredicate token) {
        throw new DeadEvaluationRuntimeException();
    }

    public static final class DeadEvaluationRuntimeException extends EvaluationRuntimeException{
        DeadEvaluationRuntimeException(){
            super(new EvaluationException(DEAD_EXCEPTION_TITLE, null));
        }
    }

}
