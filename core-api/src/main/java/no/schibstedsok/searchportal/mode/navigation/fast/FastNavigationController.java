/* Copyright (2005-2007) Schibsted Søk AS
 *
 * Jul 20, 2007 3:03:13 PM
 */
package no.schibstedsok.searchportal.mode.navigation.fast;

import no.schibstedsok.searchportal.mode.navigation.NavigationController;
import no.schibstedsok.searchportal.mode.navigation.FastNavigationConfig;
import no.schibstedsok.searchportal.mode.NavigationConfig;
import no.schibstedsok.searchportal.result.NavigationItem;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import no.schibstedsok.searchportal.result.BasicNavigationItem;
import no.schibstedsok.searchportal.result.NavigationHelper;
import no.schibstedsok.searchportal.result.FastSearchResult;
import no.schibstedsok.searchportal.result.Modifier;
import no.schibstedsok.searchportal.datamodel.DataModel;

import java.util.List;

/**
 * TODO: Move into sesat-search-command-control-spi
 * @author <a href="mailto:magnus.eklund@sesam.no">Magnus Eklund</a>
 */
public class FastNavigationController implements NavigationController {

    private final String commandName;
    private final FastNavigationConfig nav;

    public FastNavigationController(final FastNavigationConfig nav) {
        this.commandName = nav.getCommandName();
        this.nav = nav;

        assert(this.commandName != null);
    }

    public NavigationItem getNavigationItems(final DataModel dataModel) {
        final ResultList<? extends ResultItem> searchResult = dataModel.getSearch(commandName).getResults();

        final NavigationItem item = new BasicNavigationItem();

        if (searchResult instanceof FastSearchResult) {
            
            final FastSearchResult fsr = (FastSearchResult) searchResult;

            final List<Modifier> modifiers = fsr.getModifiers(nav.getId());


            if (modifiers != null && modifiers.size() > 0) {
                for (final Modifier modifier : modifiers) {
                    final String navigatorName = modifier.getNavigator() == null ? null : modifier.getNavigator().getName();
                    final String url = NavigationHelper.getUrlFragment(dataModel, nav, modifier.getName(), navigatorName);
                    item.addResult(new BasicNavigationItem(modifier.getName(), url, modifier.getCount()));
                }
            }

        }
        return item;
    }
}
