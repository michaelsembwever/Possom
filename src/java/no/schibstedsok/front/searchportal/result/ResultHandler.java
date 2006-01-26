package no.schibstedsok.front.searchportal.result;

import java.util.Map;
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
    public interface Context extends SiteContext{
        SearchResult getSearchResult();
    }
    
    void handleResult(Context cxt, Map parameters);
}
