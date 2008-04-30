/*
 * Copyright (2007) Schibsted Søk AS
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
 *
 * @author <a href="mailto:magnus.eklund@gmail.com">Magnus Eklund</a>
 * @version $Id$
 */
package no.sesat.search.view.navigation;

/* Immutable helper class for handling offsets.
 * 
 **/ 
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
    
    public int getCurrentPageFromCount(){
        return offset + 1;
    }
    
    public int getCurrentPageToCount(){
        return Math.min(offset + pageSize, hitCount);
    }
}
