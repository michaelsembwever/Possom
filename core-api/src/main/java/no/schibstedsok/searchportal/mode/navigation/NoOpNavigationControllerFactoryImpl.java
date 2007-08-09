/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 *
 * Jul 24, 2007 3:10:51 PM
 */
package no.schibstedsok.searchportal.mode.navigation;

import no.schibstedsok.searchportal.mode.NavigationConfig;
import no.schibstedsok.searchportal.result.NavigationItem;
import no.schibstedsok.searchportal.result.BasicNavigationItem;

/**
 * Default navigation controller. Does nothing.
 *  
 * @author <a href="mailto:magnus.eklund@sesam.no">Magnus Eklund</a>
 */
public class NoOpNavigationControllerFactoryImpl implements NavigationControllerFactory, NavigationController {
    public NavigationController get(final NavigationConfig.Nav nav) {
        return this;
    }

    public NavigationItem getNavigationItems(final Context context) {
        return new BasicNavigationItem();
    }
}
