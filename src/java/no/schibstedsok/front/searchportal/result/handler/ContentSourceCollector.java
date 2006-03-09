// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result.handler;

import java.util.Iterator;
import java.util.Map;
import no.schibstedsok.front.searchportal.result.FastSearchResult;
import no.schibstedsok.front.searchportal.result.Modifier;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class ContentSourceCollector implements ResultHandler {

    public void handleResult(final Context cxt, final Map parameters) {

        if (cxt.getSearchResult().getHitCount() >= 0) {

            final FastSearchResult fastResult = (FastSearchResult) cxt.getSearchResult();

            if (fastResult.getModifiers("sources") != null) {
                for (Iterator iterator = fastResult.getModifiers("sources").iterator(); iterator.hasNext();) {
                    final Modifier modifier = (Modifier) iterator.next();
                    cxt.addSource(modifier);
                }
            }
        }
    }
}