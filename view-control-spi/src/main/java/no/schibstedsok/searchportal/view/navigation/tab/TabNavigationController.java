/* Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 *
 * Jul 20, 2007 3:03:13 PM
 */
package no.schibstedsok.searchportal.view.navigation.tab;

import no.schibstedsok.searchportal.view.navigation.TabNavigationConfig;
import no.schibstedsok.searchportal.view.navigation.NavigationController;
import no.schibstedsok.searchportal.result.NavigationItem;
import no.schibstedsok.searchportal.result.BasicNavigationItem;
import no.schibstedsok.searchportal.datamodel.DataModel;

import java.util.List;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.datamodel.search.SearchDataObject;

/**
 * TODO: Move into sesat-search-command-control-spi
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
        item.setHitCount(0);
        for(String commandName : commandNames){
            
            final SearchDataObject searchDO = dataModel.getSearch(commandName);
            if(null != searchDO){
                item.setHitCount(item.getHitCount() + searchDO.getResults().getHitCount());            
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
