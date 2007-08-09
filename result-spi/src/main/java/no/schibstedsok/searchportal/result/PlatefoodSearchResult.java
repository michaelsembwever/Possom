/* Copyright (2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.schibstedsok.no/confluence/display/SESAT/SESAT+License
 */
/*
 * PlatefoodSearchResult.java
 *
 * Created on 24. august 2006, 15:23
 *
 */

package no.schibstedsok.searchportal.result;

/**
 *
 * @param T
 * @author SSTHKJER
 * @version $Id$
 */
public final class PlatefoodSearchResult<T extends ResultItem> extends BasicResultList<T> {

    private boolean ppcTopListQuery = false;

    /**
     *
     * @param cmd
     * @param top
     */
    public PlatefoodSearchResult( final boolean top) {
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
