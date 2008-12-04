/* Copyright (2005-2008) Schibsted Søk AS
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.apache.log4j.Logger;

/**
 * An implementation of TokenEvaluator which uses a set of {@link Pattern} to
 * decide if a token occurs in a query.
 * <b>Immutable</b>
 *
 *
 * @version $Id$
 */
public final class RegExpTokenEvaluator implements TokenEvaluator {

    private static final Logger LOG = Logger.getLogger(RegExpTokenEvaluator.class);

    private final Collection<Pattern> expressions = new ArrayList<Pattern>();
    private final boolean queryDependant;

    /**
     * Create a new RegExpTokenEvaluator.
     *
     * @param expressions
     *            the patterns to use. Elements of collection must be
     *            {@link Pattern}.
     * @param queryDependant
     */
    public RegExpTokenEvaluator(final Collection<Pattern> expressions, final boolean queryDependant) {
        this.expressions.addAll(expressions);
        this.queryDependant = queryDependant;
    }

    /**
     * Returns true if any of the patterns matches the query.
     *  Wraps to evaluateToken with exactMatchRequired == false
     * @param token
     *            not used by this implementation.
     * @param term
     *            the term currently parsing.
     * @param query
     *            the query to find matches in.
     *              can be null. this indicates we can just use the term.
     *
     * @return true if any of the patterns matches.
     */
    public boolean evaluateToken(final TokenPredicate token, final String term, final String query) {

        for (final Pattern p : expressions) {
            final Matcher m = term == null ? p.matcher(query) : p.matcher(term);
            final int stringLength = term == null ? query.length() : term.length();

            if(m.find()){
                if( term == null || (m.start() == 0 && m.end() == stringLength) ){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isQueryDependant(final TokenPredicate predicate) {
        return queryDependant;
    }

    public Set<String> getMatchValues(final TokenPredicate token, final String term) {
        return Collections.emptySet();
    }

}
