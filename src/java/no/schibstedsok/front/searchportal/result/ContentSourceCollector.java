package no.schibstedsok.front.searchportal.result;

import no.schibstedsok.front.searchportal.query.RunningQuery;

import java.util.*;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class ContentSourceCollector implements ResultHandler {

    public void handleResult(SearchResult result, Map parameters) {
        RunningQuery query = result.getSearchCommand().getQuery();
        if (result.getHitCount() > 0) {
            FastSearchResult fastResult = (FastSearchResult) result;
            if (fastResult.getModifiers("sources") != null) {
                for (Iterator iterator = fastResult.getModifiers("sources").iterator(); iterator.hasNext();) {
                    Modifier modifier = (Modifier) iterator.next();
                    query.addSource(modifier);
                }
            }
        }
    }
}