/* Copyright (2007-2008) Schibsted Søk AS
 *   This file is part of SESAT.
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
 * NavigationHelper.java
 *
 * Created on 12/06/2007, 17:13:43
 *
 */

package no.sesat.search.view.navigation;

import no.sesat.search.datamodel.DataModel;
import no.sesat.search.result.BasicNavigationItem;
import no.sesat.search.result.NavigationItem;

/**
 * All public methods require take a datamodel argument. This datamodel must in VIEW_CONSTRUCTION state.
 * It essentially means that this helper class can only be used from jsp/velocity code.
 *
 *
 *
 * @version $Id$
 *
 */
public final class NavigationHelper {

    // Constants -----------------------------------------------------
    public static String removeQuery(final String url) {
        return url.replaceAll("(&amp;)?q=[^&]*", "");
    }

    /**
     * @todo move into domain? (NavigationItem & BasicNavigationItem)
     */
    public static NavigationConfig.Nav getFirstNotSelected(DataModel dm, NavigationConfig.Nav nav) {

        if (dm.getParameters().getValue(nav.getId()) != null
                && !nav.getChildNavs().isEmpty()
                && !nav.getChildNavs().get(0).isVirtual()) {

            return getFirstNotSelected(dm, nav.getChildNavs().get(0));

        } else {

            final int navResultSize = null != nav.getId()
                    && null != dm.getNavigation().getNavigation(nav.getId())
                    && null != dm.getNavigation().getNavigation(nav.getId()).getResults()
                    ? dm.getNavigation().getNavigation(nav.getId()).getResults().size()
                    : 0;

// TODO: Specification is a mess, so this becomes ugly. See history in prio-198 & SEARCH-3320.
// TODO: Haven't found a general way to solve this. Special case for Oslo.
// TODO: New JIRA created to resolve this: SEARCH-3451
//            return 1 == navResultSize && !nav.getChildNavs().isEmpty()
//                    ? getFirstNotSelected(dm, nav.getChildNavs().get(0))
//                    : nav;


            return 1 == navResultSize && !nav.getChildNavs().isEmpty() && (nav.isAutoNavigation() || isOslo(dm, nav))
                    ? getFirstNotSelected(dm, nav.getChildNavs().get(0))
                    : nav;
        }
    }

    public static NavigationItem getSingleNavigationItem(DataModel dm, final String navId, final String value) {
        final NavigationItem item = dm.getNavigation().getNavigation(navId);

        if (item != null && item.getChildByTitle(value) != null) {

            return item.getChildByTitle(value);
        } else {
            final BasicNavigationItem navigationItem = new BasicNavigationItem();

            navigationItem.setHitCount(0);
            navigationItem.setTitle(value);

            return navigationItem;
        }
    }

    public String getResetUrl(final DataModel dm, final String navId) {
        return dm.getNavigation().getNavigation("reset_" + navId).getUrl();
    }

    /** Checks if navigation is "oslo".
     *
     * @deprecated {@link http://sesat.no/scarab/issues/id/SKER5009} A general solution is required here
     *  that everybody can benefit from.
     *  Or documentation of how to avoid this in the index.
     *
     * @param dm datamodel
     * @param nav navigation to check
     * @return true if title == "oslo"
     */
    private static boolean isOslo(DataModel dm, NavigationConfig.Nav nav) {
        return dm.getNavigation().getNavigation(nav.getId()).getResults().get(0).getTitle().equalsIgnoreCase("oslo");
    }
}
