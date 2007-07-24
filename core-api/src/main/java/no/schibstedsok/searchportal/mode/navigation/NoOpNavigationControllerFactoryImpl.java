/* Copyright (2005-2007) Schibsted Søk AS
 *
 * Jul 24, 2007 3:10:51 PM
 */
package no.schibstedsok.searchportal.mode.navigation;

import no.schibstedsok.searchportal.mode.NavigationConfig;
import no.schibstedsok.searchportal.result.NavigationItem;
import no.schibstedsok.searchportal.result.BasicNavigationItem;
import no.schibstedsok.searchportal.datamodel.DataModel;

public class NoOpNavigationControllerFactoryImpl implements NavigationControllerFactory, NavigationController {
    public NavigationController get(final NavigationConfig.Nav nav) {
        return this;
    }

    public NavigationItem getNavigationItems(final DataModel dataModel, final String name) {
        return new BasicNavigationItem();
    }
}
