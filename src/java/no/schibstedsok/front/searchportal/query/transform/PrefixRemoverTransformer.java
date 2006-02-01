package no.schibstedsok.front.searchportal.query.transform;

import no.schibstedsok.front.searchportal.query.*;
import no.schibstedsok.front.searchportal.query.token.RegExpEvaluatorFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import no.schibstedsok.front.searchportal.query.token.TokenPredicate;
import no.schibstedsok.front.searchportal.site.Site;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class PrefixRemoverTransformer extends AbstractQueryTransformer {

    private Collection prefixes = new ArrayList();

    public String getTransformedQuery(final Context cxt) {
        
        final String originalQuery = cxt.getQueryString();

        String result = originalQuery;
        
        for (Iterator iterator = prefixes.iterator(); iterator.hasNext();) {
            final TokenPredicate token = TokenPredicate.valueOf((String) iterator.next());
            final StopWordRemover remover = RegExpEvaluatorFactory.valueOf(cxt.getSite()).getStopWordRemover(token);
            result = remover.removeStopWords(result);
        }

        return result;
    }
}
