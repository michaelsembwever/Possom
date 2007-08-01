/* Copyright (2005-2007) Schibsted Søk AS
 *
 * Jul 20, 2007 2:23:11 PM
 */
package no.schibstedsok.searchportal.mode.navigation.tab;

import no.schibstedsok.searchportal.mode.navigation.NavigationControllerFactory;
import no.schibstedsok.searchportal.mode.navigation.NavigationController;
import no.schibstedsok.searchportal.mode.navigation.TabNavigationConfig;

/**
 * TODO: Move into sesat-search-command-control-spi once that module is ready for action.
 * 
 */
public final class TabNavigationControllerFactory implements NavigationControllerFactory<TabNavigationConfig> {
    public NavigationController get(final TabNavigationConfig nav) {
        return new TabNavigationController(nav);
    }

}
