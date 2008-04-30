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
import static no.sesat.search.view.navigation.ResultPagingNavigationConfig.OFFSET_KEY;
import org.apache.log4j.Logger;


/** Paging navigation controller.
 *
 * @author <a href="mailto:magnus.eklund@gmail.com">Magnus Eklund</a>
 * @version $Id$
 */
public final class ResultPagingNavigationController
        implements NavigationControllerFactory<ResultPagingNavigationConfig>, NavigationController {

    private static final Logger LOG = Logger.getLogger(ResultPagingNavigationController.class);
    
    public static final String CURRENT_PAGE = "currentPage";
    public static final String NUMBER_OF_PAGES = "numberOfPages";
    public static final String FIRST_VISIBLE_PAGE = "firstVisiblePage";
    public static final String LAST_VISIBLE_PAGE = "lastVisiblePage";
    public static final String CURRENT_PAGE_FROM_COUNT = "currentPageFromCount";
    public static final String CURRENT_PAGE_TO_COUNT = "currentPageToCount";
    
    private static final String MSG_PREV = "prev";
    private static final String MSG_NEXT = "next";

    private ResultPagingNavigationConfig config;

    public NavigationController get(final ResultPagingNavigationConfig nav) {

        this.config = nav;
        return this;
    }

    public NavigationItem getNavigationItems(final Context context) {

        final SearchDataObject search = context.getDataModel().getSearch(config.getCommandName());
        NavigationItem item = null;

        if (search == null) {
            LOG.info("Could not find search result for command " + config.getCommandName());

        }else{

            final ResultList<? extends ResultItem> searchResult = search.getResults();

            final int hitCount;
            if(config.getHitcountSource().length() > 0 && null != searchResult.getField(config.getHitcountSource())) {
                hitCount = Integer.parseInt(searchResult.getField(config.getHitcountSource()));
            }else {
                hitCount = searchResult.getHitCount();
            }
            final StringDataObject offsetString = context.getDataModel().getParameters().getValue(OFFSET_KEY);
            final int offset = offsetString == null ? 0 : Integer.parseInt(offsetString.getUtf8UrlEncoded());

            item = new BasicNavigationItem();

            final PagingHelper pager
                    = new PagingHelper(hitCount, config.getPageSize(), offset, config.getNumberOfPages());

            // it is useful to have these fields in most the original search command and in the NavigationItem
            searchResult.addField(CURRENT_PAGE, Integer.toString(pager.getCurrentPage()));
            item.addField(CURRENT_PAGE, searchResult.getField(CURRENT_PAGE));
            searchResult.addField(NUMBER_OF_PAGES, Integer.toString(pager.getNumberOfPages()));
            item.addField(NUMBER_OF_PAGES, searchResult.getField(NUMBER_OF_PAGES));
            searchResult.addField(FIRST_VISIBLE_PAGE, Integer.toString(pager.getFirstVisiblePage()));
            item.addField(FIRST_VISIBLE_PAGE, searchResult.getField(FIRST_VISIBLE_PAGE));
            searchResult.addField(LAST_VISIBLE_PAGE, Integer.toString(pager.getLastVisiblePage()));
            item.addField(LAST_VISIBLE_PAGE, searchResult.getField(LAST_VISIBLE_PAGE));
            searchResult.addField(CURRENT_PAGE_FROM_COUNT, Integer.toString(pager.getCurrentPageFromCount()));
            item.addField(CURRENT_PAGE_FROM_COUNT, searchResult.getField(CURRENT_PAGE_FROM_COUNT));
            searchResult.addField(CURRENT_PAGE_TO_COUNT, Integer.toString(pager.getCurrentPageToCount()));
            item.addField(CURRENT_PAGE_TO_COUNT, searchResult.getField(CURRENT_PAGE_TO_COUNT));

            final TextMessages messages = TextMessages.valueOf(context.getSite());

            // Add navigation item for previous page.
            if (pager.getCurrentPage() > 1) {
                final String pageOffset = Integer.toString(pager.getOffsetOfPage(pager.getCurrentPage() - 1));
                final String url = context.getUrlGenerator().getURL(pageOffset, config);
                item.addResult(new BasicNavigationItem(messages.getMessage(MSG_PREV), url, config.getPageSize()));
            }

            // Add navigation items for the individual pages.
            for (int i = pager.getFirstVisiblePage(); i <= pager.getLastVisiblePage(); ++i) {

                final String pageOffset = Integer.toString(pager.getOffsetOfPage(i));
                final String url = context.getUrlGenerator().getURL(pageOffset, config);

                final BasicNavigationItem navItem
                        = new BasicNavigationItem(Integer.toString(i), url, config.getPageSize());

                navItem.setSelected(i == pager.getCurrentPage());

                item.addResult(navItem);
            }

            // Add navigation item for next page.
            if (pager.getCurrentPage() < pager.getNumberOfPages()) {
                final String pageOffset = Integer.toString(pager.getOffsetOfPage(pager.getCurrentPage() + 1));
                final String url = context.getUrlGenerator().getURL(pageOffset, config);
                item.addResult(new BasicNavigationItem(messages.getMessage(MSG_NEXT), url, config.getPageSize()));
            }
        }
        return item;
    }
}
