/*
 * Copyright (2005-2012) Schibsted ASA
 * This file is part of Possom.
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
 */
package no.sesat.search.query;

import no.sesat.commons.visitor.Visitable;
import java.util.Set;
import no.sesat.search.query.token.TokenPredicate;

/** A Clause in this project represents a single term or operation on or between terms in a Query string.
 * A heirarchy of Clause objects will therefore represent a Query and avoid unneccessary string manipulations.
 * <b>All Clause subclasses MUST be immutable.</b>
 * State describing information will be stored in the wrapping Query class.
 *
 * @version $Id$
 *
 */
public interface Clause extends Visitable {
    /**
     * get the term.
     * @return the term.
     */
    String getTerm();

    /**
     * get the set of known predicates.
     * @return the set of known predicates.
     */
    Set<TokenPredicate> getKnownPredicates();

    /**
     * the set of possible predicates.
     * @return the set of possible predicates.
     */
    Set<TokenPredicate> getPossiblePredicates();

}
