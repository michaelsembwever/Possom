// Copyright (2006-2007) Schibsted Søk AS
package no.schibstedsok.searchportal.view.output;

import java.util.Iterator;
import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.handler.ResultHandler;
import org.apache.log4j.Logger;

/** TODO rename to DebugOutputResultHandler
 *
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public final class TextOutputResultHandler implements ResultHandler {
    
    private static final Logger LOG = Logger.getLogger(TextOutputResultHandler.class);;

    public void handleResult(final Context cxt, final DataModel datamodel) {
        LOG.info("--- --- --- ---");

        for (ResultItem basicSearchResultItem : cxt.getSearchResult().getResults()) {

            for (String name : basicSearchResultItem.getFieldNames()) {
                LOG.info(name + " => " + basicSearchResultItem.getField(name));
            }

            LOG.info("--- --- --- ---");
        }
    }
}
