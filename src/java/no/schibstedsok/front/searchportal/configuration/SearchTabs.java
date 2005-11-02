package no.schibstedsok.front.searchportal.configuration;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class SearchTabs {

    public Map searchModes = new HashMap();

    public void addMode(SearchMode mode) {
        searchModes.put(mode.getKey(), mode);
    }

    public SearchMode getSearchMode(String modeKey) {
        return (SearchMode) searchModes.get(modeKey);
    }

    public void stopAll() {
        for (Iterator iterator = searchModes.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            SearchMode mode = (SearchMode) searchModes.get(key);
            mode.getExecutor().stop();
        }
    }
}
