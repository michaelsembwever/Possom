/** Copyright (2007-2008) Schibsted ASA
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
**/
package no.sesat.search.view.navigation;

import org.apache.log4j.Logger;
import no.sesat.search.result.*;
import no.sesat.search.datamodel.DataModel;
import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.datamodel.search.SearchDataObject;

import java.util.List;


/**
 * @version $Id$
 */
public class TrimedNavigationController implements NavigationController, NavigationControllerFactory<TrimedNavigationConfig> {

    private static final Logger LOG = Logger.getLogger(TrimedNavigationController.class);


    private TrimedNavigationConfig nav;

   /* public TrimedNavigationController(final TrimedNavigationConfig nav) {
        this.commandName = nav.getCommandName();
        this.nav = nav;

        assert(this.commandName != null);
    } */

    public NavigationItem getNavigationItems(final Context context) {

        final DataModel dataModel = context.getDataModel();
        final SearchDataObject search = dataModel.getSearch(nav.getCommandName());

        final NavigationItem item = new BasicNavigationItem();

        if (search == null) {
            LOG.error("Search for " + nav.getCommandName() + "not found.");
            return item;
        }

        final ResultList<ResultItem> searchResult = search.getResults();

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
                    final BasicNavigationItem i = new BasicNavigationItem(trimString(modifier.getName()), url, modifier.getCount());

                    if (selectedValue != null && selectedValue.getString().equals(i.getTitle())) {
                        i.setSelected(true);
                    }

                    item.addResult(i);
                    if (sizeCounter == maxsize) {
                        break;
                    }
                }
            }

        }
        return item;
    }

    public NavigationController get(final TrimedNavigationConfig nav) {
       this.nav = nav;
       return this;
    }

    private String trimString(String toBeTrimmed){
        String originalString = toBeTrimmed;
        int index = toBeTrimmed.lastIndexOf(nav.getSeparator());
         // trim away trailing separator if it exists
        if(index == toBeTrimmed.length() -1)
        {
           originalString = originalString.substring(0,originalString.length() -1);
           index = originalString.lastIndexOf(nav.getSeparator());
        }
        if(index == -1)
        {
           return originalString;
        }
        else{
            return originalString.substring(index+nav.getSeparator().length(),originalString.length());
        }
    }
}
