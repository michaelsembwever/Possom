/* Copyright (2005-2007) Schibsted Søk AS
 *
 * Jul 20, 2007 3:03:13 PM
 */
package no.schibstedsok.searchportal.mode.navigation.tab;

import no.schibstedsok.searchportal.mode.navigation.NavigationController;
import no.schibstedsok.searchportal.result.NavigationItem;
import no.schibstedsok.searchportal.result.BasicNavigationItem;
import no.schibstedsok.searchportal.datamodel.DataModel;

import java.util.List;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.datamodel.search.SearchDataObject;
import no.schibstedsok.searchportal.mode.navigation.TabNavigationConfig;

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
