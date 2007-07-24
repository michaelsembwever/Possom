/* Copyright (2005-2007) Schibsted Søk AS
*
* Jul 20, 2007 11:18:34 AM
*/
package no.schibstedsok.searchportal.mode.navigation;

import no.schibstedsok.searchportal.mode.NavigationConfig;


/**
 * TODO: Move into sesat-search-command-control-spi once that module is ready for action.
 */
public interface NavigationControllerFactory<T extends NavigationConfig.Nav> {
    NavigationController get(T nav);
}
