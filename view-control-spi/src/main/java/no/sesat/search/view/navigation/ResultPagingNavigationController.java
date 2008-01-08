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
 * Jul 26, 2007 9:19:47 AM
 */
package no.sesat.search.view.navigation;

import no.sesat.search.datamodel.generic.StringDataObject;
import no.sesat.search.datamodel.search.SearchDataObject;
import no.sesat.search.result.BasicNavigationItem;
import no.sesat.search.result.NavigationItem;
import no.sesat.search.result.ResultItem;
import no.sesat.search.result.ResultList;
import no.sesat.search.site.config.TextMessages;
import org.apache.log4j.Logger;


/**
 * @author maek
 * @version $Id$
 */
public final class ResultPagingNavigationController
        implements NavigationControllerFactory<ResultPagingNavigationConfig>, NavigationController {

    private static final Logger LOG = Logger.getLogger(ResultPagingNavigationController.class);

    private ResultPagingNavigationConfig config;

    public NavigationController get(final ResultPagingNavigationConfig nav) {
        this.config = nav;
        return this;
    }

    public NavigationItem getNavigationItems(Context context) {

        final SearchDataObject search = context.getDataModel().getSearch(config.getCommandName());
        NavigationItem item = null;

        if (search == null) {
            LOG.info("Could not find search result for command " + config.getCommandName());

        }else{

            final ResultList<? extends ResultItem> searchResult = search.getResults();

            int hitCount;
            if(config.getHitcountSource().length() > 0) {
                hitCount = Integer.parseInt(searchResult.getField(config.getHitcountSource()));
            }
            else {
                hitCount = searchResult.getHitCount();
            }
            final StringDataObject offsetString = context.getDataModel().getParameters().getValue("offset");
            final int offset = offsetString == null ? 0 : Integer.parseInt(offsetString.getUtf8UrlEncoded());

            item = new BasicNavigationItem();
            final PagingHelper pager = new PagingHelper(hitCount, config.getPageSize(), offset, config.getNumberOfPages());

            final TextMessages messages = TextMessages.valueOf(context.getSite());

            // Add navigation item for previous page.
            if (pager.getCurrentPage() > 1) {
                final String pageOffset = Integer.toString(pager.getOffsetOfPage(pager.getCurrentPage() - 1));
                final String url = context.getUrlGenerator().getURL(pageOffset, config);
                item.addResult(new BasicNavigationItem(messages.getMessage("prev"), url, config.getPageSize()));
            }

            // Add navigation items for the individual pages.
            for (int i = pager.getFirstVisiblePage(); i <= pager.getLastVisiblePage(); ++i) {
                final String pageOffset = Integer.toString(pager.getOffsetOfPage(i));
                final String url = context.getUrlGenerator().getURL(pageOffset, config);
                final BasicNavigationItem navItem = new BasicNavigationItem(Integer.toString(i), url, config.getPageSize());

                navItem.setSelected(i == pager.getCurrentPage());

                item.addResult(navItem);
            }

            // Add navigation item for next page.
            if (pager.getCurrentPage() < pager.getNumberOfPages()) {
                final String pageOffset = Integer.toString(pager.getOffsetOfPage(pager.getCurrentPage() + 1));
                final String url = context.getUrlGenerator().getURL(pageOffset, config);
                item.addResult(new BasicNavigationItem(messages.getMessage("next"), url, config.getPageSize()));
            }
        }
        return item;
    }
}
