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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * The default implementation used. Should not be used directly.
 * The Categories enumerations delegated to this class.
 * The anonymous TokenPredicates are instances of this class.
 *
 * Must be unique by the name field.
 *
 * @version $Id$
 */
public class TokenPredicateImpl extends AbstractTokenPredicate {

    static final Set<TokenPredicate> TOKENS = new CopyOnWriteArraySet<TokenPredicate>();

    private final String name;

    private final ExactTokenPredicateImpl exactPeer;

    TokenPredicateImpl(final String name) {
        super();

        this.name = name;

        TOKENS.add(this);
        exactPeer = new ExactTokenPredicateImpl(this);
    }

    public String name() {
        return name;
    }

    public TokenPredicate exactPeer() {
        return exactPeer;
    }

}