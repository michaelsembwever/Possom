/*
 * Copyright (2005-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License
 * 
 */
package no.sesat.searchportal.result;

import java.io.Serializable;

/**
 * @deprecated replaced by SEARCH-3159 - Replace PagingDisplayHelper
 * 
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Id$</tt>
 */
public final class PagingDisplayHelper implements Serializable {

    private int pageSize = 10;
    private int maxPages = 10;

    private int numberOfResults;

    private int currentOffset = 0;

    /**
     * 
     * @param numberOfResults 
     */
    public PagingDisplayHelper(final int numberOfResults) {
        this.numberOfResults = numberOfResults;
    }

    /**
     * 
     * @param numberOfResults 
     * @param pageSize 
     * @param maxPages 
     */
    public PagingDisplayHelper(final int numberOfResults, final int pageSize, final int maxPages) {
        this.numberOfResults = numberOfResults;
        this.pageSize = pageSize;
        this.maxPages = maxPages;
    }

    /**
     * 
     * @param pageSize 
     * @param maxPages 
     */
    public PagingDisplayHelper(final int pageSize, final int maxPages) {
        this.pageSize = pageSize;
        this.maxPages = maxPages;
    }

    /**
     * 
     * @return 
     */
    public int getCurrentPage() {
        return currentOffset / pageSize + 1;
    }

    /**
     * 
     * @return 
     */
    public int getNumberOfPages() {
        return (numberOfResults + pageSize - 1) / pageSize;
    }

    /**
     * 
     * @return 
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * 
     * @param pageSize 
     */
    public final void setPageSize(final int pageSize) {
        this.pageSize = pageSize;
    }
    
    /**
     * 
     * @return 
     */
    public boolean isFirstPage() {
        return getCurrentPage() == 1;
    }

    /**
     * 
     * @return 
     */
    public boolean isLastPage() {
        return getCurrentPage() == getNumberOfPages();
    }

    /**
     * 
     * @param offset 
     */
    public void setCurrentOffset(final int offset) {
        currentOffset = offset;
    }

    /**
     * 
     * @return 
     */
    public int getOffsetOfNextPage() {
        return getFirstHitOnPage() + pageSize - 1;
    }

    /**
     * 
     * @return 
     */
    public int getOffsetOfPreviousPage() {
        return (getCurrentPage() - 2) * pageSize;
    }

    /**
     *
     * @param page
     * @return
     */
    public int getOffsetOfPage(final int page) {
        return (page - 1) * (pageSize);
    }

    /**
     * 
     * @return 
     */
    public int getFirstVisiblePage() {

        int firstPage = 0;
        int n = (getCurrentOffset()/pageSize);
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

    /**
     * 
     * @return 
     */
    public int getLastVisiblePage() {
        int pageSet = getFirstVisiblePage() + maxPages - 1;

        return getNumberOfPages() < pageSet ? getNumberOfPages() : pageSet;
    }

    /**
     * 
     * @return 
     */
    public int getMaxPages() {
        return maxPages;
    }

    /**
     * 
     * @return 
     */
    public int getFirstHitOnPage() {
        return (getCurrentPage() - 1) * pageSize + currentOffset % pageSize + 1;
    }

    /**
     * 
     * @return 
     */
    public int getLastHitOnPage() {
        return Math.min(numberOfResults, getFirstHitOnPage() + pageSize - 1);
    }

    /**
     * 
     * @param numberOfResults 
     */
    public void setNumberOfResults(final int numberOfResults) {
        this.numberOfResults = numberOfResults;
    }

    /**
     * 
     * @return 
     */
    public int getNumberOfResults() {
        return numberOfResults;
    }
    
    /**
     * 
     * @return 
     */
    public int getCurrentOffset() {
        return currentOffset;
    }
}
