/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 *
 * Jul 20, 2007 2:23:11 PM
 */
package no.schibstedsok.searchportal.view.navigation.tab;

import no.schibstedsok.searchportal.view.navigation.NavigationControllerFactory;
import no.schibstedsok.searchportal.view.navigation.NavigationController;
import no.schibstedsok.searchportal.view.navigation.TabNavigationConfig;

/**
 * TODO: Move into sesat-search-command-control-spi once that module is ready for action.
 * 
 */
public final class TabNavigationControllerFactory implements NavigationControllerFactory<TabNavigationConfig> {
    public NavigationController get(final TabNavigationConfig nav) {
        return new TabNavigationController(nav);
    }

}
