/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.

 */
package no.sesat.search.result.handler;

import no.sesat.search.datamodel.DataModel;
import no.sesat.search.mode.config.SearchConfiguration;
import no.sesat.search.query.QueryContext;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import no.sesat.search.site.SiteContext;
import no.sesat.search.site.config.ResourceContext;
import no.sesat.search.view.config.SearchTab;

/**
 * @version <tt>$Id$</tt>
 * @author <a href="mailto:magnus.eklund@gmail.com">Magnus Eklund</a>
 *
 */
public interface ResultHandler {

    /**
     * Contextual demands from a ResultHandler.
     * Slightly unusual in that the context never becomes a member field but is only used inside the
     * handleResult method.
     */
    public interface Context extends ResourceContext, QueryContext, SiteContext {

        /**
         *
         * @return
         */
        ResultList<ResultItem> getSearchResult();

        /**
         *
         * @return
         */
        SearchTab getSearchTab();

        /**
         *
         * @return
         */
        SearchConfiguration getSearchConfiguration();

        /**
         * Returns the query as it is after the query transformers have been applied to it.
         *
         * @return
         */
        String getDisplayQuery();
    }

    /**
     *
     * @param cxt
     * @param datamodel
     */
    void handleResult(Context cxt, DataModel datamodel);
}
