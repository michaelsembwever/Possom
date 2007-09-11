/*
 * Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 */

/**
 * @author <a href="mailto:magnus.eklund@sesam.no">Magnus Eklund</a>
 */
package no.sesat.search.view.navigation;

public final class PagingHelper {

    private final int hitCount;
    private final int pageSize;
    private final int offset;
    private final int maxPages;

    public PagingHelper(final int hitCount, final int pageSize, final int offset, final int maxPages) {
        this.hitCount = hitCount;
        this.pageSize = pageSize;
        this.offset = offset;
        this.maxPages = maxPages;
    }

    public int getFirstVisiblePage() {

        int firstPage;
        int n = (offset/pageSize);
        if (n > 5) {
            if ( ( getNumberOfPages() - getCurrentPage() ) < 5) {
                firstPage = getNumberOfPages() - 9;
                if (firstPage <= 0) {
                    firstPage = 1;
                }
            } else {
                firstPage = (n - 5 + 1);
            }
        } else {
            firstPage = 1;
        }

        return firstPage;
    }

    public int getLastVisiblePage() {
        int pageSet = getFirstVisiblePage() + maxPages - 1;

        return getNumberOfPages() < pageSet ? getNumberOfPages() : pageSet;
    }

    public int getOffsetOfPage(final int page) {
        return (page - 1) * (pageSize);
    }

    public int getNumberOfPages() {
        return (hitCount + pageSize - 1) / pageSize;
    }

    public int getCurrentPage() {
        return offset / pageSize + 1;
    }
}
