/* Copyright (2006-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.

 */
package no.sesat.search.view.output;

import java.util.Iterator;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.handler.ResultHandler;
import org.apache.log4j.Logger;

/** TODO rename to DebugOutputResultHandler
 *
 *
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
