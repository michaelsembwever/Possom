/* Copyright (2007-2012) Schibsted ASA
 *   This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 *
 * TokenEvaluationEngineContext.java
 *
 * Created on 22 December 2006, 16:10
 *
 */

package no.sesat.search.query.token;

import no.sesat.commons.ioc.BaseContext;

/**
 *
 * @version $Id$
 *
 */
public interface TokenEvaluationEngineContext extends BaseContext{

    /** Get the tokenEvalautorFactory.
     * Responsible for  handing out evaluators against TokenPredicates.
     * Also holds state information about the current term/clause we are finding predicates against.
     *
     * @return the TokenEvaluationEngine this Parser will use.
     */
    TokenEvaluationEngine getTokenEvaluationEngine();
}
