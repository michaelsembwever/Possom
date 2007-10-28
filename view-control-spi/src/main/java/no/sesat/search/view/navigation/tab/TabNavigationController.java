/* Copyright (2005-2007) Schibsted Søk AS
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
 * Jul 20, 2007 3:03:13 PM
 */
package no.sesat.search.view.navigation.tab;

import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.datamodel.search.SearchDataObject;
import no.sesat.search.result.BasicNavigationItem;
import no.sesat.search.result.NavigationItem;
import no.sesat.search.view.navigation.NavigationController;
import no.sesat.search.view.navigation.TabNavigationConfig;

import java.util.List;

/** The NavigationController for cross-vertical navigation.
 * Allowed to map to multiple commands and is selected if any of those commands are current.
 * @version $Id$
 */
public final class TabNavigationController implements NavigationController {

    private final List<String> commandNames;
    private final TabNavigationConfig nav;

    public TabNavigationController(final TabNavigationConfig nav) {
        
        commandNames = nav.getCommandNames();
        this.nav = nav;

        assert(null != commandNames);
    }

    public NavigationItem getNavigationItems(final Context context) {

        final DataModel dataModel = context.getDataModel();
        final NavigationItem item = new BasicNavigationItem();
        
        // hitcount
        item.setHitCount(-1);
        
        for(String commandName : commandNames){
            
            final SearchDataObject searchDO = dataModel.getSearch(commandName);
            if(null != searchDO){
                
                final int hitCount = searchDO.getResults().getHitCount();
                
                // do not accumulate "-1" hitcounts
                item.setHitCount( -1 == item.getHitCount() && -1 == hitCount
                        ? -1
                        : Math.max(0, item.getHitCount()) + Math.max(0, hitCount));
            }
        }
        
        // selected
        final StringDataObject selectedValue = dataModel.getParameters().getValue(nav.getField());
        if (null != selectedValue && nav.getValues().contains(selectedValue.getString())) {
            item.setSelected(true);
        }
        
        return item;
    }
}
