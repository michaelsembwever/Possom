/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 *
 * Jul 20, 2007 2:23:11 PM
 */
package no.sesat.search.view.navigation.tab;

import no.sesat.search.view.navigation.NavigationControllerFactory;
import no.sesat.search.view.navigation.NavigationController;
import no.sesat.search.view.navigation.TabNavigationConfig;

/**
 * TODO: Move into sesat-search-command-control-spi once that module is ready for action.
 * 
 */
public final class TabNavigationControllerFactory implements NavigationControllerFactory<TabNavigationConfig> {
    public NavigationController get(final TabNavigationConfig nav) {
        return new TabNavigationController(nav);
    }

}
