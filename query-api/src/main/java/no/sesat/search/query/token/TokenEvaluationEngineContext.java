/* Copyright (2007-2008) Schibsted Søk AS
 *   This file is part of SESAT.
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
