/* Copyright (2005-2007) Schibsted Søk AS
 *
 * Jul 20, 2007 2:23:11 PM
 */
package no.schibstedsok.searchportal.mode.navigation.fast;

import no.schibstedsok.searchportal.mode.navigation.NavigationControllerFactory;
import no.schibstedsok.searchportal.mode.navigation.NavigationController;
import no.schibstedsok.searchportal.mode.navigation.FastNavigationConfig;

/**
 * TODO: Move into sesat-search-command-control-spi once that module is ready for action.
 * 
 * @author <a href="mailto:magnus.eklund@sesam.no">Magnus Eklund</a>
 */
public final class FastNavigationControllerFactory implements NavigationControllerFactory<FastNavigationConfig> {
    public NavigationController get(final FastNavigationConfig nav) {
        return new FastNavigationController(nav);
    }

}
