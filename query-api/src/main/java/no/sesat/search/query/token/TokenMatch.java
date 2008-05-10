/*
 * Copyright (2005-2008) Schibsted Søk AS
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

import java.lang.ref.Reference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import no.sesat.commons.ref.ReferenceMap;

/** Used by VeryFastTokenEvaluator for matches against part of the query to a fast list.
 *
 * <b>Immutable</b>
 *
 *
 * @version $Id$
 **/
final class TokenMatch{

    // Constants -----------------------------------------------------

    private static final int WEAK_CACHE_INITIAL_CAPACITY = 2000;
    private static final float WEAK_CACHE_LOAD_FACTOR = 0.5f;
    private static final int WEAK_CACHE_CONCURRENCY_LEVEL = 16;

    private static final ReferenceMap<Integer,TokenMatch> WEAK_CACHE
            = new ReferenceMap<Integer,TokenMatch>(
                ReferenceMap.Type.WEAK,
                new ConcurrentHashMap<Integer,Reference<TokenMatch>>(
                    WEAK_CACHE_INITIAL_CAPACITY,
                    WEAK_CACHE_LOAD_FACTOR,
                    WEAK_CACHE_CONCURRENCY_LEVEL));

    // Static --------------------------------------------------------

    /** Hands out an instance given the 'constructing arguments'.
     * We use the flyweight pattern since instances are immutable.
     *
     * @param token
     * @param match
     * @param value
     * @return
     */
    public static TokenMatch instanceOf(
            final String token,
            final String match,
            final String value) {

        final int hashCode = computeHashCode(token, match, value);

        TokenMatch tm = WEAK_CACHE.get(hashCode);

        if(null == tm){
            tm = new TokenMatch(token, match, value);
            WEAK_CACHE.put(hashCode, tm);
        }

        return tm;
    }

    private static int computeHashCode(
            final String token,
            final String match,
            final String value) {

        int result = 17;
        result = 37*result + token.hashCode();
        result = 37*result + match.hashCode();
        result = 37*result + value.hashCode();
        return result;
    }

    // Attributes ----------------------------------------------------

    private final String token;
    private final String match;
    private final String value;
    private final Pattern matcher;

    // Constructors -------------------------------------------------

    private TokenMatch(final String token, final String match, final String value) {

        this.token = token;
        this.match = match;
        this.value = value;
        // (^|\s) or ($|\s) is neccessary to avoid matching fragments of words.
        matcher = Pattern.compile("(^|\\s)" + match + "($|\\s)", RegExpEvaluatorFactory.REG_EXP_OPTIONS);
    }

    // Public --------------------------------------------------------

    /**
     * Get the match.
     *
     * @return the match.
     */
    public String getMatch() {
        return match;
    }

    /**
     * Get the regular expression Matcher to use to find a sub-match.
     *
     * @param string
     * @return the match.
     */
    public Matcher getMatcher(final String string) {
        return matcher.matcher(string);
    }

    /**
     * Get the token.
     *
     * @return the token.
     */
    public String getToken() {
        return token;
    }

    /**
     * Get the Fast value.
     *
     * @return the value.
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
       return "token=\"" + token
               + "\"; match=\"" + match
               + "\"; value=" + (value == null ? "null" : "\"" + value + "\"")
               + "; matcher=" + matcher + ";";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TokenMatch && obj.hashCode() == hashCode();
    }

    @Override
    public int hashCode() {
        return computeHashCode(token, match, value);
    }


}
