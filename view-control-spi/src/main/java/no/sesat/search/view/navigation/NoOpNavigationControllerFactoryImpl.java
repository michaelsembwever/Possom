/* Copyright (2005-2007) Schibsted ASA
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Jul 24, 2007 3:10:51 PM
 */
package no.sesat.search.view.navigation;

import no.sesat.search.result.BasicNavigationItem;
import no.sesat.search.result.NavigationItem;

/**
 * Default navigation controller. Does nothing.
 *
 *
 */
public class NoOpNavigationControllerFactoryImpl implements NavigationControllerFactory, NavigationController {
    public NavigationController get(final NavigationConfig.Nav nav) {
        return this;
    }

    public NavigationItem getNavigationItems(final Context context) {
        return new BasicNavigationItem();
    }
}
