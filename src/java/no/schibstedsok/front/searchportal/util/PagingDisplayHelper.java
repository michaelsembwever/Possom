/*
 * Copyright (2005) Schibsted Søk AS
 * 
 */
package no.schibstedsok.front.searchportal.util;
/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public class PagingDisplayHelper {

    private int pageSize = 10;
    private int maxPages = 10;

    private int numberOfResults;

    private int currentOffset = 0;

    public PagingDisplayHelper(int numberOfResults) {
        this.numberOfResults = numberOfResults;
    }

    public PagingDisplayHelper(int numberOfResults, int pageSize, int maxPages) {
        this.numberOfResults = numberOfResults;
        this.pageSize = pageSize;
        this.maxPages = maxPages;
    }

    public PagingDisplayHelper(int pageSize, int maxPages) {
        this.pageSize = pageSize;
        this.maxPages = maxPages;
    }

    public int getCurrentPage() {
        return currentOffset / pageSize + 1;
    }

    public int getNumberOfPages() {
        return (numberOfResults + pageSize - 1) / pageSize;
    }

    public int getPageSize() {
        return pageSize;
    }

    public boolean isFirstPage() {
        return getCurrentPage() == 1;
    }

    public boolean isLastPage() {
        return getCurrentPage() == getNumberOfPages();
    }

    public void setCurrentOffset(int offset) {
        currentOffset = offset;
    }

    public int getOffsetOfNextPage() {
        return getFirstHitOnPage() + pageSize - 1;
    }

    public int getOffsetOfPreviousPage() {
        return (getCurrentPage() - 2) * pageSize;
    }

    public int getOffsetOfPage(int page) {
        return (page - 1) * (pageSize);
    }

    public int getFirstVisiblePage() {
        int firstPage = 0;
        if (getCurrentPage() >= 7) {
            if (((numberOfResults + pageSize - 1) / pageSize) > (getCurrentPage() + 4))
                firstPage = getCurrentPage() - 5;
            else
                firstPage = ((numberOfResults + pageSize - 1) / pageSize) - 9;
        } else
            firstPage = 1;
        return firstPage;
    }

    public int getLastVisiblePage() {
        int pageSet = getFirstVisiblePage() + maxPages - 1;

        return getNumberOfPages() < pageSet ? getNumberOfPages() : pageSet;
    }

    public int getMaxPages() {
        return maxPages;
    }

    public int getFirstHitOnPage() {
        return (getCurrentPage() - 1) * pageSize + currentOffset % pageSize + 1;
    }

    public int getLastHitOnPage() {
        return Math.min(numberOfResults, getFirstHitOnPage() + pageSize - 1);
    }

    public void setNumberOfResults(int numberOfResults) {
        this.numberOfResults = numberOfResults;
    }

    public int getNumberOfResults() {
        return numberOfResults;
    }
    
    public int getCurrentOffset() {
        return currentOffset;
    }
}
