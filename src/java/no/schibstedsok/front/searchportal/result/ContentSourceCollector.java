package no.schibstedsok.front.searchportal.result;

import no.schibstedsok.front.searchportal.query.RunningQuery;

import java.util.*;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class ContentSourceCollector implements ResultHandler {

    public void handleResult(Context cxt, Map parameters) {
        
        RunningQuery query = cxt.getSearchResult().getSearchCommand().getQuery();
        if (cxt.getSearchResult().getHitCount() > 0) {
            FastSearchResult fastResult = (FastSearchResult) cxt.getSearchResult();
            if (fastResult.getModifiers("sources") != null) {
                for (Iterator iterator = fastResult.getModifiers("sources").iterator(); iterator.hasNext();) {
                    Modifier modifier = (Modifier) iterator.next();
                    query.addSource(modifier);
                }
            }
        }
    }
}