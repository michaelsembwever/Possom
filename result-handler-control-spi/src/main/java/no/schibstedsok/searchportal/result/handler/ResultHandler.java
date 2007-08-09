/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License

 */
package no.schibstedsok.searchportal.result.handler;

import no.schibstedsok.searchportal.datamodel.DataModel;
import no.schibstedsok.searchportal.mode.config.SearchConfiguration;
import no.schibstedsok.searchportal.query.QueryContext;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import no.schibstedsok.searchportal.site.SiteContext;
import no.schibstedsok.searchportal.site.config.ResourceContext;
import no.schibstedsok.searchportal.view.config.SearchTab;

/**
 * @version <tt>$Id$</tt>
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
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
