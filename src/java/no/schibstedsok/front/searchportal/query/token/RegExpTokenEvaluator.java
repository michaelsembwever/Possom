// Copyright (2005-2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.token;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * An implementation of TokenEvaluator which uses a set of {@link Pattern} to
 * decide if a token occurs in a query.
 * <b>Immutable</b>
 *
 * @author <a href="magnus.eklund@sesam.no">Magnus Eklund</a>
 * @version $Revision$
 */
public final class RegExpTokenEvaluator implements TokenEvaluator {

    private final Collection<Pattern> expressions = new ArrayList<Pattern>();
    private final boolean queryDependant;

    /**
     * Create a new RegExpTokenEvaluator.
     *
     * @param expressions
     *            the patterns to use. Elements of collection must be
     *            {@link Pattern}.
     */
    public RegExpTokenEvaluator(final Collection<Pattern> expressions, final boolean queryDependant) {

        this.expressions.addAll(expressions);
        this.queryDependant = queryDependant;
    }

    /**
     * Returns true if any of the patterns matches the query.
     *
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
    public boolean evaluateToken(final String token, final String term, final String query) {

        for (Iterator iterator = expressions.iterator(); iterator.hasNext();) {
            final Pattern p = (Pattern) iterator.next();

            final Matcher m = term == null ? p.matcher(query) : p.matcher(term);

            if (m.find()) {
                return true;
            }
        }
        return false;
    }

    public boolean isQueryDependant() {
        return queryDependant;
    }
}
