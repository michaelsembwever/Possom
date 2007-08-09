/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 */
/*
 * OvertureSearchResult.java
 *
 * Created on February 28, 2006, 2:00 PM
 *
 */
package no.schibstedsok.searchportal.result;

/**
 *
 * @param T
 * @author magnuse
 * @version $Id$
 */
public final class OvertureSearchResult<T extends ResultItem> extends BasicResultList<T> {

    private boolean ppcTopListQuery = false;

    /**
     *
     * @param top
     */
    public OvertureSearchResult(final boolean top) {
        super();
        ppcTopListQuery = top;
    }

    /**
     *
     * @return
     */
    public boolean isTopListQuery() {
        return ppcTopListQuery;
    }
}
