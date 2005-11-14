package no.schibstedsok.front.searchportal.query;

import no.schibstedsok.front.searchportal.analyzer.RegExpEvaluators;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class PrefixRemoverTransformer extends AbstractQueryTransformer {

    private Collection prefixes = new ArrayList();

    public String getTransformedQuery(String originalQuery) {
        for (Iterator iterator = prefixes.iterator(); iterator.hasNext();) {
            String name = (String) iterator.next();
            StopWordRemover remover = RegExpEvaluators.getStopWordRemover(name);
            originalQuery = remover.removeStopWords(originalQuery);
        }

        return originalQuery;
    }
}
