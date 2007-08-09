/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License

 */
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
