/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 *
 *   SESAT is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SESAT is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SESAT.  If not, see <http://www.gnu.org/licenses/>.
*
* Jul 20, 2007 11:20:16 AM
*/
package no.sesat.search.view.navigation;

import no.sesat.search.result.NavigationItem;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.DataModelContext;
import no.sesat.search.site.config.ResourceContext;
import no.sesat.search.site.SiteContext;

/**
 * TODO: Move into sesat-search-command-control-spi once that module is ready for action.
 */
public interface NavigationController {

    interface Context extends DataModelContext, ResourceContext, SiteContext {
    }

    NavigationItem getNavigationItems(Context context);
}
