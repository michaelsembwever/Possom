// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.output;

import java.util.Map;
import java.util.Iterator;
import no.schibstedsok.front.searchportal.result.BasicSearchResultItem;
import no.schibstedsok.front.searchportal.result.handler.ResultHandler;
import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class TextOutputResultHandler implements ResultHandler {
    
    private static final Logger LOG = Logger.getLogger(TextOutputResultHandler.class);;

    public void handleResult(final Context cxt, final Map parameters) {
        LOG.info("--- --- --- ---");

        for (Iterator iterator = cxt.getSearchResult().getResults().iterator(); iterator.hasNext();) {
            final BasicSearchResultItem basicSearchResultItem = (BasicSearchResultItem) iterator.next();

            for (Iterator iterator1 = basicSearchResultItem.getFieldNames().iterator(); iterator1.hasNext();) {
                final String name =  (String) iterator1.next();
                LOG.info(name + " => " + basicSearchResultItem.getField(name));
            }

            LOG.info("--- --- --- ---");
        }
    }
}
