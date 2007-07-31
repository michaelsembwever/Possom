/* Copyright (2005-2007) Schibsted Søk AS
 *
 * Jul 26, 2007 9:19:47 AM
 */
package no.schibstedsok.searchportal.mode.navigation;

import no.schibstedsok.searchportal.result.NavigationItem;
import no.schibstedsok.searchportal.result.ResultItem;
import no.schibstedsok.searchportal.result.ResultList;
import no.schibstedsok.searchportal.result.BasicNavigationItem;
import no.schibstedsok.searchportal.result.NavigationHelper;
import no.schibstedsok.searchportal.datamodel.generic.StringDataObject;
import no.schibstedsok.searchportal.view.i18n.TextMessages;

/**
 * @author <a href="mailto:magnus.eklund@sesam.no">Magnus Eklund</a>
 */
public final class ResultPagingNavigationController
        implements NavigationControllerFactory<ResultPagingNavigationConfig>, NavigationController {

    private ResultPagingNavigationConfig config;

    public NavigationController get(final ResultPagingNavigationConfig nav) {
        this.config = nav;
        return this;
    }

    public NavigationItem getNavigationItems(Context context) {
        final ResultList<? extends ResultItem> searchResult = context.getDataModel().getSearch(config.getCommandName()).getResults();

        final int hitCount = searchResult.getHitCount();
        final StringDataObject offsetString = context.getDataModel().getParameters().getValue("offset");
        final int offset = offsetString == null ? 0 : Integer.parseInt(offsetString.getUtf8UrlEncoded());

        final NavigationItem item = new BasicNavigationItem();
        final PagingHelper pager = new PagingHelper(hitCount, config.getPageSize(), offset, config.getPagingSize());

        final TextMessages messages = TextMessages.valueOf(context.getSite());

        // Add navigation item for previous page.
        if (pager.getCurrentPage() > 1) {
            final String pageOffset = Integer.toString(pager.getOffsetOfPage(pager.getCurrentPage() - 1));
            final String url = NavigationHelper.getUrlFragment(context.getDataModel(), config, pageOffset, null);
            item.addResult(new BasicNavigationItem(messages.getMessage("prev"), url, config.getPageSize()));
        }

        // Add navigation items for the individual pages.
        for (int i = pager.getOffsetOfPage(pager.getFirstVisiblePage()); i <= pager.getLastVisiblePage(); ++i) {
            final String pageOffset = Integer.toString(pager.getOffsetOfPage(i));
            final String url = NavigationHelper.getUrlFragment(context.getDataModel(), config, pageOffset, null);
            item.addResult(new BasicNavigationItem(pageOffset, url, config.getPageSize()));
        }

        // Add navigation item for next page.
        if (pager.getCurrentPage() < pager.getNumberOfPages()) {
            final String pageOffset = Integer.toString(pager.getOffsetOfPage(pager.getCurrentPage() + 1));
            final String url = NavigationHelper.getUrlFragment(context.getDataModel(), config, pageOffset, null);
            item.addResult(new BasicNavigationItem(messages.getMessage("next"), url, config.getPageSize()));
        }

        return item;
    }

    private class PagingHelper {

        private final int hitCount;
        private final int pageSize;
        private final int offset;
        private final int maxPages;

        public PagingHelper(int hitCount, int pageSize, int offset, int maxPages) {
            this.hitCount = hitCount;
            this.pageSize = pageSize;
            this.offset = offset;
            this.maxPages = maxPages;
        }

        public int getFirstVisiblePage() {

            int firstPage = 0;
            int n = (offset/pageSize);
            if (n > 5)
                if ( ( getNumberOfPages() - getCurrentPage() ) < 5) {
                    firstPage = getNumberOfPages() - 9;
                    if (firstPage <= 0)
                        firstPage = 1;
                } else
                    firstPage = (n - 5 + 1);
            else
                firstPage = 1;

            return firstPage;
        }

        public int getLastVisiblePage() {
            int pageSet = getFirstVisiblePage() + maxPages - 1;

            return getNumberOfPages() < pageSet ? getNumberOfPages() : pageSet;
        }

        public int getOffsetOfPage(final int page) {
            return (page - 1) * (pageSize);
        }

        private int getNumberOfPages() {
            return (hitCount + pageSize - 1) / pageSize;
        }

        private int getCurrentPage() {
            return offset / pageSize + 1;
        }
    }
}
