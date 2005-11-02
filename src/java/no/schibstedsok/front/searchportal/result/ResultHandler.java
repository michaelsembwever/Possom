package no.schibstedsok.front.searchportal.result;

import java.util.Map;

/*
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 *
 */

public interface ResultHandler {
    void handleResult(SearchResult result, Map parameters);
}
