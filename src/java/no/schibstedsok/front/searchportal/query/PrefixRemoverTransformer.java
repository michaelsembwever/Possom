package no.schibstedsok.front.searchportal.query;

import no.schibstedsok.front.searchportal.analyzer.RegExpEvaluators;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import no.schibstedsok.front.searchportal.analyzer.TokenPredicate;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class PrefixRemoverTransformer extends AbstractQueryTransformer {

    private Collection prefixes = new ArrayList();

    public String getTransformedQuery(final String originalQuery) {
        String result = originalQuery;
        
        for (Iterator iterator = prefixes.iterator(); iterator.hasNext();) {
            final TokenPredicate token = TokenPredicate.valueOf((String) iterator.next());
            final StopWordRemover remover = RegExpEvaluators.getStopWordRemover(token);
            result = remover.removeStopWords(result);
        }

        return result;
    }
}
