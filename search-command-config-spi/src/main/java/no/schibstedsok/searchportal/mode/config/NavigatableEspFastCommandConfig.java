// Copyright (2007) Schibsted Søk AS
/*
 * NavigatableESPFastCommandConfig.java
 *
 * Created on August 20, 2006, 6:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package no.schibstedsok.searchportal.mode.config;

import java.util.HashMap;
import java.util.Map;
import no.schibstedsok.searchportal.mode.config.CommandConfig.Controller;
import no.schibstedsok.searchportal.result.Navigator;

/**
 *
 * @author maek
 * @version $Id$
 */
@Controller("NavigatableESPFastCommand")
public class NavigatableEspFastCommandConfig extends EspFastCommandConfig {

    final Map<String, Navigator> navigators = new HashMap<String, Navigator>();

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
