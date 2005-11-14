package no.schibstedsok.front.searchportal.analyzer;

import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import no.schibstedsok.front.searchportal.query.StopWordRemover;

/**
 * An implementation of TokenEvaluator which uses a set of {@link Pattern} to
 * decide if a token occurs in a query.
 *
 * @author <a href="magnus.eklund@sesam.no">Magnus Eklund</a>
 * @version $Revision$
 */
public class RegExpTokenEvaluator implements TokenEvaluator, StopWordRemover {

    private Collection expressions;

    /**
     * Create a new RegExpTokenEvaluator.
     *
     * @param expressions
     *            the patterns to use. Elements of collection must be
     *            {@link Pattern}.
     */
    public RegExpTokenEvaluator(final Collection expressions) {
        this.expressions = expressions;
    }

    /**
     * Returns true if any of the patterns matches the query.
     *
     * @param token
     *            not used by this implementation.
     * @param query
     *            the query to find matches in.
     *
     * @return true if any of the patterns matches.
     */
    public boolean evaluateToken(final String token, final String query) {
        for (Iterator iterator = expressions.iterator(); iterator.hasNext();) {
            Pattern p = (Pattern) iterator.next();

            Matcher m = p.matcher(query);

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

        if (originalQuery.indexOf('"') > -1) {
            return originalQuery;
        }

        String newQuery = originalQuery;

        for (Iterator iterator = expressions.iterator(); iterator.hasNext();) {
            Pattern p = (Pattern) iterator.next();
            Matcher m = p.matcher(newQuery);
            newQuery = m.replaceAll("");
        }

        return newQuery;
    }
}
