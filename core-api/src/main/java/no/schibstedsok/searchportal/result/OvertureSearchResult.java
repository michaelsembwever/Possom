// Copyright (2007) Schibsted Søk AS
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
