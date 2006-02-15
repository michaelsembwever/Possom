// Copyright (2005-2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.query.token;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import no.schibstedsok.front.searchportal.query.run.StopWordRemover;

/**
 * An implementation of TokenEvaluator which uses a set of {@link Pattern} to
 * decide if a token occurs in a query.
 * <b>Immutable</b>
 *
 * @author <a href="magnus.eklund@sesam.no">Magnus Eklund</a>
 * @version $Revision$
 */
public final class RegExpTokenEvaluator implements TokenEvaluator, StopWordRemover {

    private final Collection/*<Pattern>*/ expressions = new ArrayList/*<Pattern>*/();
    private final boolean queryDependant;

    /**
     * Create a new RegExpTokenEvaluator.
     *
     * @param expressions
     *            the patterns to use. Elements of collection must be
     *            {@link Pattern}.
     */
    public RegExpTokenEvaluator(final Collection/*<Pattern>*/ expressions, final boolean queryDependant) {

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

    /**
     * Remove all substrings from originalQuery that matches any of the
     * expressions.
     *
     * @param originalQuery
     *            the query to remove stop words form.
     *
     * @return  the query with all matches removed.
     * @todo    does not belong in this class.
     */
    public String removeStopWords(final String originalQuery) {

        // [FIXME] we really only want to avoid removing StopWords *inside* the quotes. Not altogether.
        if (originalQuery.indexOf('"') > -1) {
            return originalQuery;
        }

        String newQuery = originalQuery;

        for (Iterator iterator = expressions.iterator(); iterator.hasNext();) {
            Pattern p = (Pattern) iterator.next();
            Matcher m = p.matcher(newQuery);
            newQuery = m.replaceFirst("");
        }

        // FIXME    
        if (newQuery.equals("")  || originalQuery.equalsIgnoreCase("nyheter") || originalQuery.equalsIgnoreCase("tv") || originalQuery.matches("p.* tv idag")) {
            return originalQuery;
        } else {
            return newQuery;
        }
    }

    public boolean isQueryDependant() {
        return queryDependant;
    }
}
