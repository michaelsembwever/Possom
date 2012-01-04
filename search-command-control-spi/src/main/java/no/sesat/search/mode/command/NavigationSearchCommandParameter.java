/*
* Copyright (2012) Schibsted ASA
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
*/
package no.sesat.search.mode.command;

import no.sesat.search.datamodel.navigation.NavigationDataObject;
import no.sesat.search.view.navigation.NavigationConfig.Nav;

/** Implementation that handles parameters that are related to the navigation configuration.
 *
 * @see #isActive()
 *
 * @version $Id$
 */
class NavigationSearchCommandParameter extends BaseSearchCommandParameter {

    // Static --------------------------------------------------------
    // Attributes ----------------------------------------------------

    private final String navigationMapKey;

    private final NavigationDataObject navDO;

    // Constructors --------------------------------------------------
    // Public --------------------------------------------------------

    public NavigationSearchCommandParameter(
            final SearchCommand.Context context,
            final String name,
            final String navigationMapKey,
            final Origin... lookupOrder) {

        super(context, name, lookupOrder);
        this.navigationMapKey = navigationMapKey;
        this.navDO = context.getDataModel().getNavigation();
    }

    /** {@inheritDoc}
     *
     * Returns true if there exists from the navigation configuration
     * a Nav in the NavMap with the key "navigationMapKey"
     * whos commandName is equal to the current command's name.
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean isActive() {

        final boolean navMapExists = null != navDO && null != navDO.getConfiguration();

        final Nav nav = navMapExists
                ? navDO.getConfiguration().getNavMap().get(navigationMapKey)
                : null;

        return null != nav && getContext().getSearchConfiguration().getId().equals(nav.getCommandName());
    }

    // Package protected ---------------------------------------------
    // Protected -----------------------------------------------------
    // Private -------------------------------------------------------

}

