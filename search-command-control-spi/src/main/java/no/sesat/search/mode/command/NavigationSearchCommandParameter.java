/*
* Copyright (2008) Schibsted Søk AS
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
*/
package no.sesat.search.mode.command;

import no.sesat.search.view.navigation.NavigationConfig.Nav;

/** Implementation that handles parameters that are related to the navigation configuration.
 *
 * @see #isActive()
 *
 * @version $Id$
 */
class NavigationSearchCommandParameter extends BaseSearchCommandParameter {

    private final String navigationMapKey;

    public NavigationSearchCommandParameter(
            final SearchCommand.Context context,
            final String name,
            final String navigationMapKey,
            final Origin... lookupOrder) {

        super(context, name, lookupOrder);
        this.navigationMapKey = navigationMapKey;
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

        final boolean navMapExists =
                null != getContext().getDataModel().getNavigation()
                && null != getContext().getDataModel().getNavigation().getConfiguration();

        final Nav nav = navMapExists
                ? getContext().getDataModel().getNavigation().getConfiguration().getNavMap().get(navigationMapKey)
                : null;

        return null != nav && getContext().getSearchConfiguration().getId().equals(nav.getCommandName());
    }
}

