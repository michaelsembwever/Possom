/*
 * NavigatableAdvancedFastConfiguration.java
 *
 * Created on August 20, 2006, 6:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.mode.config;

import java.util.HashMap;
import java.util.Map;
import no.schibstedsok.searchportal.result.Navigator;

/**
 *
 * @author maek
 */
public class NavigatableAdvancedFastConfiguration extends AdvancedFastSearchConfiguration {

    final Map<String, Navigator> navigators = new HashMap<String, Navigator>();
    
    public NavigatableAdvancedFastConfiguration(final SearchConfiguration asc){
        super(asc);
    }

    public Map<String, Navigator> getNavigators() {
        return navigators;
    }
    public void addNavigator(final Navigator navigator, final String navKey) {
        navigators.put(navKey, navigator);
    }

    public Navigator getNavigator(final String navigatorKey) {
        return navigators.get(navigatorKey);
    }
}
