/* Copyright (2005-2008) Schibsted Søk AS
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
package no.sesat.search.view.navigation.fast;

import no.sesat.search.view.navigation.NavigationController;
import no.sesat.search.result.NavigationItem;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import no.sesat.search.result.BasicNavigationItem;
import no.sesat.search.result.FastSearchResult;
import no.sesat.search.result.Modifier;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.search.SearchDataObject;
import no.sesat.search.datamodel.generic.StringDataObject;

import java.util.List;
import no.sesat.search.view.navigation.FastNavigationConfig;
import org.apache.log4j.Logger;

/**
 *
 */
public class FastNavigationController implements NavigationController {

    private static final Logger LOG = Logger.getLogger(FastNavigationController.class);

    private final String commandName;
    private final FastNavigationConfig nav;

    public FastNavigationController(final FastNavigationConfig nav) {
        this.commandName = nav.getCommandName();
        this.nav = nav;

        assert(this.commandName != null);
    }

    public NavigationItem getNavigationItems(final Context context) {

        final DataModel dataModel = context.getDataModel();
        final SearchDataObject search = dataModel.getSearch(commandName);

        final NavigationItem item = new BasicNavigationItem();

        if (search == null) {
            LOG.error("Search for '" + commandName + "' not found.");
            return item;
        }

        final ResultList<? extends ResultItem> searchResult = search.getResults();

        if (searchResult instanceof FastSearchResult) {

            final FastSearchResult fsr = (FastSearchResult) searchResult;

            final List<Modifier> modifiers = fsr.getModifiers(nav.getId());

            final int maxsize = nav.getMaxsize();

            final StringDataObject selectedValue = dataModel.getParameters().getValue(nav.getField());

            int sizeCounter = 0;

            if (modifiers != null && modifiers.size() > 0) {
                for (final Modifier modifier : modifiers) {
                    sizeCounter ++;
                    final String url = context.getUrlGenerator().getURL(modifier.getName(), nav);
                    final BasicNavigationItem i = new BasicNavigationItem(modifier.getName(), url, modifier.getCount());

                    if (selectedValue != null && selectedValue.getString().equals(i.getTitle())) {
                        i.setSelected(true);
                    }

                    // If an item is selected all other items on the same navigation level are excluded.
                    if (!nav.isExcludeOtherMatches() || selectedValue == null || i.isSelected()){
                        item.addResult(i);
                    }

                    if (sizeCounter == maxsize) {
                        break;
                    }
                }
            }

        }
        return item;
    }
}
