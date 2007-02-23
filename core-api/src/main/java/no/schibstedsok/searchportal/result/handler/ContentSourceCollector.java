// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.result.handler;

import java.util.List;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.Modifier;


/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class ContentSourceCollector implements ResultHandler {

    public void handleResult(final Context cxt, final DataModel datamodel) {

        if (cxt.getSearchResult().getHitCount() >= 0) {

            final FastSearchResult fastResult = (FastSearchResult) cxt.getSearchResult();

            if (fastResult.getModifiers("sources") != null) {
                for (Modifier modifier : (List<Modifier>)fastResult.getModifiers("sources")) {
                    cxt.addSource(modifier);
                }
            }
        }
    }
}