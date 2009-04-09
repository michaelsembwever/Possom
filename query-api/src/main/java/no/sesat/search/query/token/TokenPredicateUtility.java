/* Copyright (2008) Schibsted ASA
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

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class providing all useful static methods around TokenPredicates.
 *
 * @version $Id$
 */
public final class TokenPredicateUtility {

    private static final Map<String, TokenPredicate> ANONYMOUS_TOKENS = new Hashtable<String, TokenPredicate>();

    static {
        // ensures all the enums have been loaded before any of the following static methods are called.
        // offspin to this is that there can be no references back to Static from Categories or TokenPredicateImpl.
        Categories.values();
    }

    public static TokenPredicate getTokenPredicate(final String name) throws IllegalArgumentException {

        try {
            // the following conditional isn't neccessary (Categories checks such a case)
            //  but it's a reminder of how things should be done generally.
            return name.startsWith(TokenPredicate.EXACT_PREFIX)
                    ? Categories.valueOf(name.replaceFirst(TokenPredicate.EXACT_PREFIX, "")).exactPeer()
                    : Categories.valueOf(name);

        }catch (IllegalArgumentException iae) {
            return getAnonymousTokenPredicate(name);
        }
    }

    public static TokenPredicate getAnonymousTokenPredicate(final String name) throws IllegalArgumentException {


        if (!ANONYMOUS_TOKENS.containsKey(name.startsWith(TokenPredicate.EXACT_PREFIX)
                ? name.replaceFirst(TokenPredicate.EXACT_PREFIX, "")
                : name)) {

            throw new IllegalArgumentException("No anonymous token found with name " + name);
        }

        return name.startsWith(TokenPredicate.EXACT_PREFIX)
                ? ANONYMOUS_TOKENS.get(name.replaceFirst(TokenPredicate.EXACT_PREFIX, "")).exactPeer()
                : ANONYMOUS_TOKENS.get(name);
    }

    public static TokenPredicate createAnonymousTokenPredicate(final String name) {

        ANONYMOUS_TOKENS.put(name, new TokenPredicateImpl(name.startsWith(TokenPredicate.EXACT_PREFIX)
                ? name.replaceFirst(TokenPredicate.EXACT_PREFIX, "")
                : name));

        return getAnonymousTokenPredicate(name);
    }

    public static Set<TokenPredicate> getTokenPredicates() {

        return Collections.unmodifiableSet(TokenPredicateImpl.TOKENS);
    }
}
