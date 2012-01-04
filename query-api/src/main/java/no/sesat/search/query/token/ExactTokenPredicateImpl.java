/* Copyright (2012) Schibsted ASA
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
package no.sesat.search.query.token;

/**
 * An token predicate peer that only evaluates to true against its original token predicate on exact query matches.
 * Must be unique by the name field.
 *
 * @version $Id$
 */
public final class ExactTokenPredicateImpl extends AbstractTokenPredicate{

    // Attributes -----------------------------------------------------

    private final TokenPredicate delegate;

    // Constructors -----------------------------------------------------

    ExactTokenPredicateImpl(final TokenPredicate token){
        delegate = token;
    }

    // TokenPredicate implementation ------------------------------------

    public String name() {
        return EXACT_PREFIX + delegate.name();
    }

    public TokenPredicate exactPeer() {
        return this;
    }

    // private -----------------------------------------------------
}
