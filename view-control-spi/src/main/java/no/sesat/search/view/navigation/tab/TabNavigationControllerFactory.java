/* Copyright (2005-2012) Schibsted ASA
 * This file is part of Possom.
 *
 *   Possom is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Possom is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Possom.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Jul 20, 2007 2:23:11 PM
 */
package no.sesat.search.view.navigation.tab;

import no.sesat.search.view.navigation.NavigationController;
import no.sesat.search.view.navigation.NavigationControllerFactory;
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
