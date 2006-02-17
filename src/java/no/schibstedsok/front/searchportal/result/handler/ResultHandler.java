// Copyright (2006) Schibsted Søk AS
package no.schibstedsok.front.searchportal.result.handler;

import java.util.Map;
import no.schibstedsok.front.searchportal.configuration.loader.ResourceContext;
import no.schibstedsok.front.searchportal.query.QueryContext;
import no.schibstedsok.front.searchportal.query.QueryStringContext;
import no.schibstedsok.front.searchportal.result.Modifier;
import no.schibstedsok.front.searchportal.result.SearchResult;
import no.schibstedsok.front.searchportal.site.SiteContext;

/*
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 *
 */
public interface ResultHandler {
    /** Contextual demands from a ResultHandler.
     * Slightly unusual in that the context never becomes a member field but is only used inside the
     * handleResult method.
     */
    public interface Context extends SiteContext, QueryContext, QueryStringContext, ResourceContext {
        SearchResult getSearchResult();

        /** @deprecated implementations should be using the QueryContext instead! */
        String getQueryString();

        /** Result handling action **/
        void addSource(Modifier modifier);
    }

    void handleResult(Context cxt, Map parameters);
}
